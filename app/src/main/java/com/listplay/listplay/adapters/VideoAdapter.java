package com.listplay.listplay.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.listplay.listplay.classes.CustomRecyclerListener;
import com.listplay.listplay.models.Video;
import com.listplay.listplay.R;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

/**
 * Created by Vareta on 11-06-2016.
 */
public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoHolder>{
    private List<Video> items;
    private CustomRecyclerListener mListener;
    private DateTime dt;

    public VideoAdapter(List<Video> items) {
        this.items = items;
    }

    public class VideoHolder extends RecyclerView.ViewHolder implements OnClickListener, OnLongClickListener {
        private TextView nombreCancion;
        private TextView nombreArtista;
        private TextView duracion;

        public VideoHolder(View itemView) {
            super(itemView);
            nombreCancion = (TextView)itemView.findViewById(R.id.nombreCancion);
            nombreArtista = (TextView)itemView.findViewById(R.id.nombreArtista);
            duracion = (TextView)itemView.findViewById(R.id.duracion);
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
    public VideoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_element, parent, false);

        return new VideoHolder(v);
    }

    @Override
    public void onBindViewHolder(VideoHolder holder, int position) {
        if (items.get(position).getArtista() != null) {
            holder.nombreCancion.setText(items.get(position).getCancion());
            holder.nombreArtista.setText(items.get(position).getArtista());
        } else {
            holder.nombreCancion.setText(items.get(position).getTitulo());
            holder.nombreArtista.setText("");
        }
        holder.duracion.setText(new DateTime(items.get(position).getDuracion()).toString("mm:ss"));

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Elimina un item dada cierta posicion
     * @param position posicion del elemento a eliminar
     */
    public void remove(int position) {
        items.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * Cambia de posicion entre elementos de la lista
     * @param firstPosition posicion inicial
     * @param secondPosition posicion final
     */
    public void swap(int firstPosition, int secondPosition) {
        Collections.swap(items, firstPosition, secondPosition);
        notifyItemMoved(firstPosition, secondPosition);
    }
}
