package com.jaguarlandrover.rvi;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2015 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    RemoteConnectionManager.java
 * Project: RVI SDK
 *
 * Created by Lilli Szafranski on 5/19/15.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import android.util.Log;

/**
 * The remote connection manager of the RVI node.
 */
public class RemoteConnectionManager
{
    private final static String TAG = "RVI:RemoteCon...Manager";

    private BluetoothConnection mBluetoothConnection;
    private ServerConnection    mDirectServerConnection;

    private DlinkPacketParser mDataParser;

    private RemoteConnectionManagerListener mListener;

    RemoteConnectionManager() {
        mDataParser = new DlinkPacketParser(new DlinkPacketParser.DlinkPacketParserListener()
        {
            @Override
            public void onPacketParsed(DlinkPacket packet) {
                if (mListener != null) mListener.onRVIDidReceivePacket(packet);
            }
        });

        RemoteConnectionInterface.RemoteConnectionListener connectionListener = new RemoteConnectionInterface.RemoteConnectionListener()
        {
            @Override
            public void onRemoteConnectionDidConnect() {
                if (mListener != null) mListener.onRVIDidConnect();
            }

            @Override
            public void onRemoteConnectionDidDisconnect() {
                if (mListener != null) mListener.onRVIDidDisconnect();
            }

            @Override
            public void onRemoteConnectionDidFailToConnect(Error error) {
                if (mListener != null) mListener.onRVIDidFailToConnect(error);
            }

            @Override
            public void onRemoteConnectionDidReceiveData(String data) {
                mDataParser.parseData(data);
            }

            @Override
            public void onDidSendDataToRemoteConnection() {
                if (mListener != null) mListener.onRVIDidSendPacket();
            }

            @Override
            public void onDidFailToSendDataToRemoteConnection(Error error) {
                if (mListener != null) mListener.onRVIDidFailToSendPacket(error);
            }
        };

        mBluetoothConnection = new BluetoothConnection();
        mDirectServerConnection = new ServerConnection();

        mBluetoothConnection.setRemoteConnectionListener(connectionListener);
        mDirectServerConnection.setRemoteConnectionListener(connectionListener);
    }

    /**
     * Connect the local RVI node to the remote RVI node.
     */
    void connect() {
        closeConnections();

        RemoteConnectionInterface remoteConnection = selectEnabledRemoteConnection();

        if (remoteConnection == null) return;

        remoteConnection.connect();
    }

    /**
     * Disconnect the local RVI node from the remote RVI node
     */
    void disconnect() {
        closeConnections();
        mDataParser.clear();
    }

    /**
     * Send an RVI request packet.
     *
     * @param dlinkPacket the dlink packet
     */
    void sendPacket(DlinkPacket dlinkPacket) {
        Log.d(TAG, Util.getMethodName());

        RemoteConnectionInterface remoteConnection = selectConnectedRemoteConnection();

        if (remoteConnection == null) return; // TODO: Implement a cache to send out stuff after a connection has been established

        remoteConnection.sendRviRequest(dlinkPacket);
    }

    private RemoteConnectionInterface selectConnectedRemoteConnection() {
        if (mDirectServerConnection.isEnabled() && mDirectServerConnection.isConnected())
            return mDirectServerConnection;
        if (mBluetoothConnection.isEnabled() && mBluetoothConnection.isConnected())
            return mBluetoothConnection;

        return null;
    }

    private RemoteConnectionInterface selectEnabledRemoteConnection() { // TODO: This is going to be buggy if a connection is enabled but not connected; the other connections won't have connected
        if (mDirectServerConnection.isEnabled())                        // TODO: Rewrite better 'choosing' code
            return mDirectServerConnection;
        if (mBluetoothConnection.isEnabled())
            return mBluetoothConnection;

        return null;
    }

    private void closeConnections() {
        mDirectServerConnection.disconnect();
        mBluetoothConnection.disconnect();
    }

    /**
     * Sets the server url to the remote RVI node, when using a TCP/IP link to interface with a remote node.
     *
     * @param serverUrl the server url
     */
    void setServerUrl(String serverUrl) {
        /*RemoteConnectionManager.ourInstance.*/mDirectServerConnection.setServerUrl(serverUrl);
    }

    /**
     * Sets the server port of the remote RVI node, when using a TCP/IP link to interface with a remote node.
     *
     * @param serverPort the server port
     */
    void setServerPort(Integer serverPort) {
        /*RemoteConnectionManager.ourInstance.*/mDirectServerConnection.setServerPort(serverPort);
    }

//    /**
//     * Gets listener.
//     *
//     * @return the listener
//     */
//    static RemoteConnectionManagerListener getListener() {
//        return RemoteConnectionManager.ourInstance.mListener;
//    }

    /**
     * Sets the remote connection manager listener.
     *
     * @param listener the listener
     */
    void setListener(RemoteConnectionManagerListener listener) {
        /*RemoteConnectionManager.ourInstance.*/mListener = listener;
    }
}
