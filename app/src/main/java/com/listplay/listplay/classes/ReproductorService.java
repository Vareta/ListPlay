package com.listplay.listplay.classes;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.util.PlayerControl;
import com.google.android.exoplayer.util.Util;
import com.listplay.listplay.R;
import com.listplay.listplay.activities.Reproductor;
import com.listplay.listplay.models.Video;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

public class ReproductorService extends Service implements ExoPlayer.Listener{
    private static final String TAG = "ReproductorService";
    private static final int RENDERER_COUNT = 1;
    private static final int minBufferMs = 1000;
    private static final int minRebufferMs = 5000;
    private static final int minLoadableRetryCount = 5;
    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int BUFFER_SEGMENT_COUNT = 256;
    /**
     * Tiempo en el cual una url de audio es valida (5hrs en milisegundos)
     */
    private static final int TIEMPO_LIMITE = 18000000;
    /**
     * Para cuando se requiere reproducir el siguiente elemento de la lista
     */
    public static final int SIGUIENTE = 0;
    /**
     * Para cuando se requiere reproducir el elemento anterior de la lista
     */
    public static final int ANTERIOR = 1;
    /**
     * Para cuando la funcion shuffle se aplica para un playlist desde el inicio de su reproduccion
     */
    public static final int DESDE_EL_INICIO = 0;
    /**
     * Para cuando la funcion shuffle se aplica para cuando ya se encuentra reproduciendo algun item de la playlist
     * o cuando se inicia la reproduccion desde un video en especifico, pero en modo shuffle
     */
    public static final int DESDE_X_VIDEO = 1;
    private Allocator allocator;
    private DataSource dataSource;
    private int posVideoAnterior;
    private int posVideoActual;
    private int posVideoSiguiente;

    private ExoPlayer exoPlayer;
    private List<Video> aReproducir;
    private String userAgent;
    PlayerControl playerControl;
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    private ExoPlayer.Listener exoListener;

