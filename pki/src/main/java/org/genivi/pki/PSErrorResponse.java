package org.genivi.pki;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2016 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    PSErrorResponse.java
 * Project: PKI
 *
 * Created by Lilli Szafranski on 10/13/16.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import com.google.gson.annotations.SerializedName;

/**
 * Object that represents the body of an error response from the provisioning server.
 */
public class PSErrorResponse extends ProvisioningServerResponse
{
    @SerializedName("reason")
    private String mReason = "unknown";

    /**
     * Constructor.
     */
    public PSErrorResponse() {
    }

    /**
     * Constructor.
     * @param reason The reason for the error.
     */
    PSErrorResponse(String reason) {
        setStatus("error");

        mReason = reason;
    }

    /**
     * Gets the reason for the error.
     * @return The reason for the error.
     */
    public String getReason() {
        return mReason;
    }
}
