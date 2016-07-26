package com.listplay.listplay.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer.util.Util;
import com.listplay.listplay.R;
import com.listplay.listplay.adapters.PlayListAdapter;
import com.listplay.listplay.classes.CRUD;
import com.listplay.listplay.classes.CustomRecyclerListener;
import com.listplay.listplay.classes.Utilities;
import com.listplay.listplay.models.PlayList;

import java.util.List;

/**
 * Created by Vareta on 21-07-2016.
 */
public class AddToPlayListDialog extends DialogFragment implements CustomRecyclerListener {
    private TextView nuevaPlayList;
    private TextView cancelar;
    private RecyclerView recyclerView;
    private List<PlayList> playLists;
    private List<Integer> numVideosPorPlayList;
    private PlayListAdapter adaptador;
    private boolean existenPlayLists;
    private long idVideo;
    private CRUD crud;

    public AddToPlayListDialog() {
        //Empty constructor
    }

    public static AddToPlayListDialog newInstance(long idVideo) {
        AddToPlayListDialog addFrag = new AddToPlayListDialog();
        Bundle variables = new Bundle();
        variables.putLong("idvideo", idVideo);
        addFrag.setArguments(variables);
        addFrag.setStyle(STYLE_NORMAL, R.style.AppDialogTheme);
        return addFrag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_to_playlist_dialog, container);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setElementos(view);
        setListeners();
    }

    private void setElementos(View view) {
        crud = new CRUD();
        idVideo = getArguments().getLong("idvideo");
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_playlist_dialog);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        nuevaPlayList = (TextView) view.findViewById(R.id.nueva_playlist);
        cancelar = (TextView) view.findViewById(R.id.cancelar);
        getDialog().setTitle(getString(R.string.titulo_addto));
        new TraerPlayLists().execute(this);
    }

    private void setListeners() {
        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        nuevaPlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                NewPlayListDialog newPlayListDialog = NewPlayListDialog.newInstance(idVideo);
                newPlayListDialog.show(fm, "new_playlist_dialog_fragment");
            }
        });
    }

    @Override
    public void customClickListener(View v, int position) {
        Utilities util = new Utilities();
        if (crud.añadirVideoAPlaylist(idVideo, playLists.get(position).getId())) { //si el video es añadido con exito
            util.mensajeCorto(getContext(), getString(R.string.exito_video_addto));
            getDialog().dismiss();
        } else { //si el video ya existe
            util.mensajeCorto(getContext(), getString(R.string.video_repetido_addto));
        }
    }

    @Override
    public void customLongClickListener(View v, int position) {
        //no aplica
    }

    private class TraerPlayLists extends AsyncTask<CustomRecyclerListener, Void, Void> {

        @Override
        protected Void doInBackground(CustomRecyclerListener... params) {
            playLists = crud.getAllPlayList();
            if (!playLists.isEmpty()) {
                existenPlayLists = true;
                numVideosPorPlayList = crud.getNumVideosPorPlayList(playLists);
                adaptador = new PlayListAdapter(getContext(), playLists, numVideosPorPlayList);
                adaptador.setClickListener(params[0]);
            } else {
                existenPlayLists = false;
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Void result) {
            if (!existenPlayLists) {
                recyclerView.setVisibility(View.GONE);
            } else {
                recyclerView.setAdapter(adaptador);
            }
        }

    }

}
