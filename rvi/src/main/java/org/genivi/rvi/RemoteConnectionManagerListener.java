package org.genivi.rvi;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2015 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    RemoteConnectionManagerListener.java
 * Project: RVI
 *
 * Created by Lilli Szafranski on 6/30/15.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * The interface remote connection manager listener.
 */
interface RemoteConnectionManagerListener // TODO: Get rid of this middle man
{
    /**
     * On RVI did connect.
     */
    void onRVIDidConnect();

    /**
     * On RVI did disconnect.
     *
     * @param error The error.
     */
    void onRVIDidDisconnect(Throwable error);

    /**
     * On RVI did fail to connect.
     *
     * @param error The error.
     */
    void onRVIDidFailToConnect(Throwable error);

    /**
     * On RVI did receive packet.
     *
     * @param packet The packet.
     */
    void onRVIDidReceivePacket(DlinkPacket packet);

    /**
     * RVI did receive packet but there was an error with the packet.
     *
     * @param packet The packet.
     * @param error The error.
     */
    void onRVIDidFailToReceivePacket(DlinkPacket packet, Throwable error);

    /**
     * On RVI did send packet.
     *
     * @param packet The packet.
     */
    void onRVIDidSendPacket(DlinkPacket packet);

    /**
     * On RVI did fail to send packet.
     *
     * @param packet The packet.
     * @param error The error.
     */
    void onRVIDidFailToSendPacket(DlinkPacket packet, Throwable error);
}
