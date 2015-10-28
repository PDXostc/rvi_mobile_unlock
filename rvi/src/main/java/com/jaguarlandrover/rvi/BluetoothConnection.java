package com.jaguarlandrover.rvi;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2015 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    BluetoothConnection.java
 * Project: RVI SDK
 *
 * Created by Lilli Szafranski on 5/19/15.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * The Bluetooth @RemoteConnectionInterface implementation
 */
class BluetoothConnection implements RemoteConnectionInterface
{
    private final static String TAG = "RVI:BluetoothConnection";

    private RemoteConnectionListener mRemoteConnectionListener;

    @Override
    public void sendRviRequest(DlinkPacket dlinkPacket) {
        return;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void connect() {
    }

    @Override
    public void disconnect() {
    }

    @Override
    public void setRemoteConnectionListener(RemoteConnectionListener remoteConnectionListener) {
        mRemoteConnectionListener = remoteConnectionListener;
    }
}
