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

import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;

/**
 * The remote connection manager of the RVI node.
 */
public class RemoteConnectionManager
{
    private final static String TAG = "RVI/RemoteCnnctnManager";

    private ServerConnection                mDirectServerConnection;
    private RemoteConnectionInterface       mRemoteConnection;

    private DlinkPacketParser               mDataParser;

    private RemoteConnectionManagerListener mListener;

    RemoteConnectionManager() {
        mDataParser = new DlinkPacketParser(new DlinkPacketParser.DlinkPacketParserListener()
        {
            @Override
            public void onPacketParsed(DlinkPacket packet) {
                Log.d(TAG, "RCVRVI(" + packet.getType() + "): " + packet.toJsonString());

                if (mListener != null) mListener.onRVIDidReceivePacket(packet);
            }

            @Override
            public void onPacketFailedToParse(DlinkPacket packet, Throwable error) {
                if (mListener != null) mListener.onRVIDidFailToReceivePacket(packet, error);
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
                if (mListener != null) mListener.onRVIDidFailToSendPacket(null, error);
            }
        };

        mDirectServerConnection = new ServerConnection();
        mDirectServerConnection.setRemoteConnectionListener(connectionListener);
    }

    /**
     * Connect the local RVI node to the remote RVI node.
     */
    void connect() {
        if (!mDirectServerConnection.isConfigured()) {
            if (mListener != null) mListener.onRVIDidFailToConnect(new Error("Interface not configured"));
            return;
        }

        mRemoteConnection = mDirectServerConnection;

        mRemoteConnection.connect();
    }

    /**
     * Disconnect the local RVI node from the remote RVI node
     */
    void disconnect() {
        if (mRemoteConnection != null) mRemoteConnection.disconnect(null);
        else if (mListener != null) mListener.onRVIDidDisconnect(null);

        mDataParser.clear();
    }

    /**
     * Send an RVI request packet.
     *
     * @param dlinkPacket the dlink packet
     */
    void sendPacket(DlinkPacket dlinkPacket) {
        if (dlinkPacket == null) return;

        Log.d(TAG, "SNDRVI(" + dlinkPacket.getType() + "): " + dlinkPacket.toJsonString());

        if (mRemoteConnection == null) {
            if (mListener != null) mListener.onRVIDidFailToSendPacket(dlinkPacket, new Error("Interface not selected"));
        } else if (!mRemoteConnection.isConfigured()) {
            if (mListener != null) mListener.onRVIDidFailToSendPacket(dlinkPacket, new Error("Interface not configured"));
        } else if (!mRemoteConnection.isConnected()) {
            if (mListener != null) mListener.onRVIDidFailToSendPacket(dlinkPacket, new Error("Interface not connected"));
        } else {
            mRemoteConnection.sendRviRequest(dlinkPacket);
        }
    }

    void setKeyStores(KeyStore serverKeyStore, KeyStore deviceKeyStore, String deviceKeyStorePassword) {
        mDirectServerConnection.setServerKeyStore(serverKeyStore);
        mDirectServerConnection.setLocalDeviceKeyStore(deviceKeyStore);
        mDirectServerConnection.setLocalDeviceKeyStorePassword(deviceKeyStorePassword);
    }

    /**
     * Sets the server url to the remote RVI node, when using a TCP/IP link to interface with a remote node.
     *
     * @param serverUrl the server url
     */
    void setServerUrl(String serverUrl) {
        mDirectServerConnection.setServerUrl(serverUrl);
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
     * Sets the remote connection manager listener.
     *
     * @param listener the listener
     */
    void setListener(RemoteConnectionManagerListener listener) {
        mListener = listener;
    }

    Certificate getRemoteDeviceCertificate() {
        return mRemoteConnection.getRemoteDeviceCertificate();
    }

    Certificate getLocalDeviceCertificate() {
        return mRemoteConnection.getLocalDeviceCertificate();
    }

    Certificate getServerCertificate() {
        return mRemoteConnection.getServerCertificate();
    }
}
