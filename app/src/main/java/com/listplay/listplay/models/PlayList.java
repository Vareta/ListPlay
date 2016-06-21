package com.listplay.listplay.models;

import org.joda.time.DateTime;
import org.litepal.crud.DataSupport;

import java.util.Date;

/**
 * Created by Vareta on 27-05-2016.
 */
public class PlayList extends DataSupport {
    private long id;
    private String nombre;
    private DateTime fechaCreacion;

    public void setId(long id) {
        this.id = id;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public void setFechaCreacion(DateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public long getId() {
        return id;
    }
    public String getNombre() {
        return nombre;
    }
    public DateTime getFechaCreacion() {
        return fechaCreacion;
    }
}
