package com.jaguarlandrover.auto.remote.vehicleentry.wear;
/**
 * Copyright (C) 2015, Jaguar Land Rover
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0.  The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 */

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.widget.Toast;

/**
 * Created by stoffe on 6/23/15.
 */
public class WatchNotificationReceiver extends WakefulBroadcastReceiver {
    public static final String CONTENT_KEY = "Unlock!?";
    static int notificationId = 1;

    public WatchNotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
//        Intent displayIntent = new Intent(context, MyDisplayActivity.class);
        String text = intent.getStringExtra(CONTENT_KEY);
//        Notification notification = new Notification.Builder(context)
//                //.setSmallIcon(R.drawable.common_signin_btn_icon_dark)
//                .setContentTitle(text)
//                .extend(new Notification.WearableExtender()
//                        .setDisplayIntent(PendingIntent.getActivity(context, 0, displayIntent,
//                                PendingIntent.FLAG_UPDATE_CURRENT)))
//                .build();
//        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(0, notification);


        // Build intent for notification content
        Intent viewIntent = new Intent(context, WatchDisplayActivity.class);
        //viewIntent.putExtra(EXTRA_EVENT_ID, eventId);
        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(context, 0, viewIntent, 0);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.rvi)
                        .extend(new NotificationCompat.WearableExtender()
                                .setContentIcon(R.drawable.rvi))
                        .setVibrate(new long[]{500, 500, 500, 500, 500, 500})
                        .setContentTitle(text)
                        .setContentText(text)
                        .setContentIntent(viewPendingIntent);

// Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(context);

// Build the notification and issues it with notification manager.
        notificationManager.notify(notificationId++, notificationBuilder.build());


        Toast.makeText(context, "Not Post", Toast.LENGTH_SHORT).show();
    }
}
