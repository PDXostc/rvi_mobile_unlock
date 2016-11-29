package com.jaguarlandrover.pki;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2016 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    PSCertificateResponse.java
 * Project: UnlockDemo
 *
 * Created by Lilli Szafranski on 10/13/16.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import com.google.gson.annotations.SerializedName;

import java.security.KeyStore;
import java.util.ArrayList;

public class PSCertificateResponse extends ProvisioningServerResponse
{
    @SerializedName("signed_certificate")
    private String mDeviceCertificate;

    @SerializedName("server_certificate")
    private String mServerCertificate;

    @SerializedName("jwt")
    private ArrayList<String> mJwtCredentials;

    private transient KeyStore mServerKeyStore;

    private transient KeyStore mDeviceKeyStore;

    public PSCertificateResponse() {
    }

    String getDeviceCertificate() {
        return mDeviceCertificate;
    }

    String getServerCertificate() {
        return mServerCertificate;
    }

    public ArrayList<String> getJwtCredentials() {
        return mJwtCredentials;
    }

    public KeyStore getServerKeyStore() {
        return mServerKeyStore;
    }

    void setServerKeyStore(KeyStore serverKeyStore) {
        mServerKeyStore = serverKeyStore;
    }

    public KeyStore getDeviceKeyStore() {
        return mDeviceKeyStore;
    }

    void setDeviceKeyStore(KeyStore deviceKeyStore) {
        mDeviceKeyStore = deviceKeyStore;
    }
}
