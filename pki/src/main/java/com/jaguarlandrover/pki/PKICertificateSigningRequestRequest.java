package com.jaguarlandrover.pki;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2016 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    CertificateSigningRequest.java
 * Project: UnlockDemo
 *
 * Created by Lilli Szafranski on 10/13/16.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import com.google.gson.annotations.SerializedName;

public class PKICertificateSigningRequestRequest extends PKIServerRequest {
    @SerializedName("certificate_signing_request")
    private String mCertificateSigningRequest;

    public PKICertificateSigningRequestRequest() {
        setType(Type.CERTIFICATE_SIGNING_REQUEST);
    }

    public PKICertificateSigningRequestRequest(String certificateSigningRequest) {
        setType(Type.CERTIFICATE_SIGNING_REQUEST);

        mCertificateSigningRequest = certificateSigningRequest;
    }

    public String getCertificateSigningRequest() {
        return mCertificateSigningRequest;
    }
}
