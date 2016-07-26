package com.listplay.listplay.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.listplay.listplay.R;
import com.listplay.listplay.classes.CRUD;
import com.listplay.listplay.models.Video;

/**
 * Created by Vareta on 19-07-2016.
 */
public class EditVideoDialog extends DialogFragment {
    private Context appContext;
    private Video video;
    private EditText nombreEdit;
    private EditText artistaEdit;
    private TextView tituloYoutube;
    private TextView aceptar;
    private TextView cancelar;
    private CRUD crud;
    private EditVideoListener mListener;


    public EditVideoDialog() {
        //Empty constructor
    }

    public static EditVideoDialog newInstance(long idVideo) {
        EditVideoDialog editFrag = new EditVideoDialog();
        Bundle variables = new Bundle();
        variables.putLong("idvideo", idVideo);
        editFrag.setArguments(variables);
        editFrag.setStyle(STYLE_NORMAL, R.style.AppDialogTheme);
        return editFrag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.edit_popup, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setPopUpElements(view);
        setListeners();
    }


    private void setPopUpElements(View popUpView) {
        crud = new CRUD();
        video = crud.getVideo(getArguments().getLong("idvideo"));
        nombreEdit = (EditText) popUpView.findViewById(R.id.nombre_edit);
        artistaEdit = (EditText) popUpView.findViewById(R.id.artista_edit);
        tituloYoutube = (TextView) popUpView.findViewById(R.id.titulo_youtube_contenido);
        aceptar = (TextView) popUpView.findViewById(R.id.aceptar);
        cancelar = (TextView) popUpView.findViewById(R.id.cancelar);
        nombreEdit.setText(video.getCancion());
        artistaEdit.setText(video.getArtista());
        tituloYoutube.setText(video.getTitulo());
        aceptar.setEnabled(false);
        cambiaColorAceptar();
        getDialog().setTitle(getString(R.string.editar_popup));


    }

    private void setListeners() {
        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (aceptar.isEnabled()) {//realiza el proceso para guardar los cambios
                    crud.editVideo(video.getId(), nombreEdit.getText().toString(), artistaEdit.getText().toString());
                    mListener = (EditVideoListener) getActivity();
                    mListener.onEditVideo(video.getId(), nombreEdit.getText().toString(), artistaEdit.getText().toString());
                    getDialog().dismiss();
                }
            }
        });

        nombreEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (video.getCancion() == null || video.getCancion().equals("")) {
                    if (charSequence.toString().trim().length() > 0) { //ha habido un cambio
                        aceptar.setEnabled(true);
                    } else {
                        aceptar.setEnabled(false);
                    }
                } else {
                    if (charSequence.toString().trim().length() > 0 && !video.getArtista().equals(charSequence)) { //ha habido un cambio
                        aceptar.setEnabled(true);
                    } else {
                        aceptar.setEnabled(false);
                    }
                }
                cambiaColorAceptar();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        artistaEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (video.getArtista() == null || video.getArtista().equals("")) {
                    if (charSequence.toString().trim().length() > 0) { //ha habido un cambio
                        aceptar.setEnabled(true);
                    } else {
                        aceptar.setEnabled(false);
                    }
                } else {
                    if (charSequence.toString().trim().length() > 0 && !video.getArtista().equals(charSequence)) { //ha habido un cambio
                        aceptar.setEnabled(true);
                    } else {
                        aceptar.setEnabled(false);
                    }
                }
                cambiaColorAceptar();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void cambiaColorAceptar() {
        if (aceptar.isEnabled()) {
            aceptar.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        } else {
            aceptar.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccentLight));
        }
    }

    public interface EditVideoListener {
        void onEditVideo(long idVideo, String nombreCancion, String artista);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
