package org.genivi.pki;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2016 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    PKIManager.java
 * Project: UnlockDemo
 *
 * Created by Lilli Szafranski on 8/9/16.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import android.content.Context;

import java.security.KeyStore;
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
        void managerDidReceiveResponseFromServer(ProvisioningServerResponse response);
    }

    public interface CertificateSigningRequestGeneratorListener
    {
        void generateCertificateSigningRequestSucceeded(String certificateSigningRequest);

        void generateCertificateSigningRequestFailed(Throwable reason);
    }

    public static void generateKeyPairAndCertificateSigningRequest(Context context, CertificateSigningRequestGeneratorListener listener, Date startDate, Date endDate, String principalFormatterPattern, Object... principalFormatterArgs) {
        KeyStoreInterface.generateKeyPairAndCertificateSigningRequest(context, listener, startDate, endDate, principalFormatterPattern, principalFormatterArgs);
    }

    public static void sendCertificateSigningRequest(Context context, PKIManager.ProvisioningServerListener listener, String baseUrl, String requestUrl, ProvisioningServerRequest certificateSigningRequest) {
        BackendServerInterface.sendProvisioningServerRequest(context, listener, baseUrl, requestUrl, certificateSigningRequest);
    }

    public static void sendTokenVerificationRequest(Context context, PKIManager.ProvisioningServerListener listener, String baseUrl, String requestUrl, ProvisioningServerRequest tokenVerificationRequest) {
        BackendServerInterface.sendProvisioningServerRequest(context, listener, baseUrl, requestUrl, tokenVerificationRequest);
    }

    public static void deleteAllKeysAndCerts(Context context) {
        KeyStoreInterface.deleteAllKeysAndCerts(context);
    }

    public static void deleteServerCerts(Context context) {
        KeyStoreInterface.deleteServerCerts(context);
    }

    public static Boolean hasValidDeviceCert(Context context) {
        return KeyStoreInterface.hasValidDeviceCert(context);
    }

    public static Boolean hasValidServerCert(Context context) {
        return KeyStoreInterface.hasValidServerCert(context);
    }

    public static KeyStore getDeviceKeyStore(Context context) {
        return KeyStoreInterface.getDeviceKeyStore(context);
    }

    public static KeyStore getServerKeyStore(Context context) {
        return KeyStoreInterface.getServerKeyStore(context);
    }

    public static String getPublicKey(Context context) {
        return KeyStoreInterface.getPublicKey(context);
    }

    public static void printPemEncodedDeviceCertificate(Context context) {
        KeyStoreInterface.printPemEncodedDeviceCertificate(context);
    }

    public static void printPemEncodedServerCertificate(Context context) {
        KeyStoreInterface.printPemEncodedServerCertificate(context);
    }
}
