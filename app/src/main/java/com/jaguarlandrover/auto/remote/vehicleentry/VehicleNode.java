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
import android.content.res.AssetManager;
import android.preference.PreferenceManager;
import android.util.Log;
import com.jaguarlandrover.rvi.RVIRemoteNode;
import com.jaguarlandrover.rvi.RVIRemoteNodeListener;
import com.jaguarlandrover.rvi.ServiceBundle;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.*;

public class VehicleNode
{
    private final static String TAG = "UnlockDemo:VehicleNode";

    /* Static variables */
    private static Context applicationContext = UnlockApplication.getContext();

    private static boolean isUnlocked;

    private static SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);

    private static RVIRemoteNode rviNode = new RVIRemoteNode(null);

//    /* RVI fully-qualified service identifier parts */
//    private final static String RVI_DOMAIN        = "genivi.org";
    private final static String FOB_SIGNAL_BUNDLE = "fob";

    /* Remote service identifiers */
    public final static String FOB_SIGNAL_UNLOCK      = "unlock";
    public final static String FOB_SIGNAL_LOCK        = "lock";
//    public final static String FOB_SIGNAL_AUTO_UNLOCK = "auto_unlock";
//    public final static String FOB_SIGNAL_AUTO_LOCK   = "auto_lock";
    public final static String FOB_SIGNAL_START       = "start";
    public final static String FOB_SIGNAL_STOP        = "stop";
    public final static String FOB_SIGNAL_HORN        = "horn";
    public final static String FOB_SIGNAL_TRUNK       = "trunk";
    public final static String FOB_SIGNAL_PANIC       = "panic";
    public final static String FOB_SIGNAL_LIGHTS      = "lights";

//    /* Service bundles */
//    private final static ServiceBundle fobSignalServiceBundle = new ServiceBundle(applicationContext, RVI_DOMAIN, FOB_SIGNAL_BUNDLE, null);

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

//        /* Listeners */
//        ServiceBundle.ServiceBundleListener serviceBundleListener = new ServiceBundle.ServiceBundleListener()
//        {
//            @Override
//            public void onServiceInvoked(ServiceBundle serviceBundle, String serviceIdentifier, Object parameters) {
//
//            }
//        };

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


//        try {
//            rviNode.setKeyStores(getKeyStore("server-certs", "BKS", "password"), getKeyStore("client.p12", "PKCS12", "password"), "password");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        //rviNode.addJWTCredentials("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJyaWdodF90b19pbnZva2UiOlsiZ2VuaXZpLm9yZyJdLCJpc3MiOiJqbHIuY29tIiwiZGV2aWNlX2NlcnQiOiJNSUlCOHpDQ0FWd0NBUUV3RFFZSktvWklodmNOQVFFTEJRQXdRakVMTUFrR0ExVUVCaE1DVlZNeER6QU5CZ05WQkFnTUJrOXlaV2R2YmpFUk1BOEdBMVVFQnd3SVVHOXlkR3hoYm1ReER6QU5CZ05WQkFvTUJrZEZUa2xXU1RBZUZ3MHhOVEV4TWpjeU16RTBOVEphRncweE5qRXhNall5TXpFME5USmFNRUl4Q3pBSkJnTlZCQVlUQWxWVE1ROHdEUVlEVlFRSURBWlBjbVZuYjI0eEVUQVBCZ05WQkFjTUNGQnZjblJzWVc1a01ROHdEUVlEVlFRS0RBWkhSVTVKVmtrd2daOHdEUVlKS29aSWh2Y05BUUVCQlFBRGdZMEFNSUdKQW9HQkFKdHZpTThBUklyRnF1UGMwbXlCOUJ1RjlNZGtBLzJTYXRxYlpNV2VUT1VKSEdyakJERUVNTFE3ems4QXlCbWk3UnF1WVlaczY3U3lMaHlsVkdLaDZzSkFsZWN4YkhVd2o3Y1pTUzFibUtNamU2TDYxZ0t3eEJtMk5JRlUxY1ZsMmpKbFRhVTlWWWhNNHhrNTd5ajI4bmtOeFNZV1AxdmJGWDJORFgyaUg3YjVBZ01CQUFFd0RRWUpLb1pJaHZjTkFRRUxCUUFEZ1lFQWhicVZyOUUvME03MjluYzZESStxZ3FzUlNNZm95dkEzQ21uL0VDeGwxeWJHa3V6TzdzQjhmR2pnTVE5enpjYjZxMXVQM3dHalBpb3FNeW1pWVlqVW1DVHZ6ZHZSQlorNlNEanJaZndVdVlleGlLcUk5QVA2WEthSGxBTDE0K3JLKzZITjR1SWtaY0l6UHdTTUhpaDFic1RScHlZNVozQ1VEY0RKa1l0VmJZcz0iLCJ2YWxpZGl0eSI6eyJzdGFydCI6MTQ1MjE5Mjc3Nywic3RvcCI6MTQ4MzcyODc3N30sInJpZ2h0X3RvX3JlZ2lzdGVyIjpbImdlbml2aS5vcmciXSwiY3JlYXRlX3RpbWVzdGFtcCI6MTQ1MjE5Mjc3NywiaWQiOiJpbnNlY3VyZV9jcmVkZW50aWFscyJ9.TBDUJFL1IQ039Lz7SIkcblhz62jO35STJ8OiclL_xlxEE_L_EjnELrDOGvkIh7zhhl8RMHkUJcTFQKF7P6WDJ5rUJejXJlkTRf-aVmHqEhpspRw6xD2u_2A9wmTWLJF94_wsEb7M7xWCXVrbexu_oik85zmuxRQgRE5wrTC7DDQ");

