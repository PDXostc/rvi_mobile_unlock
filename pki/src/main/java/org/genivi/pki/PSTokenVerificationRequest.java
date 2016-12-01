package org.genivi.pki;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2016 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    PSTokenVerificationRequest.java
 * Project: PKI
 *
 * Created by Lilli Szafranski on 10/13/16.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import com.google.gson.annotations.SerializedName;

/**
 * Object that represents the body of a token verification request sent to the provisioning server.
 */
public class PSTokenVerificationRequest extends ProvisioningServerRequest
{
    private transient String mToken = "";

    private transient String mCertificateId = "";

    @SerializedName("jwt")
    private String jwt = null;

    /**
     * Constructor.
     */
    public PSTokenVerificationRequest() {
        setType(Type.TOKEN_VERIFICATION);
    }

    /**
     * Constructor.
     * @param token The one-time token sent to the device through email, text, etc.
     * @param certificateId A certificate id sent with the one-time token.
     */
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

    /**
     * Gets the one-time token sent to the device through email, text, etc.
     * @return The one-time token sent to the device through email, text, etc.
     */
    public String getToken() {
        return mToken;
    }

    /**
     * Gets the certificate id sent with the one-time token.
     * @return The certificate id sent with the one-time token.
     */
    public String getCertificateId() {
        return mCertificateId;
    }
}
