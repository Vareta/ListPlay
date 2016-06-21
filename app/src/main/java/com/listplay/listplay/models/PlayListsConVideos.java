package com.listplay.listplay.models;

import org.litepal.crud.DataSupport;

/**
 * Created by Vareta on 27-05-2016.
 */
public class PlayListsConVideos extends DataSupport {
    private long id;
    private long playListId;
    private long videoId;
    private int posicion;

    public void setId(long id) {
        this.id = id;
    }
    public void setPlayListId(long playListId) {
        this.playListId = playListId;
    }
    public void setVideoId(long videoId) {
        this.videoId = videoId;
    }
    public void setPosicion(int posicion) {
        this.posicion = posicion;
    }

    public long getId() {
        return id;
    }
    public long getPlayListId() {
        return playListId;
    }
    public long getVideoId() {
        return videoId;
    }
    public int getPosicion() {
        return posicion;
    }
}
