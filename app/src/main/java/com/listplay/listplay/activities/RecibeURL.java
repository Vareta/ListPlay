package com.listplay.listplay.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.listplay.listplay.classes.URLService;

import org.litepal.LitePalApplication;

public class RecibeURL extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String url = intent.getStringExtra(Intent.EXTRA_TEXT); //obtiene el url compartido hacia la aplicacion
        Intent serviceIntent = new Intent(this, URLService.class); //crea un nuevo intent para el service URLService
        serviceIntent.putExtra("url", url); //añade el url compartido
        startService(serviceIntent); // inicia el service
        finish(); //finaliza la actividad

    }
}
