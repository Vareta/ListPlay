package com.listplay.listplay.models;

import android.os.Parcelable;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * Created by Vareta on 27-05-2016.
 */
public class Video extends DataSupport {
    private long id;
    private String titulo;
    private String cancion;
    private String artista;
    private String idVideoYoutube;
    private String urlImagen;
    private String urlVideoAudio;
    private int duracion;
    private int numReproducciones;
    private long tiempoLimite;
    private long fechaCreacion;

    public void setId(long id) {
        this.id = id;
    }
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    public void setCancion(String cancion) {
        this.cancion = cancion;
    }
    public void setArtista(String artista) {
        this.artista = artista;
    }
    public void setIdVideoYoutube(String idVideoYoutube) {
        this.idVideoYoutube = idVideoYoutube;
    }
    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }
    public void setUrlVideoAudio(String urlVideoAudio) {
        this.urlVideoAudio = urlVideoAudio;
    }
    public void setDuracion(int duracion) {
        this.duracion = duracion;
    }
    public void setNumReproducciones(int numReproducciones) {
        this.numReproducciones = numReproducciones;
    }
    public void setTiempoLimite(long tiempoLimite) {
        this.tiempoLimite = tiempoLimite;
    }
    public void setFechaCreacion(long fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public long getId() {
        return id;
    }
    public String getTitulo() {
        return titulo;
    }
    public String getCancion() {
        return cancion;
    }
    public String getArtista() {
        return artista;
    }
    public String getIdVideoYoutube() {
        return idVideoYoutube;
    }
    public String getUrlImagen() {
        return urlImagen;
    }
    public String getUrlVideoAudio() {
        return urlVideoAudio;
    }
    public int getDuracion() {
        return duracion;
    }
    public int getNumReproducciones() {
        return numReproducciones;
    }
    public long getTiempoLimite() {
        return tiempoLimite;
    }
    public long getFechaCreacion() {
        return fechaCreacion;
    }
}