    public ReproductorService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public ReproductorService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ReproductorService.this;
        }
    }

    /**
     * Realiza el proceso de reproduccion del video, llevando a cabo todos los procesos necesarios para ello.
     * De igual menera dentro de esta funcion se setea la lista de reproduccion y la posicion de los videos (actual
     * anterior y siguiente).
     * Esta es la primera funcion que se llama luego de iniciar el service como foreground
     * @param videosAReproducir Lista de videos a reproducir
     * @param posVideoAReproducir posicion dentro de la lista del video a reproducir
     */
    public void reproducirInicial(List<Video> videosAReproducir, int posVideoAReproducir) {
        aReproducir = videosAReproducir;
        userAgent = Util.getUserAgent(getBaseContext(), "ListPlay");
        exoListener = this;
        new ReproducirIni().execute(posVideoAReproducir);
    }

    /**
     * Reproduccion inicial asynctask
     */
    private class ReproducirIni extends AsyncTask<Integer, Void, Void> {
        int posVideo;
        Uri uri;
        @Override
        protected Void doInBackground(Integer... posVideoAReproducir) {
            posVideo = posVideoAReproducir[0];
            if (aCaducado(posVideo)) {
                Youtube youtube = new Youtube();
                uri = Uri.parse(youtube.getUrlAudioActualizado(aReproducir.get(posVideo).getId()));
            } else {
                uri = Uri.parse(aReproducir.get(posVideo).getUrlVideoAudio());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (exoPlayer != null && isPlaying()) {
                exoPlayer.stop();
                exoPlayer.seekTo(0);
            } else {
                exoPlayer = ExoPlayer.Factory.newInstance(RENDERER_COUNT, minBufferMs, minRebufferMs);
                allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE);
                dataSource = new DefaultUriDataSource(getBaseContext(), userAgent);
                playerControl = new PlayerControl(exoPlayer);
                exoPlayer.addListener(exoListener);
                background();
            }

            ExtractorSampleSource sampleSource = new ExtractorSampleSource(uri, dataSource, allocator, BUFFER_SEGMENT_SIZE * BUFFER_SEGMENT_COUNT, minLoadableRetryCount);
            MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource, MediaCodecSelector.DEFAULT, null, true, null, null, AudioCapabilities.getCapabilities(getBaseContext()), AudioManager.STREAM_MUSIC);

            exoPlayer.prepare(audioRenderer);
            exoPlayer.setPlayWhenReady(true);
            if (exoPlayer.getPlayWhenReady()) {
                posVideoActual = posVideo;
                posVideoAnterior = getPosVideoAnterior();
                posVideoSiguiente = getPosVideoSiguiente();
            }
            sendAvisoInicieSigVideo();
        }

    }

    /**
     * Realiza el proceso de reproduccion de video, pero para la segunda etapa, es decir, cuando se ocupan
     * los botones nextSong o prevSong
     * @param mode indica si se reproduce la siguiente cancion de la lista o la anterior
     */
    public void reproducir(int mode) {
        exoPlayer.stop();
        exoPlayer.seekTo(0);
        if (mode == SIGUIENTE) {
            posVideoActual = getPosVideoSiguiente();
        } else { //anterior
            posVideoActual = getPosVideoAnterior();
        }

        new Reproducir().execute();
    }

    /**
     * reproducir asynctask
     */
    private class Reproducir extends AsyncTask<Void, Void, Void> {
        Uri uri;
        @Override
        protected Void doInBackground(Void... posVideoAReproducir) {
            if (aCaducado(posVideoActual)) {
                Youtube youtube = new Youtube();
                uri = Uri.parse(youtube.getUrlAudioActualizado(aReproducir.get(posVideoActual).getId()));
            } else {
                uri = Uri.parse(aReproducir.get(posVideoActual).getUrlVideoAudio());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            ExtractorSampleSource sampleSource = new ExtractorSampleSource(uri, dataSource, allocator, BUFFER_SEGMENT_SIZE * BUFFER_SEGMENT_COUNT, minLoadableRetryCount);
            MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource, MediaCodecSelector.DEFAULT, null, true, null, null, AudioCapabilities.getCapabilities(getBaseContext()), AudioManager.STREAM_MUSIC);

            exoPlayer.prepare(audioRenderer);
            exoPlayer.setPlayWhenReady(true);
            if (exoPlayer.getPlayWhenReady()) {
                posVideoAnterior = getPosVideoAnterior();
                posVideoSiguiente = getPosVideoSiguiente();
            }
            sendAvisoInicieSigVideo();
        }

    }

    private void background() {
        int id = 1;
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.play_circle_outline)
                .setContentTitle("Notificacion")
                .setContentText("asdasd")
                .setPriority(Notification.PRIORITY_DEFAULT);
        Intent notificationIntent = new Intent(this, Reproductor.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        notificationIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        notification.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification.build());
        startForeground(id, notification.build());


    }

    /**
     * Obtiene la posicion actual del video dentro de la lista de reproduccion que se esta reproduciendo
     * @return la posicion en la lista del video
     */
    public int getPosVideoActual() {
        return posVideoActual;
    }

    /**
     * Obtiene la posicion del video anterior con respecto al que se esta reproduciendo actualmente
     * @return la posicion en la lista del video
     */
    public int getPosVideoAnterior() {
        if (posVideoActual == 0) { //si el video actual es el primero
            return aReproducir.size() - 1; //entrega el ultimo
        } else {
            return posVideoActual - 1;
        }
    }

    /**
     * Obtiene la posicion del siguiente video con respecto al que se esta reproduciendo actualmente
     * @return la posicion en la lista del video
     */
    public int getPosVideoSiguiente() {
        if (posVideoActual == (aReproducir.size() - 1)) { //si el video actual es el ultimo
            return 0; //entrega el primero
        } else {
            return posVideoActual + 1;
        }
    }

    /**
     * Indica si el reproductor se encuentra reproduciendo algun video
     * @return booleando indicando el resultado
     */
    public boolean isPlaying() {
        return playerControl.isPlaying();
    }

    /**
     * Inicia la reproduccion
     */
    public void start() {
        playerControl.start();
    }

    /**
     * pausa la reproduccion
     */
    public void pause() {
        playerControl.pause();
    }

    /**
     * Obtiene la posicion actual de reproduccion del video
     * @return un entero que determina la posicion en milisegundos
     */
    public int getCurrentPosition() {
        return playerControl.getCurrentPosition();
    }

    /**
     * Obtiene el video actual que se esta reproduciendo
     * @return el video en cuestion
     */
    public Video getVideoActual() {
        return aReproducir.get(posVideoActual);
    }

    /**
     * Obtiene la duracion total del video
     * @return duracion en milisegundos
     */
    public int getDuration() {
        return playerControl.getDuration();
    }

    /**
     * Obtiene el valor de cuanto se ha "cargado" (buffer) del video
     * @return un entero que determina el valor
     */
    public long getBufferedPosition() {
        return exoPlayer.getBufferedPosition();
    }

    /**
     * Consulta si la instancia de exoplayer es nula
     * @return verdadero o falso
     */
    public boolean isExoPlayerNull() {
        return exoPlayer == null;
    }

    /**
     * Shuffle mode para la lista de videos
     * @param mode indica si es desde el inicio o si es desde una instancia en donde ya se encuentra reproduciendo
     *             algun video
     */
    private void shuffle(int mode) {
        if (mode == DESDE_EL_INICIO) {
            Collections.shuffle(aReproducir);
        } else {
            Video actual = aReproducir.get(posVideoActual);
            aReproducir.remove(posVideoActual);
            Collections.shuffle(aReproducir);
            aReproducir.add(0, actual);
            posVideoActual = 0;
        }
    }

    /**
     * Verifica si el url de audio del vidoe a caducado
     * @param posicion indice en la lista del elemento a consultar
     * @return verdero o falso
     */
    private boolean aCaducado(int posicion) {
        long diferencia = (new DateTime().getMillis()) - aReproducir.get(posicion).getTiempoLimite();
        if (diferencia > TIEMPO_LIMITE) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Envia un aviso a la actividad reproductor indicando que ahora reproduce el siguiente video
     */
    private void sendAvisoInicieSigVideo() {
        Intent intent = new Intent("siguiente");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch(playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                Log.d(TAG, "State Buffering");
                break;
            case ExoPlayer.STATE_ENDED:
                Log.d(TAG, "State ended");
                reproducir(SIGUIENTE);
                break;
            case ExoPlayer.STATE_IDLE:
                Log.d(TAG, "State Idle");
                break;
            case ExoPlayer.STATE_PREPARING:
                Log.d(TAG, "State Preparing");
                break;
            case ExoPlayer.STATE_READY:
                Log.d(TAG, "State Ready");
                break;
            default:
                break;
        }
    }

    @Override
    public void onPlayWhenReadyCommitted() {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }
}
