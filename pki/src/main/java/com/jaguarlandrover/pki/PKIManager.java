package com.jaguarlandrover.pki;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2016 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    ProvisioningServerInterface.java
 * Project: UnlockDemo
 *
 * Created by Lilli Szafranski on 8/9/16.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

//import java.net.URL;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;


public class PKIManager {
    private final static String TAG = "UnlockDemo:PKIManager";

    private static PKIManager ourInstance = new PKIManager();

    //private static Context applicationContext = null;

    private static PKIManagerState pkiManagerState = PKIManagerState.UNKNOWN;

    private static String pkiServerBaseUrl                      = null;
    private static String pkiServerCertificateSigningRequestUrl = null;
    private static String pkiServerTokenValidationUrl           = null;

    private PKIManager() {

    }

    //public static void load(Context context) { applicationContext = context; }

    public static PKIManagerState getState() { return pkiManagerState; }

    public static String getPkiServerBaseUrl() {
        return pkiServerBaseUrl;
    }

    public static Class<PKIManager> setPkiServerBaseUrl(String pkiServerBaseUrl) {
        PKIManager.pkiServerBaseUrl = pkiServerBaseUrl;

        return PKIManager.class;
    }

    public static String getPkiServerCertificateSigningRequestUrl() {
        return pkiServerCertificateSigningRequestUrl;
    }

    public static Class<PKIManager> setPkiServerCertificateSigningRequestUrl(String pkiServerCertificateSigningRequestUrl) {
        PKIManager.pkiServerCertificateSigningRequestUrl = pkiServerCertificateSigningRequestUrl;

        return PKIManager.class;
    }

    public static String getPkiServerTokenValidationUrl() {
        return pkiServerTokenValidationUrl;
    }

    public static Class<PKIManager> setPkiServerTokenValidationUrl(String pkiServerTokenValidationUrl) {
        PKIManager.pkiServerTokenValidationUrl = pkiServerTokenValidationUrl;

        return PKIManager.class;
    }

    /**
     * The PKI manager listener interface.
     */
    public interface ProvisioningServerListener
    {
        void certificateSigningRequestSuccessfullySent();

        void certificateSigningRequestSuccessfullyReceived();

        /**
         * Called when the manager receives server-signed server certificate, server-signed device certificate, and list of server-signed jwt
         * privileges from the provisioning server (containing the server's public key).
         */
        void managerDidReceiveServerSignedStuff(KeyStore serverCertificateKeyStore, KeyStore deviceCertificateKeyStore, String deviceKeyStorePassword, ArrayList<String> defaultPrivileges);
    }

    public interface CertificateSigningRequestGeneratorListener
    {
        void generateCertificateSigningRequestSucceeded(String certificateSigningRequest);

        void generateCertificateSigningRequestFailed(Throwable reason);
    }

    public static void generateKeyPairAndCertificateSigningRequest(Context context, CertificateSigningRequestGeneratorListener listener, Date startDate, Date endDate, String principalFormatterPattern, Object... principalFormatterArgs) {
        ManagerHelper.generateKeyPairAndCertificateSigningRequest(context, listener, startDate, endDate, principalFormatterPattern, principalFormatterArgs);
    }

    public static void sendCertificateSigningRequest(Context context, PKIManager.ProvisioningServerListener listener, String baseUrl, String requestUrl, String certificateSigningRequest, Boolean extraValidationExpected) {
        ManagerHelper.sendCertificateSigningRequest(context, listener, baseUrl, requestUrl, certificateSigningRequest, extraValidationExpected);
    }

    public static void sendTokenVerificationRequest(Context context, PKIManager.ProvisioningServerListener listener, String baseUrl, String requestUrl, String tokenVerificationString) {
        ManagerHelper.sendTokenVerificationRequest(context, listener, baseUrl, requestUrl, tokenVerificationString);
    }


}