//        fobSignalServiceBundle.setListener(serviceBundleListener);

        rviNode.setListener(nodeListener);

//        rviNode.addBundle(fobSignalServiceBundle);
    }

    private static KeyStore getKeyStore(String fileName, String type, String password) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException { // type = "jks"?
        AssetManager assetManager = applicationContext.getAssets();
        InputStream fis = assetManager.open(fileName);

        KeyStore ks = KeyStore.getInstance(type);
        ks.load(fis, password.toCharArray());
        fis.close();

        return ks;
    }

    public static boolean isConnecting() {
        return (connectionStatus == ConnectionStatus.CONNECTING);
    }

    public static boolean isConnected() {
        return (connectionStatus == ConnectionStatus.CONNECTED);
    }

    public static boolean isUnlocked() {
        return isUnlocked;
    }

//    public static void setKeyStoresAndPrivileges(KeyStore serverKeyStore, KeyStore deviceKeyStore, String deviceKeyStorePassword, ArrayList<String> privileges) {
//        rviNode.setKeyStores(serverKeyStore, deviceKeyStore, deviceKeyStorePassword);
//
//        for (String jwt : privileges) {
//            rviNode.addJWTCredentials(jwt);
//        }
//    }

    public static void connect() {
        if (connectionStatus == ConnectionStatus.CONNECTING) return; // TODO: Do we want to move this logic down into the SDK?

        Log.d(TAG, "Attempting to connect to vehicle.");

        rviNode.setServerUrl(preferences.getString("pref_rvi_vehicle_url", "38.129.64.40"));
        rviNode.setServerPort(Integer.parseInt(preferences.getString("pref_rvi_vehicle_port", "8807")));

//        rviNode.setBluetoothServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB".toLowerCase())); /* SerialPortServiceClass */
//        rviNode.setBluetoothChannel(Integer.parseInt(preferences.getString("pref_bt_channel", "1")));

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

        //fobSignalServiceBundle.invokeService(fobSignal, new FobParamsManager.FobParams(), 5000);
        rviNode.invokeService(FOB_SIGNAL_BUNDLE + "/" + fobSignal, new FobParamsManager.FobParams(), 5000);

        if (fobSignal.equals(FOB_SIGNAL_LOCK)) isUnlocked = false;
        if (fobSignal.equals(FOB_SIGNAL_UNLOCK)) isUnlocked = true;
    }

//    public static void setDeviceAddress(String deviceAddress) {
//        rviNode.setBluetoothDeviceAddress(deviceAddress);
//    }
}
