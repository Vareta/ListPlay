package com.listplay.listplay.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.listplay.listplay.R;
import com.listplay.listplay.classes.CRUD;
import com.listplay.listplay.classes.Utilities;

/**
 * Created by Vareta on 25-07-2016.
 */
public class NewPlayListDialog extends DialogFragment {
    private EditText nombrePlaylist;
    private TextView cancelar;
    private TextView aceptar;
    private long idVideo;
    private CRUD crud;

    public NewPlayListDialog() {
        //empty constructor
    }

    public static NewPlayListDialog newInstance(long idVideo) {
        NewPlayListDialog newPlayListDialog = new NewPlayListDialog();
        Bundle variables = new Bundle();
        variables.putLong("idvideo", idVideo);
        newPlayListDialog.setArguments(variables);
        newPlayListDialog.setStyle(STYLE_NORMAL, R.style.AppDialogTheme);
        return newPlayListDialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.new_playlist_dialog, container);
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
        nombrePlaylist = (EditText) view.findViewById(R.id.playlist);
        cancelar = (TextView) view.findViewById(R.id.cancelar);
        aceptar = (TextView) view.findViewById(R.id.aceptar);
        aceptar.setEnabled(false);
        cambiaColorAceptar();
        getDialog().setTitle(getString(R.string.new_playlist_title_addto));
        muestraTeclado();
    }

    private void setListeners() {
        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ocultarTeclado();
                getDialog().dismiss();
            }
        });

        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (aceptar.isEnabled()) { //si existe texto en el edittext
                    Utilities util = new Utilities();
                    String nombre = nombrePlaylist.getText().toString().trim(); //obtiene el texto
                    if (!crud.existePlaylist(nombre)) { //si no existe la playlist
                        crud.añadirPlayList(nombre); //la añade
                        long playlistId = crud.getPlaylistId(nombre); //consigue su id
                        crud.añadirVideoAPlaylist(idVideo, playlistId); //añade el video a la playlist
                        util.mensajeCorto(getContext(), getString(R.string.exito_playlist_y_video_addto)); //mensaje de exito al usuario
                        //Busca el dialogo anterior y lo cierra
                        DialogFragment dialogoAnterior = (DialogFragment) getActivity().getSupportFragmentManager().findFragmentByTag("add_to_playlist_fragment");
                        dialogoAnterior.getDialog().dismiss();
                        //cierra el dialogo actual
                        getDialog().dismiss();
                    } else {//si ya existe la playlist
                        util.mensajeCorto(getContext(), getString(R.string.lista_repetida_addto));
                    }
                }
            }
        });

        nombrePlaylist.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    aceptar.setEnabled(true);
                } else {
                    aceptar.setEnabled(false);
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

    private void ocultarTeclado() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(nombrePlaylist.getWindowToken(), 0);
    }

    private void muestraTeclado() {
        InputMethodManager imm = (InputMethodManager)   getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }


    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
        muestraTeclado(); //en realidad lo cierra, por algun motivo, ocultarTeclado no cumple su funcion en esta etapa
    }

}
