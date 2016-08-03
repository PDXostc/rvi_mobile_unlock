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

import java.security.KeyStore;
import java.util.UUID;

/**
 * The remote connection manager of the RVI node.
 */
public class RemoteConnectionManager
{
    private final static String TAG = "RVI:RemoteCon...Manager";

    private BluetoothConnection mBluetoothConnection;
    private ServerConnection    mDirectServerConnection;
    private RemoteConnectionInterface mRemoteConnection;

    public enum ConnectionType
    {
        UNKNOWN,
        SERVER,
        BLUETOOTH,
        GLOBAL
    }

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
            public void onRemoteConnectionDidDisconnect(Throwable error) {
                if (mListener != null) mListener.onRVIDidDisconnect(error);
            }

            @Override
            public void onRemoteConnectionDidFailToConnect(Throwable error) {
                if (mListener != null) mListener.onRVIDidFailToConnect(error);
            }

            @Override
            public void onRemoteConnectionDidReceiveData(String data) {
                mDataParser.parseData(data);
            }

            @Override
            public void onDidSendDataToRemoteConnection(DlinkPacket packet) {
                if (mListener != null) mListener.onRVIDidSendPacket(packet);
            }

            @Override
            public void onDidFailToSendDataToRemoteConnection(Throwable error) {
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
    void connect(ConnectionType type) {
        //closeConnections(type);
        //if (mRemoteConnection != null) mRemoteConnection.disconnect(null);

        mRemoteConnection = selectConfiguredRemoteConnection(type);

        if (mRemoteConnection != null) mRemoteConnection.connect();
    }

    /**
     * Disconnect the local RVI node from the remote RVI node
     */
    void disconnect(ConnectionType type) {
        //closeConnections(type);

        if (mRemoteConnection != null) mRemoteConnection.disconnect(null);

        mDataParser.clear();
    }

    /**
     * Send an RVI request packet.
     *
     * @param dlinkPacket the dlink packet
     */
    void sendPacket(DlinkPacket dlinkPacket) {
        if (dlinkPacket == null) return;

        Log.d(TAG, Util.getMethodName() + ": " + dlinkPacket.getClass().toString());

//        RemoteConnectionInterface remoteConnection = selectConnectedRemoteConnection();
//
//        if (remoteConnection == null) return; // TODO: Implement a cache to send out stuff after a connection has been established
//
//        remoteConnection.sendRviRequest(dlinkPacket);

        if (mRemoteConnection == null) {
            if (mListener != null) mListener.onRVIDidFailToSendPacket(new Error("Interface not selected"));
        } else if (!mRemoteConnection.isConfigured()) {
            if (mListener != null) mListener.onRVIDidFailToSendPacket(new Error("Interface not configured"));
        } else if (!mRemoteConnection.isConnected()) {
            if (mListener != null) mListener.onRVIDidFailToSendPacket(new Error("Interface not connected"));
        } else {
            mRemoteConnection.sendRviRequest(dlinkPacket);
        }
    }

//    private RemoteConnectionInterface selectConnectedRemoteConnection() {
//        if (mDirectServerConnection.isConfigured() && mDirectServerConnection.isConnected())
//            return mDirectServerConnection;
//        if (mBluetoothConnection.isConfigured() && mBluetoothConnection.isConnected())
//            return mBluetoothConnection;
//
//        return null;
//    }

    private RemoteConnectionInterface selectConfiguredRemoteConnection(ConnectionType type) { // TODO: This is going to be buggy if a connection is enabled but not connected; the other connections won't have connected
        RemoteConnectionInterface remoteConnectionInterface;                                  // TODO: Rewrite better 'choosing' code

        if (type == ConnectionType.SERVER)
            remoteConnectionInterface = mDirectServerConnection;
        else if (type == ConnectionType.BLUETOOTH)
            remoteConnectionInterface = mBluetoothConnection;
        else if (type == ConnectionType.GLOBAL && mDirectServerConnection.isConfigured())
            remoteConnectionInterface = mDirectServerConnection;
        else
            remoteConnectionInterface = mBluetoothConnection;

        if (!remoteConnectionInterface.isConfigured()) {
            if (mListener != null) mListener.onRVIDidFailToConnect(new Error("Interface not configured"));
            return null;
        }

        return remoteConnectionInterface;
    }

//    private void closeConnections(ConnectionType type) {
//        if (type == ConnectionType.GLOBAL || type == ConnectionType.SERVER)
//            mDirectServerConnection.disconnect(RemoteConnection.AppInitiatedDisconnectionTrigger.get());
//        if (type == ConnectionType.GLOBAL || type == ConnectionType.BLUETOOTH)
//            mBluetoothConnection.disconnect(RemoteConnection.AppInitiatedDisconnectionTrigger.get());
//    }

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
        mDirectServerConnection.setServerPort(serverPort);
    }

    /**
     * Sets the trusted server certificate of the remote RVI node, when using a TCP/IP link to interface with a remote node.
     *
     * @param clientKeyStore the server certificate key store
     * @param serverKeyStore the server certificate key store
     */
    void setKeyStores(KeyStore serverKeyStore, KeyStore clientKeyStore, String clientKeyStorePassword) {
        mDirectServerConnection.setServerKeyStore(serverKeyStore);
        mDirectServerConnection.setClientKeyStore(clientKeyStore);
        mDirectServerConnection.setClientKeyStorePassword(clientKeyStorePassword);
    }

    /**
     * Sets the device address of the remote Bluetooth receiver on the remote RVI node, when using a Bluetooth link to interface with a remote node.
     *
     * @param deviceAddress the Bluetooth device address
     */
    void setBluetoothDeviceAddress(String deviceAddress) {
        mBluetoothConnection.setDeviceAddress(deviceAddress);
    }

    /**
     * Sets the Bluetooth service record identifier of the remote RVI node, when using a Bluetooth link to interface with a remote node.
     *
     * @param serviceRecord the service record identifier
     */
    void setBluetoothServiceRecord(UUID serviceRecord) {
        mBluetoothConnection.setServiceRecord(serviceRecord);
    }

    /**
     * Sets the Bluetooth channel of the remote RVI node, when using a Bluetooth link to interface with a remote node.
     *
     * @param channel the channel
     */
    void setBluetoothChannel(Integer channel) {
        /*RemoteConnectionManager.ourInstance.*/mBluetoothConnection.setChannel(channel);
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
