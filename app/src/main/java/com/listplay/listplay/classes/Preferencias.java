package com.listplay.listplay.classes;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Vareta on 24-06-2016.
 */
public class Preferencias {
    public static final String REPRODUCTOR_PREF = "reproductorpref";
    public static final String SHUFFLE = "shuffle";
    public static final String REPEAT = "repeat";
    public static final String REPEAT_ONCE = "repeatonce";
    public static final String SLEEP_TIMER = "sleeptimer";


    public void iniPreferencias(Context context) {
        SharedPreferences sp = context.getSharedPreferences(REPRODUCTOR_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(SHUFFLE, false);
        editor.putBoolean(REPEAT, false);
        editor.putBoolean(REPEAT_ONCE, false);
        editor.putInt(SLEEP_TIMER, 0); //0 = desactivado
        editor.apply();
    }

    public boolean existePreferencia(Context context) {
        SharedPreferences sp = context.getSharedPreferences(REPRODUCTOR_PREF, Context.MODE_PRIVATE);
        return sp.contains(SHUFFLE);
    }

    public void setShuffle(Context context, boolean valor) {
        SharedPreferences sp = context.getSharedPreferences(REPRODUCTOR_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(SHUFFLE, valor);
        editor.apply();
    }

    public void setRepeat(Context context, boolean valor) {
        SharedPreferences sp = context.getSharedPreferences(REPRODUCTOR_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(REPEAT, valor);
        if (valor) {
            editor.putBoolean(REPEAT_ONCE, false);
        }
        editor.apply();
    }

    public void setRepeatOnce(Context context, boolean valor) {
        SharedPreferences sp = context.getSharedPreferences(REPRODUCTOR_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(REPEAT_ONCE, valor);
        if (valor) {
            editor.putBoolean(REPEAT, false);
        }
        editor.apply();
    }

    public boolean isShuffle(Context context) {
        SharedPreferences sp = context.getSharedPreferences(REPRODUCTOR_PREF, Context.MODE_PRIVATE);

        return sp.getBoolean(SHUFFLE, false);
    }

    public boolean isRepeat(Context context) {
        SharedPreferences sp = context.getSharedPreferences(REPRODUCTOR_PREF, Context.MODE_PRIVATE);

        return sp.getBoolean(REPEAT, false);
    }

    public boolean isRepeatOnce(Context context) {
        SharedPreferences sp = context.getSharedPreferences(REPRODUCTOR_PREF, Context.MODE_PRIVATE);

        return sp.getBoolean(REPEAT_ONCE, false);
    }

    public int getSleepTimer(Context context) {
        SharedPreferences sp = context.getSharedPreferences(REPRODUCTOR_PREF, Context.MODE_PRIVATE);
        return sp.getInt(SLEEP_TIMER, 0);
    }

    public void setSleepTimer(Context context, int tiempo) {
        SharedPreferences sp = context.getSharedPreferences(REPRODUCTOR_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(SLEEP_TIMER, tiempo);
        editor.apply();
    }
}