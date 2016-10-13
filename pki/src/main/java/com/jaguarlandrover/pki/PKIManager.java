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

//import java.net.URL;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Date;


public class PKIManager
{
    private final static String TAG = "PKI/PKIManager_________";

    private PKIManager() {

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


        void managerDidReceiveResponseFromServer(PKIServerResponse response);
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

    public static void sendCertificateSigningRequest(Context context, PKIManager.ProvisioningServerListener listener, String baseUrl, String requestUrl, PKICertificateSigningRequestRequest certificateSigningRequest) {
        ProvisioningServerInterface.sendProvisioningServerRequest(context, listener, baseUrl, requestUrl, certificateSigningRequest);
    }

    public static void sendTokenVerificationRequest(Context context, PKIManager.ProvisioningServerListener listener, String baseUrl, String requestUrl, PKITokenVerificationRequest tokenVerificationRequest) {
        tokenVerificationRequest.setJwt(KeyStoreManager.createJwt(context, tokenVerificationRequest.getJwtBody()));
        ProvisioningServerInterface.sendProvisioningServerRequest(context, listener, baseUrl, requestUrl, tokenVerificationRequest);
    }

    public static void deleteAllKeysAndCerts(Context context) {
        KeyStoreManager.deleteAllKeysAndCerts(context);
    }

    public static void deleteServerCerts(Context context) {
        KeyStoreManager.deleteServerCerts(context);
    }

    public static Boolean hasValidSignedDeviceCert(Context context) {
        return KeyStoreManager.hasValidSignedDeviceCert(context);
    }

    public static Boolean hasValidSignedServerCert(Context context) {
        return KeyStoreManager.hasValidSignedServerCert(context);
    }

    public static KeyStore getDeviceKeyStore(Context context) {
        return KeyStoreManager.getDeviceKeyStore(context);
    }

    public static KeyStore getServerKeyStore(Context context) {
        return KeyStoreManager.getServerKeyStore(context);
    }

    public static String getPublicKey(Context context) {
        return KeyStoreManager.getPublicKey(context);
    }
}
