package com.listplay.listplay.fragments;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

import com.listplay.listplay.R;
import com.listplay.listplay.classes.Preferencias;

/**
 * Created by Vareta on 27-07-2016.
 */
public class SleepTimerDialog extends DialogFragment {
    private SeekBar seekBar;
    private TextView desactivar;
    private TextView cancelar;
    private TextView aceptar;
    private int tiempoEnPref;
    private Preferencias pref;
    private SleepTimerListener mListener;

    public SleepTimerDialog() {
        //empty constructor
    }

    public static SleepTimerDialog newInstance() {
        SleepTimerDialog sleepDialog = new SleepTimerDialog();
        sleepDialog.setStyle(STYLE_NORMAL, R.style.AppDialogTheme);
        return sleepDialog;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sleep_timer_dialog, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setElements(view);
        setListeners();
    }

    private void setElements(View view) {
        pref = new Preferencias();
        seekBar = (SeekBar) view.findViewById(R.id.tiempo);
        desactivar = (TextView) view.findViewById(R.id.desactivar);
        cancelar = (TextView) view.findViewById(R.id.cancelar);
        aceptar = (TextView) view.findViewById(R.id.aceptar);
        seekBar.setMax(180);
        tiempoEnPref = pref.getSleepTimer(getContext()); //obtiene la posicion anteriormente usada
        seekBar.setProgress(tiempoEnPref); //setea la posicion anterior en la seekbar
        if (tiempoEnPref != 0) { //tiempo anterior
            getDialog().setTitle(getString(R.string.titulo1_sleep) + " " + tiempoEnPref + " " + getString(R.string.titulo2_sleep));
        } else { //igual 0 --> desactivado
            getDialog().setTitle(getString(R.string.titulo1_sleep) + " " + getString(R.string.titulo3_sleep));
        }

        mListener = (SleepTimerListener) getActivity();

    }

    private void setListeners() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                int roundedProgress;
                if (i < 60) {
                    roundedProgress = (int) (Math.rint((double) i / 10) * 10);
                } else {
                    roundedProgress = (int) (Math.rint((double) i / 20) * 20);
                }
                tiempoEnPref = roundedProgress; //se le asigna para asi guardar el valor en caso de ser necesario
                if (fromUser) {
                    seekBar.setProgress(roundedProgress);
                    if (roundedProgress != 0) { //tiempo anterior
                        getDialog().setTitle(getString(R.string.titulo1_sleep) + " " +roundedProgress + " " + getString(R.string.titulo2_sleep));
                    } else { //igual 0 --> desactivado
                        getDialog().setTitle(getString(R.string.titulo1_sleep)+ " " + getString(R.string.titulo3_sleep));
                    }
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        desactivar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onDesactivarSleepTimer();
                getDialog().dismiss();
            }
        });

        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onAceptarSleepTimer(tiempoEnPref);
                getDialog().dismiss();
            }
        });

    }

    public interface SleepTimerListener {
        void onDesactivarSleepTimer();
        void onAceptarSleepTimer(int tiempo);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
