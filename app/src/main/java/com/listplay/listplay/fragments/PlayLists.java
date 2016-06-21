package com.listplay.listplay.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.listplay.listplay.adapters.PlayListAdapter;
import com.listplay.listplay.classes.CRUD;
import com.listplay.listplay.classes.CustomRecyclerListener;
import com.listplay.listplay.models.PlayList;
import com.listplay.listplay.R;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PlayLists.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class PlayLists extends Fragment implements CustomRecyclerListener{
    private OnFragmentInteractionListener mListener;
    private RecyclerView recyclerView;
    private TextView noPlayLists;
    private ProgressBar progressBar;
    private List<PlayList> playLists;
    private List<Integer> numVideosPorPlayList;
    private boolean existenPlayLists;
    private PlayListAdapter adaptador;
    private CRUD crud;
    private FloatingActionButton fab;

    public PlayLists() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View convertView =  inflater.inflate(R.layout.fragment_play_lists, container, false);
        recyclerView = (RecyclerView)convertView.findViewById(R.id.playListRecyclerview);
        noPlayLists = (TextView)convertView.findViewById(R.id.noPlayLists);
        progressBar = (ProgressBar)getActivity().findViewById(R.id.progressBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        fab = (FloatingActionButton)convertView.findViewById(R.id.fab);
        crud = new CRUD();
        new TraerPlayLists().execute(this);
        return convertView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void customClickListener(View v, int position) {
        mListener.onPlayListClick(playLists.get(position).getId());
    }

    @Override
    public void customLongClickListener(View v, int position) {

    }

    public interface OnFragmentInteractionListener {
        void onPlayListClick(long playListId);
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
            noPlayLists.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void result) {
            if (!existenPlayLists) {
                noPlayLists.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                recyclerView.setAdapter(adaptador);
            }
            progressBar.setVisibility(View.GONE);
        }

    }
}
