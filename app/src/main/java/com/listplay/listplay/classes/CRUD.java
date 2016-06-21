package com.listplay.listplay.classes;

import android.os.AsyncTask;
import android.util.Log;

import com.listplay.listplay.models.PlayList;
import com.listplay.listplay.models.PlayListsConVideos;
import com.listplay.listplay.models.Video;
import com.listplay.listplay.models.YoutubePlayer;

import org.joda.time.DateTime;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vareta on 07-06-2016.
 */
public class CRUD {
    private static final int SEMANA_EN_MILI_SEC = 604800000;
    /***************REPRODUCTOR*****************/

    /**
     * Consulta si acaso el reproductor de youtube se encuentra registrado dentro de la tabla
     * @param reproductorId Id del reproductor a consultar
     * @return null en caso de no existir o el reproductor en caso de existir
     */
    public List<YoutubePlayer> existeReproductorId(String reproductorId) {
        List<YoutubePlayer> youtubePlayer = DataSupport.where("idYoutubePlayer =?", reproductorId).find(YoutubePlayer.class);
        if (youtubePlayer == null || youtubePlayer.isEmpty()) {
            return null;
        } else {
            return youtubePlayer;
        }
    }

    /**
     * Guarda el reproductor de youtube en la tabla
     * @param idYoutubePlayer id del reproductor de youtube
     * @param nombreFuncion nombre de la funcion desencriptadora
     * @param funcion funcion para desencriptar la firma
     */
    public void guardarYoutubePlayer(String idYoutubePlayer, String nombreFuncion, String funcion) {
        YoutubePlayer yp = new YoutubePlayer();
        yp.setIdYoutubePlayer(idYoutubePlayer);
        yp.setNombreFuncion(nombreFuncion);
        yp.setFuncion(funcion);
        yp.setFechaEnMilisegundos(new DateTime().getMillis());
        yp.save();
    }

    /*********VIDEO************/

    /**
     * Consulta si acaso el video existe o no
     * @param idVideoYoutube id del video a consultar
     * @return cualquiera sea el caso
     */
    public boolean existeVideo(String idVideoYoutube) {
        List<Video> youtubePlayer = DataSupport.where("idVideoYoutube =?", idVideoYoutube).find(Video.class);
        if (youtubePlayer == null || youtubePlayer.isEmpty()) {
            return false;
        } else {
            return true;

        }
    }

    /**
     * Retorna la id (en la tabla) del video de youtube.
     * Aqui los datos llegan comprobados, por lo que siempre existe el video
     * @param idVideoYoutube id del video
     * @return id del video
     */
    private long getVideoId(String idVideoYoutube) {
        List<Video> youtubePlayer = DataSupport.where("idVideoYoutube =?", idVideoYoutube).find(Video.class);
        return youtubePlayer.get(0).getId();
    }

    /**
     * Guarda el video en la tabla
     * @param cancion Nombre de la cancion (si es que la logra obtener)
     * @param artista nombre del artista (de no obtener el nombre de la cancion, en este campo va el titulo del video)
     * @param idVideoYoutube id del video
     * @param urlImagen url de la imagen thumbnail del video
     * @param urlVideoAudio url de el audio del video
     * @param duracion duracion total del video
     */
    public void guardarVideo(String titulo, String cancion, String artista, String idVideoYoutube,
                               String urlImagen, String urlVideoAudio, int duracion) {
        Video video = new Video();
        video.setTitulo(titulo);
        if (cancion != null) {
            video.setCancion(cancion);
            video.setArtista(artista);
        }
        video.setIdVideoYoutube(idVideoYoutube);
        video.setUrlImagen(urlImagen);
        video.setUrlVideoAudio(urlVideoAudio);
        video.setDuracion(duracion);
        video.setNumReproducciones(0); //default
        video.setTiempoLimite(new DateTime().getMillis());
        video.setFechaCreacion(new DateTime().getMillis());
        video.save();
    }

    /**
     * Retorna el video segun la id de este. En este punto siempre llega el video validado, es decir, nunca
     * debiese no encontrar el video
     * @param idVideo id del video a obtener
     * @return video obtenido
     */
    public Video getVideo(long idVideo) {
        Video video;
        video = DataSupport.find(Video.class, idVideo);
        return video;
    }

    /**
     * Obtiene los videos que se añadieron hasta hace una semana. Si los videos recientes no son suficientes,
     * entonces toma videos mas antiguos.
     * Sólo entregara menos de 20 videos recientes cuando no existan mas de 20 video en total en toda la
     * coleccion de videos
     * @return videos recientes
     */
    public List<Video> getVideosAgregadosRecientemente() {
        List<Video> allVideos;
        List<Video> recientes = new ArrayList<>();

        allVideos = DataSupport.findAll(Video.class); //obtiene los videos
        if (allVideos == null || allVideos.isEmpty()) { //si no existen
            return null;
        } else {
            long tiempoDelta;
            for (Video video : allVideos) {
                tiempoDelta = (new DateTime().getMillis()) - video.getFechaCreacion(); //obtiene la diferencia de tiempo
                if (tiempoDelta < SEMANA_EN_MILI_SEC) { //si es menor a una semana lo agrega
                    recientes.add(video);
                }
            }

            if (recientes.isEmpty() || recientes.size() < 21) { //si la lista de recientes no contiene suficientes videos
                if (allVideos.size() < 21) {// en caso de ser menos de 20 videos en total, los añade todos como recientes
                    recientes = allVideos ;
                } else { // caso contrario, añade hasta completar un total de 20 videos
                    int inicio = allVideos.size() - recientes.size();
                    int hasta = inicio - (20 - recientes.size());
                    for (int i = inicio - 1; i >= hasta; i--) {
                        recientes.add(allVideos.get(i));
                    }
                }
            }
            return recientes;
        }
    }


