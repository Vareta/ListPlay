package com.listplay.listplay.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
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

    /**
     * Obtiene los videos de la playlist de manera asincrona para luego adjuntarlas al adaptador
     * y mostrarlas en la vista
     */
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
                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallBack);
                itemTouchHelper.attachToRecyclerView(recyclerView); //añade la lista a la escucha
            }
            progressBar.setVisibility(View.GONE);
        }

    }

    ItemTouchHelper.SimpleCallback itemTouchCallBack = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            adaptador.swap(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            crud.swapVideosEnPlaylist(playListId, videos.get(viewHolder.getAdapterPosition()).getId(), videos.get(target.getAdapterPosition()).getId());
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            adaptador.remove(viewHolder.getAdapterPosition());
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                View itemView = viewHolder.itemView;

                Paint paint = new Paint();
                Bitmap bitmap;

                if (dX < 0) {// swiping left
                    paint.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));

                    bitmap = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.ic_delete_white_36dp);
                    float height = (itemView.getHeight() / 2) - (bitmap.getHeight() / 2);
                    float bitmapWidth = bitmap.getWidth();

                    c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom(), paint);
                    c.drawBitmap(bitmap, ((float) itemView.getRight() - bitmapWidth) - 96f, (float) itemView.getTop() + height, null);

                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }
    };
}
