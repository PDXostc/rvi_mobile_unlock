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
import com.jaguarlandrover.rvi.RVINode;
import com.jaguarlandrover.rvi.ServiceBundle;

import java.util.*;

public class VehicleNode
{
    private final static String TAG = "UnlockDemo:VehicleNode";

    /* Static objects */
    private static Context applicationContext = UnlockApplication.getContext();

    private static SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);

    private static RVINode rviNode = new RVINode(null);

    /* RVI fully-qualified service identifier parts */
    private final static String RVI_DOMAIN        = "jlr.com";
    private final static String FOB_SIGNAL_BUNDLE = "fob";

    /* Remote service identifiers */
    public final static String FOB_SIGNAL_UNLOCK      = "unlock";
    public final static String FOB_SIGNAL_LOCK        = "lock";
    public final static String FOB_SIGNAL_AUTO_UNLOCK = "auto_unlock";
    public final static String FOB_SIGNAL_AUTO_LOCK   = "auto_lock";
    public final static String FOB_SIGNAL_START       = "start";
    public final static String FOB_SIGNAL_STOP        = "stop";
    public final static String FOB_SIGNAL_HORN        = "horn";
    public final static String FOB_SIGNAL_TRUNK       = "trunk";
    public final static String FOB_SIGNAL_PANIC       = "panic";
    public final static String FOB_SIGNAL_LIGHTS      = "lights";

    /* Service bundles */
    private final static ServiceBundle fobSignalServiceBundle = new ServiceBundle(applicationContext, RVI_DOMAIN, FOB_SIGNAL_BUNDLE, null);

    private enum ConnectionStatus
    {
        CONNECTING,
        CONNECTED,
        DISCONNECTING,
        DISCONNECTED
    }

    private static ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;

    private static VehicleNode ourInstance = new VehicleNode();

    //public static VehicleNode getInstance() {
    //    return ourInstance;
    //}

    private VehicleNode() {
        Log.d(TAG, "VehicleNode()");

        /* Listeners */
        ServiceBundle.ServiceBundleListener serviceBundleListener = new ServiceBundle.ServiceBundleListener()
        {
            @Override
            public void onServiceInvoked(ServiceBundle serviceBundle, String serviceIdentifier, Object parameters) {

            }
        };

        RVINode.RVINodeListener nodeListener = new RVINode.RVINodeListener()
        {
            @Override
            public void nodeDidConnect() {
                Log.d(TAG, "Connected to vehicle!");
                connectionStatus = ConnectionStatus.CONNECTED;

                //stopRepeatingTask();
            }

            @Override
            public void nodeDidFailToConnect(Throwable trigger) {
                Log.d(TAG, "Failed to connect to vehicle!");
                connectionStatus = ConnectionStatus.DISCONNECTED;
            }

            @Override
            public void nodeDidDisconnect(Throwable trigger) {
                Log.d(TAG, "Disconnected from vehicle!");
                connectionStatus = ConnectionStatus.DISCONNECTED;

                ///* Try and reconnect */
                //startRepeatingTask();
            }
        };

        fobSignalServiceBundle.setListener(serviceBundleListener);

        rviNode.setListener(nodeListener);

        rviNode.addBundle(fobSignalServiceBundle);
    }

    public static boolean isConnecting() {
        return (connectionStatus == ConnectionStatus.CONNECTING);
    }

    public static boolean isConnected() {
        return (connectionStatus == ConnectionStatus.CONNECTED);
    }

    public static void connect() {
        if (connectionStatus == ConnectionStatus.CONNECTING) return; // TODO: Do we want to move this logic down into the SDK?

        Log.d(TAG, "Attempting to connect to vehicle.");

        rviNode.setBluetoothServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB".toLowerCase())); /* SerialPortServiceClass */
        rviNode.setBluetoothChannel(Integer.parseInt(preferences.getString("pref_bt_channel", "1")));

        connectionStatus = ConnectionStatus.CONNECTING;

        rviNode.connect();
    }

    public static void disconnect() {
        connectionStatus = ConnectionStatus.DISCONNECTING;

        rviNode.disconnect();
    }

    public static void sendFobSignal(String fobSignal) {//}, Object params) {
        Log.d(TAG, "Sending signal to car: " + fobSignal);

        if (connectionStatus == ConnectionStatus.DISCONNECTED) connect();

        fobSignalServiceBundle.invokeService(fobSignal, new FobParamsManager.FobParams(), 5000);
    }

    public static void setDeviceAddress(String deviceAddress) {
        rviNode.setBluetoothDeviceAddress(deviceAddress);
    }
}
