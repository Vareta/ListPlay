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

import com.listplay.listplay.R;
import com.listplay.listplay.adapters.VideoAdapter;
import com.listplay.listplay.classes.CRUD;
import com.listplay.listplay.classes.CustomRecyclerListener;
import com.listplay.listplay.models.Video;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AllVideos.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class AllVideos extends Fragment implements CustomRecyclerListener{
    private OnFragmentInteractionListener mListener;
    private RecyclerView recyclerView;
    private TextView noAllVideos;
    private ProgressBar progressBar;
    private List<Video> allVideosList;
    private boolean existenVideos;
    private VideoAdapter adaptador;
    private CRUD crud;

    public AllVideos() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.fragment_all_videos, container, false);
        recyclerView = (RecyclerView)convertView.findViewById(R.id.allVideosRecyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        noAllVideos = (TextView)convertView.findViewById(R.id.noAllVideos);
        progressBar = (ProgressBar)getActivity().findViewById(R.id.progressBar);
        crud = new CRUD();

        new TraerAllVideos().execute(this);

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

    }

    @Override
    public void customLongClickListener(View v, int position) {

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private class TraerAllVideos extends AsyncTask<CustomRecyclerListener, Void, Void> {

        @Override
        protected Void doInBackground(CustomRecyclerListener... params) {
            allVideosList = crud.getAllVideos();
            if (!allVideosList.isEmpty()) {
                existenVideos = true;
                adaptador = new VideoAdapter(allVideosList);
                adaptador.setClickListener(params[0]);
            } else {
                existenVideos = false;
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            noAllVideos.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void result) {
            if (!existenVideos) {
                noAllVideos.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                recyclerView.setAdapter(adaptador);
            }
            progressBar.setVisibility(View.GONE);
        }

    }
}
