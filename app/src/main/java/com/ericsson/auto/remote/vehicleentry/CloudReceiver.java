package com.ericsson.auto.remote.vehicleentry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by stoffe on 6/10/15.
 */
public class CloudReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo == null || networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
                // Wifi is disconnected
                Log.i("STOFFE", "Network is disconnected: " + networkInfo);
            } else if(networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                Log.i("STOFFE", "Network is connected: " + networkInfo+" type = "+networkInfo.getTypeName()+" : "+networkInfo.getSubtypeName());
            }
        }
    }
}
