package com.listplay.listplay.activities;

import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

import com.listplay.listplay.R;
import com.listplay.listplay.classes.CRUD;
import com.listplay.listplay.fragments.AddToPlayListDialog;
import com.listplay.listplay.fragments.EditVideoDialog;
import com.listplay.listplay.classes.Preferencias;
import com.listplay.listplay.classes.ReproductorService;
import com.listplay.listplay.classes.ReproductorService.LocalBinder;
import com.listplay.listplay.classes.Utilities;
import com.listplay.listplay.fragments.SleepTimerDialog;
import com.listplay.listplay.models.Video;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

import java.util.List;


public class Reproductor extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, EditVideoDialog.EditVideoListener,
                                                 SleepTimerDialog.SleepTimerListener {
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
    private Handler threadHandler;
    private Video videoActual;
    private Preferencias pref;
    private Utilities util;
    private Bundle bundle;
    private Toolbar toolbar;
    private CRUD crud;
    private int contador;
    public static final int DE_PLAYLIST = 0;
    public static final int FORMA_PERMANENTE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reproductor);
        pref = new Preferencias();
        util = new Utilities();
        threadHandler = new Handler();
        contador = 0;
        iniciaElementos();
        iniciaToolbar();
        buttonListeners(this);
    }

    /**
     * Inicia todos los elemnetos de la actividad
     */
    private void iniciaElementos() {
        crud = new CRUD();
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);
        tiempoActual = (TextView) findViewById(R.id.tiempoActual);
        tiempoTotal = (TextView) findViewById(R.id.tiempoTotal);
        nombreArtista = (TextView) findViewById(R.id.nombreArtista);
        nombreCancion = (TextView) findViewById(R.id.nombreCancion);
        prevList = (ImageButton) findViewById(R.id.prevList);
        prevSong = (ImageButton) findViewById(R.id.prevSong);
        playStop = (ImageButton) findViewById(R.id.playStop);
        nextSong = (ImageButton) findViewById(R.id.nextSong);
        nextList = (ImageButton) findViewById(R.id.nextList);
        repeat = (ImageButton) findViewById(R.id.repeat);
        shuffle = (ImageButton) findViewById(R.id.shuffle);
        portada = (ImageView) findViewById(R.id.imagen);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (!pref.isRepeat(this) && !pref.isRepeatOnce(this)) {
            repeat.setBackground(util.cambiaColorDrawable(getResources(), R.drawable.repeat, R.color.gris, getTheme()));
        }

        if (!pref.isShuffle(this)) {
            shuffle.setBackground(util.cambiaColorDrawable(getResources(), R.drawable.shuffle_variant, R.color.gris, getTheme()));
        }

        if (pref.isRepeatOnce(this)) {
            repeat.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.repeat_once, getTheme()));
        }
        Intent intent = new Intent(getBaseContext(), ReproductorService.class);
        startService(intent);
    }

    /**
     * Setea los listeners de los botones de la actividad
     *
     * @param context context de la actividad
     */
    private void buttonListeners(final Context context) {
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
                if (mService != null) {
                    mService.reproducir(ReproductorService.ANTERIOR, ReproductorService.DESDE_LOS_CONTROLES);
                    setInfoVideo();
                }
            }
        });

        nextSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mService != null) {
                    mService.reproducir(ReproductorService.SIGUIENTE, ReproductorService.DESDE_LOS_CONTROLES);
                    setInfoVideo();
                }
            }
        });

        shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pref.isShuffle(context)) {
                    pref.setShuffle(context, false);
                    shuffle.setBackground(util.cambiaColorDrawable(getResources(), R.drawable.shuffle_variant, R.color.gris, getTheme()));

                    System.out.println("shuffle off");
                } else {
                    System.out.println("shuffle on");
                    pref.setShuffle(context, true);
                    shuffle.setBackground(util.cambiaColorDrawable(getResources(), R.drawable.shuffle_variant, R.color.negro, getTheme()));
                }
            }
        });

        /**
         * Orden en cuanto al click del boton es:
         * repeat false default
         * click 1 --> repeat true
         * click 2 --> repeat one true
         */
        repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //repeat off --> repeat on
                if (!pref.isRepeat(context) && !pref.isRepeatOnce(context)) {
                    pref.setRepeat(context, true);
                    pref.setRepeatOnce(context, false);
                    repeat.setBackground(util.cambiaColorDrawable(getResources(), R.drawable.repeat, R.color.negro, getTheme()));
                    System.out.println("repeat on");
                } else if (pref.isRepeat(context)) {//repeat on --> repeat once
                    pref.setRepeat(context, false);
                    pref.setRepeatOnce(context, true);
                    repeat.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.repeat_once, getTheme()));
                    System.out.println("repeat once");
                } else { //repeat once --> repeat off
                    pref.setRepeatOnce(context, false);
                    pref.setRepeat(context, false);
                    repeat.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.repeat, getTheme()));
                    repeat.setBackground(util.cambiaColorDrawable(getResources(), R.drawable.repeat, R.color.gris, getTheme()));
                    System.out.println("repeat off");
                }

            }
        });

    }

    // Thread to Update position for SeekBar.
    private Runnable updateSeekBarThread = new Runnable() {

        public void run() {
            if (mService != null) {
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
    };


    private void iniciaToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        //Inicializa el localbroadcast que monitorea cuando se reproduce la siguiente cancion (automaticamente desde el service)
        IntentFilter intentFilter = new IntentFilter("siguiente");
        LocalBroadcastManager.getInstance(this).registerReceiver(sigVideoReceiver, intentFilter);

        //Inicializa el thread que monitorea la seekbar
        threadHandler.postDelayed(updateSeekBarThread, 500);

    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(sigVideoReceiver);
        threadHandler.removeCallbacks(updateSeekBarThread);
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            if (contador == 0) {
                bundle = getIntent().getExtras();
                if (bundle != null) {
                    playListAReproducir = bundle.getLong("playlistid");
                    posVideoAReproducir = bundle.getInt("posicion");
                    new Reproducir().execute();
                }
                contador++;
            }
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


    /**
     * INICIO SEEKBAR LISTENERS
     **/

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
        if (fromUser) {
            mService.seekTo(i);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onEditVideo(long idVideo, String nombreCancion, String artista) {
        mService.actualizaVideoEditado(idVideo, nombreCancion, artista); //actualiza el vidoe en la lista que se mantiene en memoria
        setInfoVideo(); //actualiza la informacion mostrada en la actividad
    }

    @Override
    public void onDesactivarSleepTimer() {
        mService.desactivaSleepTimer();
    }

    @Override
    public void onAceptarSleepTimer(int tiempo) {
        mService.setSleepTimer(tiempo);
    }


    /**
     * FIN SEEKBAR LISTENERS
     **/


    private class Reproducir extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            videosAReproducir = crud.getVideosPlaylist(playListAReproducir);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mService.reproducirInicial(videosAReproducir, posVideoAReproducir, playListAReproducir);
        }

    }

    /**
     * Actualiza, en background, los videos a reproducir. Generalmente se utiliza cuando el usuario
     * elimina un video de la lista de reproduccion o de forma permanente
     */
    private class ActualizaVideosAReproducir extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            videosAReproducir = crud.getVideosPlaylist(playListAReproducir);
            return null;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reproductor_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager fm = getSupportFragmentManager();
        switch (item.getItemId()) {
            case R.id.editar:
                EditVideoDialog editVideoDialog = EditVideoDialog.newInstance(mService.getVideoActual().getId());
                editVideoDialog.show(fm, "edit_video_fragment");
                break;

            case R.id.añadir:
                AddToPlayListDialog addToPlayListDialog = AddToPlayListDialog.newInstance(mService.getVideoActual().getId());
                addToPlayListDialog.show(fm, "add_to_playlist_fragment");
                break;

            case R.id.eliminar_de_lista:
                eliminarVideoDialog(mService.getVideoActual(), DE_PLAYLIST);
                break;

            case R.id.eliminar:
                eliminarVideoDialog(mService.getVideoActual(), FORMA_PERMANENTE);
                break;

            case R.id.sleep_timer:
                setSleepTimer();
                break;

            default:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Realiza el proceso de crear un Dialog para realizar la eliminacion de un video de la lista
     * de reproduccion actual o de forma permanente
     * @param videoAEliminar Video que se quiere eliminar
     * @param mode modo de eliminacion, ya sea de la lista de reproduccion o permanente
     */
    private void eliminarVideoDialog(final Video videoAEliminar, final int mode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (mode == DE_PLAYLIST) { //desde la playlist
            builder.setMessage(getString(R.string.contenido_deletedia));
        } else { //de forma permanente
            builder.setMessage(getString(R.string.contenido_deletedb));
        }

        builder.setTitle(getString(R.string.titulo_deletedia));
        builder.setPositiveButton(getString(R.string.aceptar_deletedia), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (mode == DE_PLAYLIST) { //desde la playlist
                    crud.eliminarVideoDeLista(videoAEliminar.getId(), playListAReproducir); //elimina el video de la DB (de la playlist)
                } else { //de forma permanente
                    crud.eliminarVideo(videoAEliminar.getId()); //elimina el video de la DB
                }
                if (videosAReproducir.size() == 1) { //si era el ultimo video de la lista
                    //Va hacia la vista principal, ya que no existen videos que reproducir
                    Intent intent = new Intent(Reproductor.this, Central.class);
                    startActivity(intent);
                    finish(); //para asi no volver al estado en donde Reproductor no tenia videos que reproducir
                } else {
                    mService.eliminarVideoDePlayList(videoAEliminar); //elimina el video en memoria
                    new ActualizaVideosAReproducir().execute(); //actualiza la lista de videos en la vista del reproductor
                }
                util.mensajeCorto(getBaseContext(), getString(R.string.exito_deletedia));
            }
        });
        builder.setNegativeButton(R.string.cancelar_deletedia, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //dismiss
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.colorAccent));

    }

    private void setSleepTimer() {
        FragmentManager fm = getSupportFragmentManager();
        SleepTimerDialog sleepDialog = SleepTimerDialog.newInstance();
        sleepDialog.show(fm, "sleep_timer");
    }

}
