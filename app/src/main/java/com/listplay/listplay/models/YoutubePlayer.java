package com.listplay.listplay.models;

import org.litepal.crud.DataSupport;

/**
 * Created by Vareta on 06-06-2016.
 */
public class YoutubePlayer extends DataSupport {
    private long id;
    private String idYoutubePlayer;
    private String nombreFuncion;
    private String funcion;
    private long fechaEnMilisegundos;

    public void setId(long id) {
        this.id = id;
    }
    public void setIdYoutubePlayer(String idYoutubePlayer) {
        this.idYoutubePlayer = idYoutubePlayer;
    }
    public void setNombreFuncion(String nombreFuncion) {
        this.nombreFuncion = nombreFuncion;
    }
    public void setFuncion(String funcion) {
        this.funcion = funcion;
    }
    public void setFechaEnMilisegundos(long fechaEnMilisegundos) {
        this.fechaEnMilisegundos = fechaEnMilisegundos;
    }

    public long getId() {
        return id;
    }
    public String getIdYoutubePlayer() {
        return idYoutubePlayer;
    }
    public String getNombreFuncion() {
        return nombreFuncion;
    }
    public String getFuncion() {
        return funcion;
    }
    public long getFechaEnMilisegundos() {
        return fechaEnMilisegundos;
    }
}
