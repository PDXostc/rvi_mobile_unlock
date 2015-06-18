/**
 *  Copyright (C) 2015, Jaguar Land Rover
 *
 *  This program is licensed under the terms and conditions of the
 *  Mozilla Public License, version 2.0.  The full text of the
 *  Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 */

package com.jaguarlandrover.auto.remote.vehicleentry;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class BeaconDetector {
    private static final String TAG = "RVI";

    final NotificationManager notificationManager;
    final Context context;

    public BeaconDetector(NotificationManager notificationManager, Context context) {
        this.notificationManager = notificationManager;
        this.context = context;
    }

    public void reportNewBeacon(String bid) {
        Notification n = creteNotification(context, "Car Discovered : " + bid);
        notificationManager.notify(0,n);
        //TODO notify the GUI
    }

    public void reportLostBeacon(String bid) {
        Notification n = creteNotification(context, "Car Out of Range : " + bid);
        notificationManager.notify(0,n);
        //TODO notify the GUI
    }

    static Notification creteNotification(Context ctx, String action) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(ctx)
                        .setSmallIcon(R.drawable.rvi)
                        .setContentTitle("Lock App")
                        .setContentText(action);

        Intent targetIntent = new Intent(ctx, LockActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        return builder.build();
    }
}
