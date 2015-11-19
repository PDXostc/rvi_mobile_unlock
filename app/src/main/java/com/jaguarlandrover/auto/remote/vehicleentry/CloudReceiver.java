/**
 * Copyright (C) 2015, Jaguar Land Rover
 *
 * This program is licensed under the terms and conditions of the Mozilla Public License, version 2.0.  The full text of the Mozilla Public License is at
 * https://www.mozilla.org/MPL/2.0/
 */

package com.jaguarlandrover.auto.remote.vehicleentry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class CloudReceiver extends BroadcastReceiver
{
    private static final String TAG = "RVI";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo == null || networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
                Log.i(TAG, "Network is disconnected: " + networkInfo);
            } else if (networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                Log.i(TAG, "Network is connected: " + networkInfo + " type = " + networkInfo.getTypeName() + " : " + networkInfo.getSubtypeName());
            }
        }
    }
}
