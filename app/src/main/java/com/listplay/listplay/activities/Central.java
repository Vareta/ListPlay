package com.listplay.listplay.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.listplay.listplay.classes.Utilities;
import com.listplay.listplay.fragments.AllVideos;
import com.listplay.listplay.fragments.Artistas;
import com.listplay.listplay.fragments.PlayLists;
import com.listplay.listplay.fragments.RecientementeAgregadas;
import com.listplay.listplay.fragments.Videos;
import com.listplay.listplay.R;
import com.listplay.listplay.models.Video;

import java.io.Serializable;
import java.util.List;

public class Central extends AppCompatActivity implements PlayLists.OnFragmentInteractionListener, Videos.OnFragmentInteractionListener,
                                                            AllVideos.OnFragmentInteractionListener, RecientementeAgregadas.OnFragmentInteractionListener,
                                                            Artistas.OnFragmentInteractionListener{
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_central);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawerLayout);
        setNavigationView();
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.contenedor, new PlayLists(), Utilities.FRAGMENT_PLAY_LISTS);
        ft.commit();
    }

    private void setNavigationView() {
        navigationView = (NavigationView)findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

                switch (item.getItemId()) {
                    case R.id.playlist_nav:
                        ft.replace(R.id.contenedor, new PlayLists(), Utilities.FRAGMENT_PLAY_LISTS);
                        break;
                    case R.id.allsongs_nav:
                        ft.replace(R.id.contenedor, new AllVideos(), Utilities.FRAGMENT_ALL_VIDEOS);
                        ft.addToBackStack(Utilities.FRAGMENT_ALL_VIDEOS);
                        break;
                    case R.id.recientes_nav:
                        ft.replace(R.id.contenedor, new RecientementeAgregadas(), Utilities.FRAGMENT_RECIENTEMENTE_AGREGADAS);
                        ft.addToBackStack(Utilities.FRAGMENT_RECIENTEMENTE_AGREGADAS);
                        break;
                    case R.id.artistas_nav:
                        ft.replace(R.id.contenedor, new Artistas(), Utilities.FRAGMENT_ARTISTAS);
                        ft.addToBackStack(Utilities.FRAGMENT_ARTISTAS);
                        break;
                    case R.id.settings_nav:
                        break;
                    default:
                        return true;
                }
                ft.commit();
                drawerLayout.closeDrawers();
                return true;
            }
        });
    }

    @Override
    public void onPlayListClick(long playListId) {
        //Envia la id de la playlist al fragment videos mediante un bundle
        Bundle bundle = new Bundle();
        bundle.putLong("playListId", playListId);
        Videos video = new Videos();
        video.setArguments(bundle);

        //Inicia el fragmente que contiene la id de la playlist
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.contenedor, video, Utilities.FRAGMENT_VIDEOS);
        ft.addToBackStack(Utilities.FRAGMENT_VIDEOS); //para que se pueda devolver a un fragment anterior
        ft.commit();
    }

    @Override
    public void onVideosClick(long playListId, int posVideoAReproducir) {
        Bundle bundle = new Bundle();
        bundle.putInt("posicion", posVideoAReproducir);
        bundle.putLong("playlistid", playListId);
        Intent intent = new Intent(Central.this, Reproductor.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
