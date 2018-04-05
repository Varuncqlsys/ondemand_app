package com.omorni.utils;

/**
 * Created by cqlsys-andorid on 7/25/2016.
 */

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.omorni.service.UpdateLocationService;


public class ConnectivityReceiver extends BroadcastReceiver {
    Activity activity;
    public static ConnectivityReceiverListener connectivityReceiverListener;

    public ConnectivityReceiver() {
        super();

    }

    @Override
    public void onReceive(Context context, Intent arg1) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (activeNetwork != null) {
            if (activeNetwork.isConnected()) {
                if (!Utils.isMyServiceRunning(UpdateLocationService.class, context)) {
                    Intent mServiceIntent = new Intent(context, UpdateLocationService.class);
                    context.startService(mServiceIntent);
                }
            }
        } else {
            context.stopService(new Intent(context, UpdateLocationService.class));
        }

        if (connectivityReceiverListener != null) {
            connectivityReceiverListener.onNetworkConnectionChanged(isConnected, context);
        }
    }

    public static boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) AppController.getInstance().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();
    }


    public interface ConnectivityReceiverListener {
        void onNetworkConnectionChanged(boolean isConnected, Context activity);
    }
}