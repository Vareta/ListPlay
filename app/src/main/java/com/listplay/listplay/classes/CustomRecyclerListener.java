package com.listplay.listplay.classes;

import android.view.View;

/**
 * Created by Vareta on 09-06-2016.
 */
public interface CustomRecyclerListener {

    void customClickListener(View v, int position);
    void customLongClickListener(View v, int position);
}
