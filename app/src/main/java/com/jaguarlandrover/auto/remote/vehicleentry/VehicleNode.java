package com.jaguarlandrover.auto.remote.vehicleentry;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2015 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    VehicleNode.java
 * Project: UnlockDemo
 *
 * Created by Lilli Szafranski on 10/28/15.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.jaguarlandrover.rvi.RVIRemoteNode;
import com.jaguarlandrover.rvi.RVIRemoteNodeListener;

import java.util.*;

public class VehicleNode
{
    private final static String TAG = "UnlockDemo:VehicleNode";

    /* Static variables */
    private static Context applicationContext = UnlockApplication.getContext();

    private static boolean isUnlocked;

    private static SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);

    private static RVIRemoteNode rviNode = new RVIRemoteNode(null);

    /* RVI fully-qualified service identifier parts */
    private final static String FOB_SIGNAL_BUNDLE = "fob";

    /* Remote service identifiers */
    final static String FOB_SIGNAL_UNLOCK      = "unlock";
    final static String FOB_SIGNAL_LOCK        = "lock";
//    final static String FOB_SIGNAL_AUTO_UNLOCK = "auto_unlock";
//    final static String FOB_SIGNAL_AUTO_LOCK   = "auto_lock";
    final static String FOB_SIGNAL_START       = "start";
    final static String FOB_SIGNAL_STOP        = "stop";
    final static String FOB_SIGNAL_HORN        = "horn";
    final static String FOB_SIGNAL_TRUNK       = "trunk";
    final static String FOB_SIGNAL_PANIC       = "panic";
    final static String FOB_SIGNAL_LIGHTS      = "lights";

    private enum ConnectionStatus
    {
        CONNECTING,
        CONNECTED,
        DISCONNECTING,
        DISCONNECTED
    }

    private static ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;

    private static VehicleNode ourInstance = new VehicleNode();

    private VehicleNode() {
        Log.d(TAG, "VehicleNode()");

        RVIRemoteNodeListener nodeListener = new RVIRemoteNodeListener()
        {
            @Override
            public void nodeDidConnect(RVIRemoteNode node) {
                Log.d(TAG, "Connected to vehicle!");
                connectionStatus = ConnectionStatus.CONNECTED;

                //stopRepeatingTask();
            }

            @Override
            public void nodeDidFailToConnect(RVIRemoteNode node, Throwable reason) {
                Log.d(TAG, "Failed to connect to vehicle!");
                connectionStatus = ConnectionStatus.DISCONNECTED;
            }

            @Override
            public void nodeDidDisconnect(RVIRemoteNode node, Throwable reason) {
                Log.d(TAG, "Disconnected from vehicle!");
                connectionStatus = ConnectionStatus.DISCONNECTED;

                ///* Try and reconnect */
                //startRepeatingTask();
            }

            @Override
            public void nodeSendServiceInvocationSucceeded(RVIRemoteNode node, String serviceIdentifier) {

            }

            @Override
            public void nodeSendServiceInvocationFailed(RVIRemoteNode node, String serviceIdentifier, Throwable reason) {

            }

            @Override
            public void nodeReceiveServiceInvocationSucceeded(RVIRemoteNode node, String serviceIdentifier, Object parameters) {

            }

            @Override
            public void nodeReceiveServiceInvocationFailed(RVIRemoteNode node, String serviceIdentifier, Throwable reason) {

            }

            @Override
            public void nodeDidAuthorizeLocalServices(RVIRemoteNode node, ArrayList<String> serviceIdentifiers) {

            }

            @Override
            public void nodeDidAuthorizeRemoteServices(RVIRemoteNode node, ArrayList<String> serviceIdentifiers) {

            }
        };

        rviNode.setListener(nodeListener);
    }

    static boolean isConnecting() {
        return (connectionStatus == ConnectionStatus.CONNECTING);
    }

    static boolean isConnected() {
        return (connectionStatus == ConnectionStatus.CONNECTED);
    }

    static boolean isUnlocked() {
        return isUnlocked;
    }

    static void connect() {
        if (connectionStatus == ConnectionStatus.CONNECTING) return; // TODO: Do we want to move this logic down into the SDK?

        Log.d(TAG, "Attempting to connect to vehicle.");

        rviNode.setServerUrl(preferences.getString("pref_rvi_vehicle_url", "38.129.64.40"));
        rviNode.setServerPort(Integer.parseInt(preferences.getString("pref_rvi_vehicle_port", "8807")));

        connectionStatus = ConnectionStatus.CONNECTING;

        rviNode.connect();
    }

    static void disconnect() {
        connectionStatus = ConnectionStatus.DISCONNECTING;

        rviNode.disconnect();
    }

    static void sendFobSignal(String fobSignal) {
        Log.d(TAG, "Sending signal to car: " + fobSignal);

        if (connectionStatus == ConnectionStatus.DISCONNECTED) connect();

        rviNode.invokeService(FOB_SIGNAL_BUNDLE + "/" + fobSignal, new FobParamsManager.FobParams(), 5000);

        if (fobSignal.equals(FOB_SIGNAL_LOCK)) isUnlocked = false;
        if (fobSignal.equals(FOB_SIGNAL_UNLOCK)) isUnlocked = true;
    }

}
