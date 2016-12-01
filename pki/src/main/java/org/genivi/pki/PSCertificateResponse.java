package org.genivi.pki;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2016 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    PSCertificateResponse.java
 * Project: PKI
 *
 * Created by Lilli Szafranski on 10/13/16.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import com.google.gson.annotations.SerializedName;

import java.security.KeyStore;
import java.util.ArrayList;

/**
 * Object that represents the body of a certificate response from the provisioning server.
 */
public class PSCertificateResponse extends ProvisioningServerResponse
{
    @SerializedName("signed_certificate")
    private String mDeviceCertificate;

    @SerializedName("server_certificate")
    private String mServerCertificate;

    @SerializedName("jwt")
    private ArrayList<String> mJwtPrivileges;

    private transient KeyStore mServerKeyStore;

    private transient KeyStore mDeviceKeyStore;

    /**
     * Constructor.
     */
    public PSCertificateResponse() {
    }

    String getDeviceCertificate() {
        return mDeviceCertificate;
    }

    String getServerCertificate() {
        return mServerCertificate;
    }

    /**
     * Gets a list of any server-signed JWT privileges sent to the device from the provisioning server.
     * @return A list of any server-signed JWT privileges sent to the device from the provisioning server.
     */
    public ArrayList<String> getJwtPrivileges() {
        return mJwtPrivileges;
    }

    /**
     * Gets the keystore containing the server's server-self-signed certificate.
     * @return A keystore containing the server's server-self-signed certificate.
     */
    public KeyStore getServerKeyStore() {
        return mServerKeyStore;
    }

    void setServerKeyStore(KeyStore serverKeyStore) {
        mServerKeyStore = serverKeyStore;
    }

    /**
     * Gets the keystore containing the device's public/private key-pair and server-signed certificate.
     * @return A keystore containing the device's public/private key-pair and server-signed certificate.
     */
    public KeyStore getDeviceKeyStore() {
        return mDeviceKeyStore;
    }

    void setDeviceKeyStore(KeyStore deviceKeyStore) {
        mDeviceKeyStore = deviceKeyStore;
    }
}
