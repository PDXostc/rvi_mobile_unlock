package com.jaguarlandrover.pki;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2016 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    ProvisioningServerRequest.java
 * Project: UnlockDemo
 *
 * Created by Lilli Szafranski on 10/13/16.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import com.google.gson.annotations.SerializedName;

public class ProvisioningServerRequest {
    private final static String TAG = "PKI/ProvServerRequest__";

    @SerializedName("type")
    private String mType = "undefined";

    public enum Type {
        UNDEFINED,
        CERTIFICATE_SIGNING_REQUEST,
        TOKEN_VERIFICATION,

    }

    public ProvisioningServerRequest() {
    }

    public ProvisioningServerRequest.Type getType() {
        switch (mType) {
            case "token_verification":
                return Type.TOKEN_VERIFICATION;
            case "certificate_signing_request":
                return Type.CERTIFICATE_SIGNING_REQUEST;
        }

        return Type.UNDEFINED;
    }

    protected void setType(ProvisioningServerRequest.Type type) {
        switch (type) {
            case UNDEFINED:
                mType = "undefined";
                break;
            case TOKEN_VERIFICATION:
                mType = "token_verification";
                break;
            case CERTIFICATE_SIGNING_REQUEST:
                mType = "certificate_signing_request";
                break;
        }
    }
}


