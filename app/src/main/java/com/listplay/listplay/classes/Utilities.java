package com.listplay.listplay.classes;

import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;

import com.listplay.listplay.R;

/**
 * Created by Vareta on 16-06-2016.
 */
public class Utilities {
    private static final String TAG = "Utilities";
    public static final String FRAGMENT_ALL_VIDEOS = "allvideos";
    public static final String FRAGMENT_PLAY_LISTS = "playlists";
    public static final String FRAGMENT_RECIENTEMENTE_AGREGADAS = "recientementeagregadas";
    public static final String FRAGMENT_VIDEOS = "videos";
    public static final String FRAGMENT_ARTISTAS = "artistas";


    /**
     * Cambia el color de un drawable (vector)
     * @param resources
     * @param drawable icono a cambiar de color
     * @param color a cambiar
     * @param theme tema
     */
    public void cambiaColorDrawable(Resources resources, int drawable, int color, Theme theme) {
        Drawable normalDrawable = ResourcesCompat.getDrawable(resources, drawable, theme);
        if (normalDrawable != null) {
            Drawable wrapDrawable = DrawableCompat.wrap(normalDrawable);
            DrawableCompat.setTint(wrapDrawable, ResourcesCompat.getColor(resources, color, theme));
        } else {
            Log.d(TAG, "ERROR: Drawable nulo");
        }
    }
}
