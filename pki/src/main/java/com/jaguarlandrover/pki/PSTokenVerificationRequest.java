package com.jaguarlandrover.pki;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2016 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    PSTokenVerificationRequest.java
 * Project: UnlockDemo
 *
 * Created by Lilli Szafranski on 10/13/16.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import com.google.gson.annotations.SerializedName;

public class PSTokenVerificationRequest extends ProvisioningServerRequest
{
    private transient String mToken = "";

    private transient String mCertificateId = "";

    @SerializedName("jwt")
    private String jwt = null;

    public PSTokenVerificationRequest() {
        setType(Type.TOKEN_VERIFICATION);
    }

    public PSTokenVerificationRequest(String token, String certificateId) {
        setType(Type.TOKEN_VERIFICATION);

        mToken = token;
        mCertificateId = certificateId;
    }

    String getJwt() {
        return jwt;
    }

    void setJwt(String jwt) {
        this.jwt = jwt;
    }

    String getJwtBody() {
        return "{ \"token\": \"" + getToken() + "\", \"certificate_id\": \"" + getCertificateId() + "\"}";
    }

    public String getToken() {
        return mToken;
    }

    public String getCertificateId() {
        return mCertificateId;
    }
}
