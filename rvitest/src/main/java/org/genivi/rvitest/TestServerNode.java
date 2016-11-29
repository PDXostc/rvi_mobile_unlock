package org.genivi.rvitest;

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2015 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    TestServerNode.java
 * Project: UnlockDemo
 *
 * Created by Lilli Szafranski on 10/28/15.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import android.os.Handler;
import android.util.Log;

import org.genivi.rvi.RVILocalNode;
import org.genivi.rvi.RVIRemoteNode;
import org.genivi.rvi.RVIRemoteNodeListener;

import java.util.*;

class TestServerNode
{
    private static final String TAG = "RVITest/TestServerNode_";

    interface Listener {
        void testServerNodeDidConnect();
        void testServerNodeDidDisconnect();
        void testServerNodeDidAuthorizeRemoteServices(Set<String> serviceIdentifiers);
        void testServerNodeDidAuthorizeLocalServices(Set<String> serviceIdentifiers);
    }

    private static RVIRemoteNode rviNode = new RVIRemoteNode(null);

    private static ArrayList<Listener> listeners = new ArrayList<>();

    private final static String  TEST_SERVER_URL  = "38.129.64.40";
    private final static Integer TEST_SERVER_PORT = 9010;

    private final static ArrayList<String> testLocalServiceIdentifiers =
            new ArrayList<>(Arrays.asList(
                    "/test/india/alpha",
                    "/test/india/bravo",
                    "/test/india/charlie",
                    "/test/juliette/charlie",
                    "/test/juliette/delta",
                    "/test/juliette/echo",
                    "/test/kilo/foxtrot/golf",
                    "/test/kilo/foxtrot/hotel",
                    "/test/kilo/golf/hotel",
                    "/test/kilo/hotel/golf",
                    "/test/kilo/foxtrot/golf/hotel",
                    "/test/lima"
            ));

    private enum ConnectionStatus
    {
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }

    private static ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;
    private static boolean shouldTryAndReconnect = false;

    private static TestServerNode ourInstance = new TestServerNode();

    private TestServerNode() {

        RVIRemoteNodeListener nodeListener = new RVIRemoteNodeListener()
        {
            @Override
            public void nodeDidConnect(RVIRemoteNode node) {
                Log.d(TAG, "Connected to RVI provisioning server!");
                connectionStatus = ConnectionStatus.CONNECTED;

                stopRepeatingTask();

                for (Listener listener : listeners)
                    listener.testServerNodeDidConnect();
            }

            @Override
            public void nodeDidFailToConnect(RVIRemoteNode node, Throwable reason) {
                Log.d(TAG, "Failed to connect to RVI provisioning server!");
                connectionStatus = ConnectionStatus.DISCONNECTED;
            }

            @Override
            public void nodeDidDisconnect(RVIRemoteNode node, Throwable reason) {
                Log.d(TAG, "Disconnected from RVI provisioning server!");
                connectionStatus = ConnectionStatus.DISCONNECTED;

                /* Try and reconnect */
                if (shouldTryAndReconnect)
                    startRepeatingTask();

                for (Listener listener : listeners)
                    listener.testServerNodeDidDisconnect();
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
            public void nodeDidAuthorizeLocalServices(RVIRemoteNode node, Set<String> serviceIdentifiers) {
                Log.d(TAG, "Local services available: " + serviceIdentifiers.toString());

                for (Listener listener : listeners)
                    listener.testServerNodeDidAuthorizeLocalServices(serviceIdentifiers);
            }

            @Override
            public void nodeDidAuthorizeRemoteServices(RVIRemoteNode node, Set<String> serviceIdentifiers) {
                Log.d(TAG, "Remote services available: " + serviceIdentifiers.toString());

                for (Listener listener : listeners)
                    listener.testServerNodeDidAuthorizeRemoteServices(serviceIdentifiers);
            }
        };

        rviNode.setListener(nodeListener);

        RVILocalNode.addLocalServices(TestApplication.getContext(), testLocalServiceIdentifiers);
    }

    Handler  timerHandler  = new Handler();
    Runnable timerRunnable = new Runnable()
    {
        @Override
        public void run() {
            if (connectionStatus == ConnectionStatus.DISCONNECTED) connect();

            timerHandler.postDelayed(this, 60 * 1000);
        }
    };

    private void startRepeatingTask() {
        timerHandler.postDelayed(timerRunnable, 60 * 1000);
    }

    private void stopRepeatingTask() {
        timerHandler.removeCallbacks(timerRunnable);
    }

    static void connect() {
        Log.d(TAG, "Attempting to connect to RVI provisioning server.");

        rviNode.setServerUrl(TEST_SERVER_URL);
        rviNode.setServerPort(TEST_SERVER_PORT);

        connectionStatus = ConnectionStatus.CONNECTING;
        shouldTryAndReconnect = true;

        rviNode.connect();
    }

    static void disconnect() {
        Log.d(TAG, "Disconnecting from the RVI provisioning server.");

        shouldTryAndReconnect = false;

        rviNode.disconnect();
    }

    static boolean isConnected() {
        return connectionStatus == ConnectionStatus.CONNECTED;
    }

    static void addListener(Listener listener) {
        listeners.add(listener);
    }

    static void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    static void updatedLocalCredentials(ArrayList<String> credentialStrings) {
        RVILocalNode.setCredentials(TestApplication.getContext(), credentialStrings);
    }

    static void updateRemoteCredentials(ArrayList<String> credentialStrings) {

    }
}
