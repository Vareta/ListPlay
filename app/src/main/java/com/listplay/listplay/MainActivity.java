package com.listplay.listplay;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.listplay.listplay.activities.Central;
import com.listplay.listplay.classes.Preferencias;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Preferencias preferencias = new Preferencias();
        if (!preferencias.existePreferencia(this)) {
            preferencias.iniPreferencias(this);
        }
        Intent intent = new Intent(MainActivity.this, Central.class);
        startActivity(intent);
        finish();
    }
}
