package com.omorni.utils;

import android.view.View;

/**
 * Created by user on 5/24/2017.
 */

public interface RecyclerViewClickListener {
    void onClick(View view, int position);

    void onLongClick(View view, int position);
}
