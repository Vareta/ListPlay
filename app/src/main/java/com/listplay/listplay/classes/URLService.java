package com.listplay.listplay.classes;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.listplay.listplay.R;

import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class URLService extends IntentService {
    private static final String TAG = "classes.URLService: ";
    private String url;
    private Youtube youtube;
    CRUD crud;

    public URLService() {
        super("URLService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            url = intent.getStringExtra("url");
            if (url.contains("youtu")) {
                youtube = new Youtube();
                crud = new CRUD();
                manejarUrl(url);
            } else { //no corresponode a una url de youtube
                notificaProcesoExtraccion("La url no corresponde a Youtube");
            }
        }
    }

    /**
     * Notifica variaciones del proceso de extraccion en la barra de notificaciones
     */
    private void notificaProcesoExtraccion(String contenido) {
        int id = 1;
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.play_circle_outline)
                .setContentTitle("Notificacion")
                .setContentText(contenido)
                .setPriority(Notification.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification.build());

    }

    /**
     * Realiza el proceso de ver si la url es de un video o un lista. Luego, dependiendo el caso
     * llama a la funcion correspondiente para almacenar el video
     * @param url direccion del video o lista a guardar
     */
    private void manejarUrl(String url) {
        String id;
        if (url.contains("playlist")) {
            id = youtube.getYoutubeUrlId(url, Youtube.URL_PLAYLIST); //id de la playlist (en url)
            Document codigoFuentePlaylist = youtube.jsoupConnect(id, Youtube.URL_PLAYLIST); //obtiene el codigo fuente de la playlist en youtube
            String nombre = youtube.getNombreYoutubePlaylist(codigoFuentePlaylist); //obtiene el nombre de la playlist en youtube
            if (nombre != null) {
                List<String> urlsId = youtube.getIdVideosPlaylist(codigoFuentePlaylist); //obtiene las id (de la url) de los videos de la playlist
                if (urlsId != null) {
                    List<String> aAñadir = new ArrayList<>(); //lista auxiliar, la cual contendra las id (de la url) que se deberan añadir
                    int videosFallidos = 0, aux;
                    notificaProcesoExtraccion("Empieza la extraccion"); //notifica inicio de extraccion

                    if (!crud.existePlaylist(nombre)) { //si la playlist no se encuentra almacenada
                        crud.añadirPlayList(nombre);
                    }

                    for (int i = 0; i < urlsId.size(); i++) {
                        if (!crud.existeVideo(urlsId.get(i))) { //si no existe video
                            aux = almacenaVideo(urlsId.get(i)); //Lo intenta almacenar
                            if (aux == 0) { //si no existen problemas
                                aAñadir.add(urlsId.get(i));
                            } else { //si falla
                                videosFallidos += aux; //se contabiliza
                            }
                        } else { //video repetido, por lo que no se agrega a la db, pero si a la lista a añadir
                            aAñadir.add(urlsId.get(i));
                            Log.d(TAG, "video repetido, id: " + urlsId.get(i));
                        }
                    }

                    crud.añadirMuchosVideosAPlaylist(aAñadir, nombre);
                    notificaProcesoExtraccion("añadidos " + (urlsId.size() - videosFallidos) + "/" + urlsId.size());
                } else {
                    notificaProcesoExtraccion("Problema al obtener los videos de la playlist. Probablemente la playlist sea privada");
                }
            } else {
                notificaProcesoExtraccion("No se pudo obtener el nombre de la playlist");
            }

        } else {
            id = youtube.getYoutubeUrlId(url, Youtube.URL_VIDEO);
            if (crud.existeVideo(id)) {
                notificaProcesoExtraccion("El video ya existe");
            } else {
                notificaProcesoExtraccion("Empieza la extraccion");
                if (almacenaVideo(id) == 0) {
                    notificaProcesoExtraccion("Video añadido");
                } else {
                    notificaProcesoExtraccion("No se pudo añadir el video");
                }
            }

        }
    }

    /**
     * Recibe la id del video para llamar a todas las funciones necesarias para extraer la informacion
     * y guardar el video
     * @param idVideo id del video a guardar
     * @return un entero. 0 --> exito o 1 --> error
     */
    private int almacenaVideo(String idVideo) {
        int resultado; //0 --> exito; 1 --> error
        Map<String, String> itemsInfoVideo = youtube.getVideoInfo(idVideo); //info del video
        if (itemsInfoVideo.get("status") != null && itemsInfoVideo.get("status").equals("ok")) { //verifica si el video es accesible
            Document codigoFuente = youtube.jsoupConnect(idVideo, Youtube.URL_VIDEO); //obtiene el codigo fuente del video de youtube, pero esta ves desde la pagina
            Map<String, String> videoInfoPagina = youtube.getVideoInfoYoutubePage(codigoFuente); //obtiene el nombre del artista, cancioion y thumnail
            String adaptiveFmts = itemsInfoVideo.get("adaptive_fmts"); //obtiene una de las cuantas urls disponibles del video

            if (adaptiveFmts != null) {
                Map<String, String> itemsUrlAudio = youtube.getElementosUrlAudio(adaptiveFmts); //obtiene los elementos que componen la url de audio
                String urlReproductorYoutube = youtube.getUrlReproductorYoutube(idVideo); //obtiene la url del reproductor de youtube
                itemsUrlAudio = youtube.desencriptarFirma(urlReproductorYoutube, itemsUrlAudio); //obtiene los items ya validados de la url de audio
                String urlAudio = youtube.creaUrlAudio(itemsUrlAudio); //forma el url de audio
                String duracion = itemsUrlAudio.get("dur").replace(".", ""); //obtiene la duracion de la cancion
                crud.guardarVideo(videoInfoPagina.get(Youtube.TITULO), videoInfoPagina.get(Youtube.CANCION), videoInfoPagina.get(Youtube.ARTISTA),
                        idVideo, videoInfoPagina.get(Youtube.THUMBNAIL), urlAudio, Integer.parseInt(duracion)); //guarda el video
                resultado = 0;
            } else {
                Log.d(TAG, "ERROR: adaptive_fmts no encontradas");
                resultado = 1;
            }

        } else {
            Log.d(TAG, "ERROR: video privado o eliminado");
            resultado = 1;
        }

        return resultado;
    }

}
