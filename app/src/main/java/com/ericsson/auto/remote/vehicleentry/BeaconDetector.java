package com.ericsson.auto.remote.vehicleentry;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

/**
 * Created by stoffe on 4/6/15.
 */
public class BeaconDetector {
    final NotificationManager notificationManager;
    final Context context;

    ///int NOTIFICATION_ID = 12345;
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
