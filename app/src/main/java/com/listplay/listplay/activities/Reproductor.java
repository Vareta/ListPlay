package com.listplay.listplay.activities;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.listplay.listplay.R;
import com.listplay.listplay.classes.CRUD;
import com.listplay.listplay.classes.Preferencias;
import com.listplay.listplay.classes.ReproductorService;
import com.listplay.listplay.classes.ReproductorService.LocalBinder;
import com.listplay.listplay.models.Video;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

import java.util.List;


public class Reproductor extends AppCompatActivity {
    private ImageButton prevList;
    private ImageButton prevSong;
    private ImageButton playStop;
    private ImageButton nextSong;
    private ImageButton nextList;
    private ImageButton repeat;
    private ImageButton shuffle;
    private ImageView portada;
    private TextView tiempoActual;
    private TextView tiempoTotal;
    private TextView nombreArtista;
    private TextView nombreCancion;
    private SeekBar seekBar;
    private ReproductorService mService;
    private boolean mBound;
    private long playListAReproducir;
    private int posVideoAReproducir;
    private List<Video> videosAReproducir;
    private Handler threadHandler = new Handler();
    private Video videoActual;
    private Preferencias pref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reproductor);
        pref = new Preferencias();
        iniciaElementos();
        buttonListeners();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            playListAReproducir = bundle.getLong("playlistid");
            posVideoAReproducir = bundle.getInt("posicion");
            new Reproducir().execute();
        }

    }

    private void iniciaElementos() {
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        tiempoActual = (TextView)findViewById(R.id.tiempoActual);
        tiempoTotal = (TextView)findViewById(R.id.tiempoTotal);
        nombreArtista = (TextView)findViewById(R.id.nombreArtista);
        nombreCancion = (TextView)findViewById(R.id.nombreCancion);
        prevList = (ImageButton)findViewById(R.id.prevList);
        prevSong = (ImageButton)findViewById(R.id.prevSong);
        playStop = (ImageButton)findViewById(R.id.playStop);
        nextSong = (ImageButton)findViewById(R.id.nextSong);
        nextList = (ImageButton)findViewById(R.id.nextList);
        repeat = (ImageButton)findViewById(R.id.repeat);
        shuffle = (ImageButton)findViewById(R.id.shuffle);
        portada = (ImageView)findViewById(R.id.imagen);

        if (!pref.isRepeat(this)) {
            repeat.setColorFilter(R.color.gris);
        }
        Intent intent = new Intent(getBaseContext(), ReproductorService.class);
        startService(intent);
    }

    private void buttonListeners() {
        playStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mService.isPlaying()) {
                    mService.pause();
                } else {
                    mService.start();
                }
            }
        });

        prevSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.reproducir(ReproductorService.ANTERIOR);
                setInfoVideo();
            }
        });

        nextSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.reproducir(ReproductorService.SIGUIENTE);
                setInfoVideo();
            }
        });
    }

    // Thread to Update position for SeekBar.
    private class UpdateSeekBarThread implements Runnable {

        public void run()  {
            if (!mService.isExoPlayerNull()) {
                int posicionActual = mService.getCurrentPosition();
                int duracionTotal = mService.getDuration();
                posVideoAReproducir = mService.getPosVideoActual();
                Long buffered = mService.getBufferedPosition();
                seekBar.setMax(duracionTotal);
                seekBar.setProgress(posicionActual);
                seekBar.setSecondaryProgress(buffered.intValue());
                tiempoActual.setText(new DateTime(posicionActual).toString("mm:ss"));
                tiempoTotal.setText(new DateTime(duracionTotal).toString("mm:ss"));
                // Delay thread 1000 milisecond.
                if (mService.isPlaying()) {
                    playStop.setSelected(true);
                } else {
                    playStop.setSelected(false);
                }

                threadHandler.postDelayed(this, 500);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, ReproductorService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Inicializa el thread que monitorea la seekbar
        UpdateSeekBarThread update = new UpdateSeekBarThread();
        threadHandler.postDelayed(update, 500);

        //Inicializa el localbroadcast que monitorea cuando se reproduce la siguiente cancion (automaticamente desde el service)
        IntentFilter intentFilter = new IntentFilter("siguiente");
        LocalBroadcastManager.getInstance(this).registerReceiver(sigVideoReceiver, intentFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(sigVideoReceiver);
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            if (nombreArtista.getText().toString().equals("")) {
                if (!mService.isExoPlayerNull()) {
                    setInfoVideo();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private class Reproducir extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            CRUD crud = new CRUD();
            videosAReproducir = crud.getVideosPlaylist(playListAReproducir);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mService.reproducirInicial(videosAReproducir, posVideoAReproducir);
        }

    }

    private void setInfoVideo() {
        videoActual = mService.getVideoActual();
        if (videoActual.getArtista() != null) {
            nombreArtista.setText(videoActual.getArtista());
            nombreCancion.setText(videoActual.getCancion());
        } else {
            nombreCancion.setText(videoActual.getTitulo());
        }
        Picasso.with(this).load(videoActual.getUrlImagen()).into(portada);
    }

    private BroadcastReceiver sigVideoReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            setInfoVideo();
        }
    };

}
