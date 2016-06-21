package com.listplay.listplay.classes;

import android.util.Log;

import com.listplay.listplay.models.Video;
import com.listplay.listplay.models.YoutubePlayer;

import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Vareta on 27-05-2016.
 */
public class Youtube {
    /**
     * Para cuando la url compartida a la aplicacion es de un video solamente
     */
    public static final int URL_VIDEO = 0;
    /**
     * Para cuando la url compartida a la aplicacion es de una playlist
     */
    public static final int URL_PLAYLIST = 1;
    /**
     * Es la url base de un video en youtube, sólo basta agregar el id del video para crear la url completa
     */
    public static final String BASE_URL_YOUTUBE_VIDEO = "https://www.youtube.com/watch?v=";
    /**
     * Es la url base de una playlist de youtube, sólo basta agregar el id de la playlist para crear la url completa
     */
    public static final String BASE_PLAYLIST_URL = "https://www.youtube.com/playlist?list=";
    /**
     * Nombre con el cual se guarda el titulo en el HashMap
     */
    public static final String TITULO = "titulo";
    /**
     * Nombre con el cual se guarda artista en el HashMap
     */
    public static final String ARTISTA = "artista";
    /**
     * Nombre con el cual se guarda cancion en el HashMap
     */
    public static final String CANCION = "cancion";
    /**
     * Nombre con el cual se guarda thumbnail en el HashMap
     */
    public static final String THUMBNAIL = "thumbnail";
    public static final String TAG = "classes.Youtube: ";
    /**
     * Obtiene la informacion que se entrega mediante la llamada a
     * "https://www.youtube.com/get_video_info?&video_id=" + videoId + "&el=detailpage&ps=default&eurl=&gl=US&hl=en"
     * y la devuelve en un diccionario
     *
     * @param videoId es la id del video de youtube
     */
    public Map<String, String> getVideoInfo(String videoId) {
        OkHttpClient client = new OkHttpClient();
        Map<String, String> items = new HashMap<>();
        Request request = new Request.Builder()
                .url("https://www.youtube.com/get_video_info?&video_id=" + videoId + "&el=detailpage&ps=default&eurl=&gl=US&hl=en")
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Codigo inesperado: " + response);
            String[] contenido = response.body().string().split("&");
            String[] aux;
            for (String item : contenido) {
                aux = item.split("=");
                if (aux.length == 2) {
                    items.put(aux[0], aux[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, String.valueOf(e));
        }

        return items;
    }

    /**
     * Recibe el string que contiene las distintas url's y se limitara a escoger la que
     * tenga el itag=140, que es la que contiene sólo la pista de audio, para luego seleccionar los elementos validos de esta
     * formando asi un diccionario con los elementos que debe poseer la url de audio
     *
     * @param adaptiveFmts string que contiene varias url's con diferentes formatos para el video en cuestion
     * @return retorna los elementos validos de la url que contiene sólo la pista de audio en forma de un diccionario
     */
    public Map<String, String> getElementosUrlAudio(String adaptiveFmts) {
        Map<String, String> elementos = new HashMap<>();
        try {
            String[] urls = URLDecoder.decode(adaptiveFmts, "UTF-8").split(",");
            for (String url : urls) {
                if (url.contains("itag=140")) {
                    elementos = extraerElementosUrlAudio(url);
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.d(TAG, String.valueOf(e));
        }

        return elementos;
    }


    /**
     * Recibe la url que contiene sólo el audio del video y entrega los elementos necesario para formar
     * la url en forma de diccionario
     *
     * @param urlAudioDesordenada contiene los parametros que forman la url de audio
     * @return retorna un diccionario que contiene todos los elementos de la url
     */
    private Map<String, String> extraerElementosUrlAudio(String urlAudioDesordenada) {
        Map<String, String> elementos = new HashMap<>();
        try {
            String urlAux = URLDecoder.decode(urlAudioDesordenada, "UTF-8").replace(";", "&"); //reemplaza el ; por un & para realizar el split
            String[] auxElementos = urlAux.split("&");
            String[] aux;
            for (String elemento : auxElementos) {
                aux = elemento.split("=");
                if (aux.length != 2) {
                    if (aux.length > 2) { //si length es 3, es decir, contiene el elemento url y otro mas (indefinido)
                        // ejemplo: url https://r7---sn-8ug-njae.googlevideo.com/videoplayback?mime audio%2Fmp4
                        //mime es otro elemento, pero esta contenido en url dado que es un ? el que lo separa y no un &
                        String[] urlSplit = aux[1].split("\\?"); //separa el elemento de la url
                        elementos.put(aux[0].trim(), urlSplit[0].trim()); //añade el elemento url
                        elementos.put(urlSplit[1].trim(), aux[2].trim()); //añade el elemento contiguo que estaba separado por un signo ?
                    }
                } else {
                    elementos.put(aux[0].trim(), aux[1].trim());
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.d(TAG, String.valueOf(e));
        }

        elementos = elementosValidosUrl(elementos); //elimna los elementos innecesarios

        return elementos;
    }

    /**
     * Obtiene todos los elementos que debe contener la url y los une para retornar un solo string, el
     * cual será la url del audio del video
     *
     * @param elementos Map que contiene los elementos que debe tener la url
     * @return url del audio del video
     */
    public String creaUrlAudio(Map<String, String> elementos) {
        StringBuilder url = new StringBuilder();
        boolean esPrimerElemento = true; //para verificar el primer elemento añadido luego de url;

        url.append(elementos.get("url")).append("?"); //añade url como primer elemento y el signo ?
        elementos.remove("url"); //remueve la url

        for (Map.Entry<String, String> entry : elementos.entrySet()) {
            if (esPrimerElemento) {
                esPrimerElemento = false;
                url.append(entry.getKey()).append("=").append(entry.getValue());
            } else {
                url.append("&").append(entry.getKey()).append("=").append(entry.getValue());
            }
        }

        return url.toString();
    }

    /**
     * Recibe un diccionario con todos los elementos extraidos de la url, para luego seleccionar sólo
     * los elementos validos para la formación de la url
     *
     * @param elementos Map de los elementos extraidos del string que contenia los elementos de la url
     * @return los elementos validos que conformaran la url
     */
    private Map<String, String> elementosValidosUrl(Map<String, String> elementos) {
        boolean contieneRatebypass = false;
        Map<String, String> resultado = new HashMap<>();

        for (Map.Entry<String, String> entry : elementos.entrySet()) {
            if (esLlaveValida(entry.getKey())) { //añade sólo las llaves validas
                resultado.put(entry.getKey(), entry.getValue());
            }
            if (entry.getKey().equals("ratebypass")) { //consulta por el elemento ratebypass
                contieneRatebypass = true;
            }
        }

        if (!contieneRatebypass) { //añade ratebypass de no tenerlo, ya que es necesario
            resultado.put("ratebypass", "yes");
        }

        return resultado;
    }

    /**
     * Verifica si un elemento pertenece al grupo de elementos que no debe ir en la url final
     *
     * @param llave nombre del elemento
     * @return booleano
     */
    private boolean esLlaveValida(String llave) {
        switch (llave) {
            case "bibrate":
                return false;
            case "init":
                return false;
            case "index":
                return false;
            case "xtags":
                return false;
            case "projection_type":
                return false;
            case "type":
                return false;
            case "codecs":
                return false;
            default:
                return true;
        }
    }

    /**
     * Verifica si se debe o no desencriptar la firma y de resultar verdadero, lo hace.
     *
     * @param urlReproductor url del reproductor de youtube, que contiene la funcion para desencriptar
     *                       la firma
     * @param elementos      elementos que deben ir en la url de audio
     * @return elementos con firma desencriptada (en caso de ser necesario)
     */
    public Map<String, String> desencriptarFirma(String urlReproductor, Map<String, String> elementos) {
        boolean deboDesencriptar = false, existeKeySig = false;
        String signature = null;
        for (Map.Entry<String, String> entry : elementos.entrySet()) {
            if (entry.getKey().equals("sig")) { //en este caso no se necesita desencriptar la firma
                existeKeySig = true;
                break;
            }
            if (entry.getKey().equals("s")) {
                signature = entry.getValue();
                deboDesencriptar = true;
                break;
            }
        }
        if (existeKeySig) {
            String value = elementos.get("sig");
            elementos.remove("sig");
            elementos.put("signature", value);
        }

        if (deboDesencriptar) {
            String signatureDesencriptada;
            String idReproductor = getIdReproductorYoutube(urlReproductor); //obtiene la id del reproductor
            if (idReproductor != null) { //si existe
                CRUD crud = new CRUD();
                List<YoutubePlayer> youtubePlayer = crud.existeReproductorId(idReproductor); //consulta si ya existe la id del reproductor
                if (youtubePlayer != null) { //si existe, desencripta la firma mediante la funcion del reproductor que ya se tenia almacenada
                    signatureDesencriptada = ejecutarJavaScript(youtubePlayer.get(0).getNombreFuncion(),
                            youtubePlayer.get(0).getFuncion(), signature);
                } else { //si no existe, entonces se hace todo el proceso de recolectar la funcion, almacenarla y desencriptar
                    String codigoFuente = getVidePlayerCode(urlReproductor);
                    signatureDesencriptada = funcionDesencriptado(codigoFuente, signature, idReproductor);
                }
                elementos.remove("s");
                elementos.put("signature", signatureDesencriptada);
            } else {
                Log.d(TAG, "ERROR: No se pudo conseguir el id del reproductor");
            }
        }

        return elementos;
    }

    /**
     * Obtiene el codigo fuente del reproductor de video
     *
     * @param urlVideoPlayer url del reproductor de video
     * @return el codigo fuente en forma de string
     */
    private String getVidePlayerCode(String urlVideoPlayer) {
        OkHttpClient client = new OkHttpClient();
        String resultado = null;

        Request request = new Request.Builder()
                .url(urlVideoPlayer)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Codigo inesperado: " + response);
            resultado = response.body().string().replace("\n", ""); //reemplaza los saltos de lineas para un mejor reconocimiento de las funciones de desencriptado
        } catch (IOException e) {
            e.printStackTrace();
        }

        return resultado;
    }

    /**
     * Realiza el proceso de captura de los recursos necesario para desencriptar la firma (nombre de la funcion, la funcion y
     * las subfunciones relacionadas) y se guarda la funcion en su tabla correspondiente
     *
     * @param codigoFuente codigo fuente del reproductor de youtube
     * @param signature    firma a desencriptar
     * @param idReproductor id del reproductor (para aspectos de almacenar informacion del reproductor)
     * @return firma desencriptada
     */
    private String funcionDesencriptado(String codigoFuente, String signature, String idReproductor) {
        String auxNombreFuncion = null, auxFuncionPrincipal = null, auxSubFuncionNombre = null, auxSubFuncion = null;
        String nombreFuncionPrincipal, funcion, signatureDesencriptada;

        Matcher nameMatcher = Pattern.compile("\\.sig\\|\\|([a-zA-Z0-9$]+)\\(").matcher(codigoFuente);//obtiene el nombre de la funcion principal
        while (nameMatcher.find()) {
            auxNombreFuncion = nameMatcher.group(1);
        }
        Matcher functionMatcher = Pattern.compile(",(" + auxNombreFuncion + "=function\\(.\\)+.*?\\}),").matcher(codigoFuente);//obtiene la funcion principal
        while (functionMatcher.find()) {
            auxFuncionPrincipal = functionMatcher.group(1);
        }
        Matcher subFunctionNameMatcher = Pattern.compile(";([A-Za-z0-9]+)\\.").matcher(auxFuncionPrincipal);//obtiene el nombre de la sub funcion dentro de la funcion principal
        while (subFunctionNameMatcher.find()) {
            auxSubFuncionNombre = subFunctionNameMatcher.group(1);
            break;
        }

        Matcher subFunctionMatcher = Pattern.compile("(var " + auxSubFuncionNombre + "=\\{.*?\\});").matcher(codigoFuente);//obtiene la sub funcion
        while (subFunctionMatcher.find()) {
            auxSubFuncion = subFunctionMatcher.group(1);
        }

        nombreFuncionPrincipal = auxNombreFuncion;
        funcion = auxFuncionPrincipal + "\n" + auxSubFuncion;

        signatureDesencriptada = ejecutarJavaScript(nombreFuncionPrincipal, funcion, signature);
        CRUD crud = new CRUD();
        crud.guardarYoutubePlayer(idReproductor, nombreFuncionPrincipal, funcion); //almacena la id del reproductor y su funcion de desencriptado

        return signatureDesencriptada;
    }

    /**
     * Ejecuta una funcion en JavaScript, en este caso, la funcion que desencripta la firma del video de youtube
     *
     * @param nombreFuncion nombre de la funcion a ejecutar
     * @param funcion       corresponde a la funcion y subfunciones relacionadas que se deben ejecutar
     * @param signature     es el parametro de entrada de la funcion a ejecutar, es decir, la firma desencriptar
     * @return devuelve la firma desencriptada
     */
    private String ejecutarJavaScript(String nombreFuncion, String funcion, String signature) {
        String resultado = null;
        Object[] params = new Object[]{signature}; //firma, entra como objeto
        // Every Rhino VM begins with the enter()
        // This Context is not Android's Context
        org.mozilla.javascript.Context rhino = org.mozilla.javascript.Context.enter();

        // Turn off optimization to make Rhino Android compatible
        rhino.setOptimizationLevel(-1);
        try {
            Scriptable scope = rhino.initStandardObjects();

            // Note the forth argument is 1, which means the JavaScript source has
            // been compressed to only one line using something like YUI
            rhino.evaluateString(scope, funcion, "JavaScript", 1, null);

            // Get the functionName defined in JavaScriptCode
            Object obj = scope.get(nombreFuncion, scope);
            if (obj instanceof Function) {
                Function jsFunction = (Function) obj;
                // Call the function with params
                Object jsResult = jsFunction.call(rhino, scope, scope, params);
                // Parse the jsResult object to a String
                resultado = org.mozilla.javascript.Context.toString(jsResult);
            }
        } finally {
            org.mozilla.javascript.Context.exit();
        }

        return resultado;

    }

    /**
     * Entrega el codigo fuente de una pagina web en forma de elementos via jsoup
     *
     * @param idVideo id del video de youtube
     * @param mode si es id de video o playlist
     * @return los elementos de la pagina web
     */
    public Document jsoupConnect(String idVideo, int mode) {
        Document doc = null;
        String url;
        if (mode == URL_VIDEO) {
            url = BASE_URL_YOUTUBE_VIDEO + idVideo;
        } else {
            url = BASE_PLAYLIST_URL + idVideo;
        }
        try {
            doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get();
        } catch (IOException e) {
            Log.d(TAG, String.valueOf(e));
            e.printStackTrace();
        }

        return doc;
    }

    /**
     * Obtiene el titulo del video de youtube y su thumbnail
     *
     * @param codigoFuente codigo fuente del video en youtube
     * @return un Map que contiene los elementos obtenidos
     */
    public Map<String, String> getVideoInfoYoutubePage(Document codigoFuente) {
        Map<String, String> elementos = new HashMap<>();
        String titulo, thumbnail = null;

        titulo = codigoFuente.getElementById("eow-title").text();
        Elements linkItems = codigoFuente.getElementById("watch7-content").select("link");
        for (Element link : linkItems) {
            if (link.attr("itemprop").equals("thumbnailUrl")) {
                thumbnail = link.attr("href");
                break;
            }
        }
        Map<String, String> nombreYcancion = getNombreArtistaYCancion(titulo);
        elementos.put(TITULO, titulo);
        if (nombreYcancion.get(CANCION) != null) {
            elementos.put(CANCION, nombreYcancion.get(CANCION));
            elementos.put(ARTISTA, nombreYcancion.get(ARTISTA));
        }
        elementos.put(THUMBNAIL, thumbnail);
        return elementos;
    }

    /**
     * obtiene la url del reproductor de youtube
     *
     * @param idVideo url del video de youtube
     * @return url del reproductor
     */
    public String getUrlReproductorYoutube(String idVideo) {
        String urlReproductor = null;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(BASE_URL_YOUTUBE_VIDEO + idVideo)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Codigo inesperado: " + response);
            String[] contenido = response.body().string().split("\n");
            for (String item : contenido) {
                if (item.contains("player/base")) {
                    Matcher nameMatcher = Pattern.compile("src=\"(.*?)\"").matcher(item);//obtiene url del reproductor
                    while (nameMatcher.find()) {
                        urlReproductor = "http:" + nameMatcher.group(1);
                    }
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, String.valueOf(e));
        }

        return urlReproductor;
    }

    /**
     * Recibe el titulo del video de youtube y entrega el nombre del artista y el nombre de la cancion.
     *
     * @param titulo string que contiene el nombre del artista y la cancion juntos
     * @return un Map que contiene el nombre del artista y la cancion.
     */
    private Map<String, String> getNombreArtistaYCancion(String titulo) {
        Map<String, String> resultado = new HashMap<>();

        String[] splitGuiones = titulo.split("-");
        if (splitGuiones.length >= 2) {
            resultado.put(ARTISTA, splitGuiones[0]);
            resultado.put(CANCION, splitGuiones[1]);
        } else {
            //resultado.put(ARTISTA, titulo);
        }

        return resultado;
    }

    /**
     * Obtiene el id del video o playlist de youtube, desde la url del video o playlist
     * @param url del video o playlist
     * @param mode valor que corresponde a si es un video o una lista
     * @return el id correspondiente
     */
    public String getYoutubeUrlId(String url, int mode) {
        String id = null;
        if (mode == Youtube.URL_VIDEO) {
            Matcher nameMatcher = Pattern.compile("be\\/(.*)").matcher(url);//obtiene url del reproductor
            while (nameMatcher.find()) {
                id = nameMatcher.group(1);
            }

        } else if (mode == Youtube.URL_PLAYLIST) {
            Matcher nameMatcher = Pattern.compile("list=(.*)").matcher(url);//obtiene url del reproductor
            while (nameMatcher.find()) {
                id = nameMatcher.group(1);
            }
            } else {
                Log.d(TAG, "ERROR: Modo no reconocido en getYoutubeUrlId()");
            }

        return id;
    }

    /**
     * Entrega la id del reproductor de youtube que esta ocupando el video en ese momento
     * @param url url del reproductor de youtube, Ej: https://s.ytimg.com/yts/jsbin/player-en_US-vflrmwhUy/base.js
     * @return el id del reproudctor de youtube, Ej: en_US-vflrmwhUy
     */
    private String getIdReproductorYoutube(String url) {
        String id = null;
        Matcher nameMatcher = Pattern.compile("player-(.*)\\/").matcher(url);//obtiene id del reproductor
        while (nameMatcher.find()) {
            id = nameMatcher.group(1);
        }

        return id;
    }

    /**
     * Obtiene los id de los videos contenidos en una playlist, ante cualquier error de no encontrar las id
     * retorna nulo
     * @param codigoFuente de la playlist
     * @return id de los videos en una lista, si es que resulta exitoso o nulo, si es que ocurre algun error
     */
    public List<String> getIdVideosPlaylist(Document codigoFuente) {
        List<String> resultado = new ArrayList<>();
        Element urlsElement = codigoFuente.getElementById("pl-load-more-destination");
        if (urlsElement != null) {
            Elements urls = urlsElement.select("tr");
            String idVideo;
            for (Element url : urls) {
                idVideo = url.attr("data-video-id");
                resultado.add(idVideo);
            }
        } else {
            resultado = null;
            Log.d(TAG, "ERROR: Error en conseguir los elementos de la playlist, lo mas probable es que la lista sea privada");
        }
        return resultado;
    }

    /**
     * Obtiene el nombre de la playlist que se quiere ingresar
     * @param codigoFuente de la playlist en youtube
     * @return el nombre de la playlist, si lo encuentra, o nulo en caso contrario
     */
    public String getNombreYoutubePlaylist(Document codigoFuente) {
        String nombre = null;
        Elements metas = codigoFuente.head().select("meta");
        for (Element meta : metas) {
            if (meta.attr("name").equals("title")) {
                nombre = meta.attr("content");
            }
        }
        return nombre;
    }

    public String getUrlAudioActualizado(long idVideo) {
        String url = "";
        CRUD crud = new CRUD();
        Video video = crud.getVideo(idVideo);
        Map<String, String> itemsInfoVideo = getVideoInfo(video.getIdVideoYoutube()); //info del video
        String adaptiveFmts = itemsInfoVideo.get("adaptive_fmts");
        Map<String, String> itemsUrlAudio = getElementosUrlAudio(adaptiveFmts);
        String urlReproductorYoutube = getUrlReproductorYoutube(video.getIdVideoYoutube());
        itemsUrlAudio = desencriptarFirma(urlReproductorYoutube, itemsUrlAudio);
        url = creaUrlAudio(itemsUrlAudio);

        video.setUrlVideoAudio(url);
        video.setTiempoLimite(new DateTime().getMillis());
        video.save();

        return url;
    }
}
