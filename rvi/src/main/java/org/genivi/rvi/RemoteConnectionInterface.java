package org.genivi.rvi;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2015 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    RemoteConnectionInterface.java
 * Project: RVI
 *
 * Created by Lilli Szafranski on 5/19/15.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import java.security.KeyStore;
import java.security.cert.Certificate;

/**
 * The Remote connection interface.
 */
interface RemoteConnectionInterface
{
    /**
     * Send an rvi request.
     *
     * @param dlinkPacket The dlink packet.
     */
    void sendRviRequest(DlinkPacket dlinkPacket);

    /**
     * Is the interface configured.
     *
     * @return The boolean.
     */
    boolean isConfigured();

    /**
     * Is the interface connected.
     *
     * @return The boolean.
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
     * @param remoteConnectionListener The remote connection listener.
     */
    void setRemoteConnectionListener(RemoteConnectionListener remoteConnectionListener);


    /**
     * Sets the key stores used by the connection.
     *
     * @param serverKeyStore The keystore containing the server certificate.
     */
    void setServerKeyStore(KeyStore serverKeyStore);


    /**
     * Sets the key stores used by the connection.
     *
     * @param localDeviceKeyStore The keystore containing the device certificate.
     */
    void setLocalDeviceKeyStore(KeyStore localDeviceKeyStore);

    /**
     * Sets the key stores used by the connection.
     *
     * @param localDeviceKeyStorePassword The password for the keystore containing the device certificate.
     */
    void setLocalDeviceKeyStorePassword(String localDeviceKeyStorePassword);

    /**
     * Gets the remote device certificate used in the TLS connection.
     *
     * @return The remote device certificate used in the TLS connection.
     */
    Certificate getRemoteDeviceCertificate();

    /**
     * Gets the local device certificate used in the TLS connection.
     *
     * @return The local device certificate used in the TLS connection.
     */
    Certificate getLocalDeviceCertificate();

    /**
     * Gets the server certificate used in the TLS connection.
     *
     * @return The server certificate used in the TLS connection.
     */
    Certificate getServerCertificate();

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
         * @param error The error.
         */
        void onDidFailToSendDataToRemoteConnection(Throwable error);
    }
}
