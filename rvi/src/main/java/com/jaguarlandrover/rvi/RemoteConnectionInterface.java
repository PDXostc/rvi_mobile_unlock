package com.jaguarlandrover.rvi;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2015 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    RemoteConnectionInterface.java
 * Project: RVI SDK
 *
 * Created by Lilli Szafranski on 5/19/15.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * The Remote connection interface.
 */
interface RemoteConnectionInterface
{
    /**
     * Send an rvi request.
     *
     * @param dlinkPacket the dlink packet
     */
    void sendRviRequest(DlinkPacket dlinkPacket);

    /**
     * Is the interface configured.
     *
     * @return the boolean
     */
    boolean isConfigured();

//    /**
//     * Is the interface available.
//     *
//     * @return the boolean
//     */
//    boolean isAvailable();

    /**
     * Is the interface connected.
     *
     * @return the boolean
     */
    boolean isConnected();

    /**
     * Connect the interface.
     */
    void connect();

    /**
     * Disconnect the interface.
     */
    void disconnect(Throwable trigger);

    /**
     * Sets remote connection listener.
     *
     * @param remoteConnectionListener the remote connection listener
     */
    void setRemoteConnectionListener(RemoteConnectionListener remoteConnectionListener); // TODO: Probably bad architecture to expect interface implementations to correctly set and use an
                                                                                         // TODO, cont'd: instance of the RemoteConnectionListener. Not sure what the best Java paradigm would be in this case

    /**
     * The remote connection listener interface.
     */
    interface RemoteConnectionListener
    {
        /**
         * Callback method for when the remote connection did connect.
         */
        void onRemoteConnectionDidConnect();

        /**
         * Callback method for when the remote connection did disconnect.
         */
        void onRemoteConnectionDidDisconnect(Throwable trigger);

        /**
         * Callback method for when the remote connection did fail to connect.
         *
         * @param error the error
         */
        void onRemoteConnectionDidFailToConnect(Throwable error);

        /**
         * Callback method for when the remote connection did receive data.
         *
         * @param data the data that was received
         */
        void onRemoteConnectionDidReceiveData(String data);

        /**
         * Callback method for when the remote connection did send data to the RVI node.
         */
        void onDidSendDataToRemoteConnection(DlinkPacket packet);

        /**
         * Callback method for when the the remote connection did fail to send data to the RVI node.
         *
         * @param error the error
         */
        void onDidFailToSendDataToRemoteConnection(Throwable error);
    }
}
