package com.listplay.listplay.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.listplay.listplay.classes.CustomRecyclerListener;
import com.listplay.listplay.models.PlayList;
import com.listplay.listplay.R;

import java.util.List;

/**
 * Created by Vareta on 09-06-2016.
 */
public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.PlayListHolder>{
    private List<PlayList> items;
    private List<Integer> videosPlayList;
    private String numVideosTextview;
    private Context context;
    private CustomRecyclerListener mListener;

    public PlayListAdapter(Context context, List<PlayList> items, List<Integer> videosPlayList) {
        this.context = context;
        this.items = items;
        this.videosPlayList = videosPlayList;
    }

    public class PlayListHolder extends RecyclerView.ViewHolder implements OnClickListener, OnLongClickListener {
        private TextView nombre;
        private TextView numVideos;

        public PlayListHolder(View itemView) {
            super(itemView);
            nombre = (TextView)itemView.findViewById(R.id.nombre);
            numVideos = (TextView)itemView.findViewById(R.id.numVideos);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.customClickListener(v, getLayoutPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mListener != null) {
                mListener.customLongClickListener(v, getLayoutPosition());
            }
            return true;
        }
    }

    public void setClickListener(CustomRecyclerListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public PlayListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_element, parent, false);
        PlayListAdapter.PlayListHolder vh = new PlayListHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(PlayListHolder holder, int position) {
        holder.nombre.setText(items.get(position).getNombre());
        numVideosTextview = Integer.toString(videosPlayList.get(position)) + " " + context.getString(R.string.numeroVideosPlaylist) ;
        holder.numVideos.setText(numVideosTextview);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