    /*************PLAYLIST****************/

    /**
     * Comprueba si la playlist ya existe
     * @param nombre de la playlist
     * @return verdadero o falso, segun sea el caso
     */
    public boolean existePlaylist(String nombre) {
        List<PlayList> playList = DataSupport.where("nombre =?", nombre).find(PlayList.class);
        if (playList == null || playList.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Añade la playlist a la tabla
     * @param nombre de la playlist
     */
    public void añadirPlayList(String nombre) {
        PlayList playList = new PlayList();
        playList.setNombre(nombre);
        playList.setFechaCreacion(new DateTime());
        playList.save();
    }

    /**
     * Obtiene todas las playlists existentes
     * @return las playlists encontradas
     */
    public List<PlayList> getAllPlayList() {
        List<PlayList> playlists;
        playlists = DataSupport.findAll(PlayList.class);

        return playlists;
    }

    /******PLAYLISTSCONVIDEOS*********/

    /**
     * Funcion para cuando se requiere añadir mas de un video a una playlist
     * @param videosId Lista con los id (id obtenida desde la url) de los videos a añadir
     * @param nombrePlayList nombre de la playlist a la cual se añadiran los videos
     */
    public void añadirMuchosVideosAPlaylist(List<String> videosId, String nombrePlayList) {
        List<PlayList> playList = DataSupport.where("nombre =?", nombrePlayList).find(PlayList.class); //obtiene la playlist
        long idPlaylist = playList.get(0).getId(); //obtiene la id de la playlist

        //obtiene los videos, si existen, de la playlist
        List<PlayListsConVideos> playListsConVideos = DataSupport.where("playListId =?", String.valueOf(idPlaylist)).find(PlayListsConVideos.class);
        if (playListsConVideos == null || playListsConVideos.isEmpty()) { //si no existen
            long idVideo;
            for (int i = 0; i < videosId.size(); i++) { //los añade todos
                idVideo = getVideoId(videosId.get(i));
                guardarVideoAPlayList(idPlaylist, idVideo, i);
            }
        } else { //si ya contiene videos
            int posicioInicial = playListsConVideos.get(playListsConVideos.size() - 1).getPosicion() + 1; //obtiene la posicion que debera llevar el primer elemento a añadir
            for (int j = 0; j < videosId.size(); j++) { //recorre la lista de vides a añadir
                long idVideoAAñadir = getVideoId(videosId.get(j)); //obtiene el id (en la tabla) del video a añadir
                if (!existeVideoEnPlayListConVideos(idVideoAAñadir, playListsConVideos)) { //si el video no existe en la playlist
                    guardarVideoAPlayList(idPlaylist, idVideoAAñadir, posicioInicial); //se añade el video
                    posicioInicial += 1; //se incrementa la posicion para el siguiente video
                }
            }
        }

    }

    /**
     * Guarda un video en la playlist asignada
     * @param idPlayList id (tabla) de la playlist
     * @param idVideo id (tabla) del video
     * @param posicion posicion en el cual quedara el video
     */
    private void guardarVideoAPlayList(long idPlayList, long idVideo, int posicion) {
        PlayListsConVideos pv = new PlayListsConVideos();
        pv.setPlayListId(idPlayList);
        pv.setVideoId(idVideo);
        pv.setPosicion(posicion);
        pv.save();
    }

    /**
     * Consulta si acaso existe el video que se quiere añadir en una playlist determinada
     * @param idVideoAAñadir id (tabla) del video a añadir
     * @param videosLista Todos los videos que corresponden a la playlist
     * @return verdadero o falso, segun sea el caso
     */
    private boolean existeVideoEnPlayListConVideos(long idVideoAAñadir, List<PlayListsConVideos> videosLista) {
        for (PlayListsConVideos video : videosLista) {
            if (idVideoAAñadir == video.getVideoId()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Obtiene el numero de videos segun la playlist dada
     * @param idPlayList id de la playlist a consultar
     * @return numero de videos
     */
    public int getNumVideosPlayList(long idPlayList) {
        List<PlayListsConVideos> videosEnPlayList = DataSupport.where("playListId =?", String.valueOf(idPlayList)).find(PlayListsConVideos.class);

        if (videosEnPlayList == null || videosEnPlayList.isEmpty()) {
            return 0;
        } else {
            return videosEnPlayList.size();
        }
    }

    /**
     * Obtiene el numero de videos segun una lista de playlists
     * @param playLists lista de playlist a consultar
     * @return Lista con el numero de videos por playlists
     */
    public List<Integer> getNumVideosPorPlayList(List<PlayList> playLists) {
        List<Integer> numVideosPorPlayList = new ArrayList<>();
        int numVideos;
        for (PlayList playList : playLists) {
            numVideos = getNumVideosPlayList(playList.getId());
            numVideosPorPlayList.add(numVideos);
        }

        return numVideosPorPlayList;
    }

    /**
     * Obtiene todos los videos contenidos en una playlist
     * @param idPlaylist id de la playlist
     * @return Lista de los videos obtenidos
     */
    public List<Video> getVideosPlaylist(long idPlaylist) {
        List<Video> resultado = new ArrayList<>();
        List<PlayListsConVideos> videosEnPlaylist = new ArrayList<>();

        videosEnPlaylist = DataSupport.where("playListId =?", String.valueOf(idPlaylist)).find(PlayListsConVideos.class);
        for (int i = 0; i < videosEnPlaylist.size(); i++) {
            resultado.add(getVideo(videosEnPlaylist.get(i).getVideoId()));
        }

        return resultado;
    }

}
