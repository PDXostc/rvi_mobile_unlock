package com.jaguarlandrover.rvi;

/**
 * The interface Remote connection manager listener.
 */
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2015 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    RemoteConnectionManagerListener.java
 * Project: RVI SDK
 *
 * Created by Lilli Szafranski on 6/30/15.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
interface RemoteConnectionManagerListener
{
    /**
     * On RVI did connect.
     */
    void onRVIDidConnect();

    /**
     * On RVI did fail to connect.
     *
     * @param error the error
     */
    void onRVIDidFailToConnect(Error error);

    /**
     * On RVI did disconnect.
     */
    void onRVIDidDisconnect();

    /**
     * On RVI did receive packet.
     *
     * @param packet the packet
     */
    void onRVIDidReceivePacket(DlinkPacket packet);

    /**
     * On RVI did send packet.
     */
    void onRVIDidSendPacket();

    /**
     * On RVI did fail to send packet.
     *
     * @param error the error
     */
    void onRVIDidFailToSendPacket(Error error);
}
