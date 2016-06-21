package com.listplay.listplay.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.listplay.listplay.adapters.VideoAdapter;
import com.listplay.listplay.classes.CRUD;
import com.listplay.listplay.classes.CustomRecyclerListener;
import com.listplay.listplay.models.Video;
import com.listplay.listplay.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Videos.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class Videos extends Fragment implements CustomRecyclerListener{
    private List<Video> videos;
    private RecyclerView recyclerView;
    private TextView noVideos;
    private ProgressBar progressBar;
    private boolean existenVideos;
    private VideoAdapter adaptador;
    private OnFragmentInteractionListener mListener;
    private long playListId;
    private CRUD crud;

    public Videos() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View convertView = inflater.inflate(R.layout.fragment_videos, container, false);
        recyclerView = (RecyclerView) convertView.findViewById(R.id.videosRecyclerview);
        progressBar = (ProgressBar)getActivity().findViewById(R.id.progressBar);
        noVideos = (TextView) convertView.findViewById(R.id.noVideos);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        crud = new CRUD();
        if (videos == null) {
            videos = new ArrayList<>();
        }
        playListId = getArguments().getLong("playListId");

        new TraerVideosPlaylist().execute(this);
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
        mListener.onVideosClick(playListId, position);
    }

    @Override
    public void customLongClickListener(View v, int position) {

    }

    public interface OnFragmentInteractionListener {
        void onVideosClick(long playListId, int posVideoAReproducir);
    }

    private class TraerVideosPlaylist extends AsyncTask<CustomRecyclerListener, Void, Void> {

        @Override
        protected Void doInBackground(CustomRecyclerListener... params) {
            videos = crud.getVideosPlaylist(playListId);
            if (!videos.isEmpty()) {
                existenVideos = true;
                adaptador = new VideoAdapter(videos);
                adaptador.setClickListener(params[0]);
            } else {
                existenVideos = false;
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            noVideos.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void result) {
            if (!existenVideos) {
                noVideos.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                recyclerView.setAdapter(adaptador);
            }
            progressBar.setVisibility(View.GONE);
        }

    }
}
