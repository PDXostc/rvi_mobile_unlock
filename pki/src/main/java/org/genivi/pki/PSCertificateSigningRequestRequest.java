package org.genivi.pki;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2016 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    PSCertificateSigningRequestRequest.java
 * Project: PKI
 *
 * Created by Lilli Szafranski on 10/13/16.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import com.google.gson.annotations.SerializedName;

/**
 * Object that represents the body of a certificate signing request request sent to the provisioning server.
 */
public class PSCertificateSigningRequestRequest extends ProvisioningServerRequest
{
    @SerializedName("certificate_signing_request")
    private String mCertificateSigningRequest;

    /**
     * Constructor.
     */
    public PSCertificateSigningRequestRequest() {
        setType(Type.CERTIFICATE_SIGNING_REQUEST);
    }

    /**
     * Constructor.
     * @param certificateSigningRequest The PEM-encoded certificate signing request.
     */
    public PSCertificateSigningRequestRequest(String certificateSigningRequest) {
        setType(Type.CERTIFICATE_SIGNING_REQUEST);

        mCertificateSigningRequest = certificateSigningRequest;
    }

    /**
     * Gets the PEM-encoded certificate signing request.
     * @return The PEM-encoded certificate signing request.
     */
    public String getCertificateSigningRequest() {
        return mCertificateSigningRequest;
    }
}
