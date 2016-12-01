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
 * Project: PKI
 *
 * Created by Lilli Szafranski on 8/9/16.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import android.content.Context;

import java.security.KeyStore;
import java.util.Date;

/**
 * The main interface for doing all things PKI. This is a singleton class, encapsulated behind static methods.
 *
 * This module is more of a reference implementation versus a library that you must include in your project. It will do many of the things
 * that you will have to do to use RVI correctly, but much of this occurs out-of-band of RVI and is dependent on your particular provisioning
 * server set up. You may want to include different data in the API calls made to your server. You may want to use a different method of
 * verification than what is used here (a token sent to an email address embedded in the CSR principal). How you implement device provisioning
 * is up to your discretion.
 *
 * What this module provides:
 *      - Key pair and certificate signing request generation, with the certificate signing request containing the public key and signed by the private key
 *      - Sending a certificate signing request to a provisioning server
 *      - Optional two-factor verification through a one-time token sent to an email address or phone number associated with the device and signed by the
 *        device's private key
 *      - Receiving from the server the server-signed device certificate, the server-self-signed server certificate, and some server-signed JWT privileges
 *      - Storage of the device keys and certificate in an AndroidKeyStore and storage of the the server certificate in a Bouncy Castle (BKS/BC) key store
 *
 * This module communicates with a simple provisioning server using our own custom json RESTful API. You can use this code, but you will need to implement your own
 * provisioning server. You can have your provisioning server use the same API as ours does or use your own API. If you use your own API, you will have to change
 * this code or write your own.
 *
 * To use RVI, you are responsible for your own secure provisioning. In detail, you will need to generate a public and private key pair and an X509 certificate
 * signing request. Embed the device's public key in the certificate signing request. This public key is essentially this device's identity. Sign the certificate
 * signing request with the device's private key, which guarantees that the certificate containing the device's public key does, in fact, belong to that device,
 * as it was signed by the device's private key. We now know that the certificate and public key have not been tampered with.
 *
 * Send the certificate signing request to the provisioning server over a secure connection (e.g., https). In this case, the provisioning server should establish
 * trust through an existing well-known Trust Authority. This step is all done out-of-band of RVI, so it is up to the implementer to guarantee the security of the
 * connection used.
 *
 * Once the server receives the certificate singing request, it is now going to assume the role of root trust authority. The server should sign the device's
 * certificate signing request with its own private key. The server must also create its own certificate which contains the server's public key corresponding to
 * the private key used in signing the device's certificate. This server will need to generate its own certificate, containing the server's public key. The server's
 * certificate will be self-signed by the same private key used in all JWT privileges sent by the local node and received from the remote node.
 *
 * These are the steps that are covered in this module. Feel free to use some or all of the code to ensure you are doing this correctly.
 */
public class PKIManager
{
    private final static String TAG = "PKI/PKIManager_________";

    /**
     * Constructor.
     */
    private PKIManager() {
    }

    /**
     * The ProvisioningServerListener interface. This is the interface that defines the call-backs to the methods relating to communication with
     * the provisioning server.
     */
    public interface ProvisioningServerListener
    {
        /**
         * The PKIManager received a response from the Provisioning server.
         *
         * @param response The response.
         */
        void managerDidReceiveResponseFromServer(ProvisioningServerResponse response);
    }


    /**
     * The CertificateSigningRequestGeneratorListener interface. This is the interface that defines the call-backs to the methods relating to key-pair and
     * certificate signing request generation.
     */
    public interface CertificateSigningRequestGeneratorListener
    {
        /**
         * The key-pair and certificate signing request generation succeeded.
         *
         * @param certificateSigningRequest The PEM-encoded certificate signing request.
         */
        void generateCertificateSigningRequestSucceeded(String certificateSigningRequest);

        /**
         * The key-pair and certificate signing request generation failed.
         *
         * @param reason The reason.
         */
        void generateCertificateSigningRequestFailed(Throwable reason);
    }

    /**
     * Generates a public/private key-pair and a certificate signing request, with the certificate signing request containing the public key and signed by the private key.
     * @param context The current context.
     * @param listener The CertificateSigningRequestGeneratorListener.
     * @param keySize The size of the key-pair (e.g., 4096)
     * @param setEncryptionRequired If the keypair generation requires encryption. Indicates that this key pair must be encrypted at rest. This will protect the key pair
     *                              with the secure lock screen credential (e.g., password, PIN, or pattern).
     *
     *                              Note that this feature requires that the secure lock screen (e.g., password, PIN, pattern) is set up, otherwise key pair generation
     *                              will fail. Moreover, this key pair will be deleted when the secure lock screen is disabled or reset (e.g., by the user or a Device
     *                              Administrator). Finally, this key pair cannot be used until the user unlocks the secure lock screen after boot.
     * @param startDate Sets the start of the validity period for the self-signed certificate of the generated key pair.
     * @param endDate Sets the end of the validity period for the self-signed certificate of the generated key pair.
     * @param principalFormatterPattern A format pattern to be used as the X.509 Principal. (E.g., "CN=%s, O=Genivi, OU=%s, EMAILADDRESS=%s")
     * @param principalFormatterArgs The formatter args to be passed into the principalFormatterPatter.
     */
    public static void generateKeyPairAndCertificateSigningRequest(Context context, CertificateSigningRequestGeneratorListener listener, Integer keySize, Boolean setEncryptionRequired, Date startDate, Date endDate, String principalFormatterPattern, Object... principalFormatterArgs) {
        KeyStoreInterface.generateKeyPairAndCertificateSigningRequest(context, listener, keySize, setEncryptionRequired, startDate, endDate, principalFormatterPattern, principalFormatterArgs);
    }

    /**
     * Sends the certificate signing request to the provisioning server.
     * @param context The current context.
     * @param listener The ProvisioningServerListener.
     * @param baseUrl The base url of your provisioning server (e.g., "https://myprovisioningserver.com").
     * @param requestUrl The url endpoint for the certificate signing request request. May be different depending on whether you want to use two-factor verification. (E.g.,
     *                   "/csr_simple" or "/csr_with_one_time_token_verification")
     * @param certificateSigningRequest The body of the certificate signing request request containing the PEM-encoded certificate signing request.
     */
    public static void sendCertificateSigningRequest(Context context, PKIManager.ProvisioningServerListener listener, String baseUrl, String requestUrl, ProvisioningServerRequest certificateSigningRequest) {
        BackendServerInterface.sendProvisioningServerRequest(context, listener, baseUrl, requestUrl, certificateSigningRequest);
    }

    /**
     * Sends a request to the server containing a one-time verification token that was extracted from an email/sms/etc. sent out-of-band to the device.
     * @param context The current context.
     * @param listener The ProvisioningServerListener.
     * @param baseUrl The base url of your provisioning server (e.g., "https://myprovisioningserver.com").
     * @param requestUrl The url endpoint for the one-time token verification request. (E.g., "/one_time_token_verification")
     * @param tokenVerificationRequest The body of the one-time token verification request. Request contains the one-time token and a certificate id and is encoded in
     *                                 a JWT which has been signed by the device's private key.
     */
    public static void sendTokenVerificationRequest(Context context, PKIManager.ProvisioningServerListener listener, String baseUrl, String requestUrl, ProvisioningServerRequest tokenVerificationRequest) {
        BackendServerInterface.sendProvisioningServerRequest(context, listener, baseUrl, requestUrl, tokenVerificationRequest);
    }

    /**
     * Deletes all keys and certificates saved to the device by the PKI module.
     * @param context The current context.
     */
    public static void deleteAllKeysAndCerts(Context context) {
        KeyStoreInterface.deleteAllKeysAndCerts(context);
    }

    /**
     * Deletes just the server certs saved to the device by the PKI module.
     * @param context The current context.
     */
    public static void deleteServerCerts(Context context) {
        KeyStoreInterface.deleteServerCerts(context);
    }

    /**
     * Do we have a valid, server-signed device certificate and public/private key-pair.
     * @param context The current context.
     * @return True if we do, false if we don't.
     */
    public static Boolean hasValidDeviceCert(Context context) {
        return KeyStoreInterface.hasValidDeviceCert(context);
    }

    /**
     * Do we have a valid, server-signed server certificate and public/private key-pair.
     * @param context The current context.
     * @return True if we do, false if we don't.
     */
    public static Boolean hasValidServerCert(Context context) {
        return KeyStoreInterface.hasValidServerCert(context);
    }

    /**
     * Returns our valid, server-signed device certificate and public/private key-pair as an Android KeyStore.
     * @param context The current context.
     * @return Our valid, server-signed device certificate and public/private key-pair, returned as an Android KeyStore.
     */
    public static KeyStore getDeviceKeyStore(Context context) {
        return KeyStoreInterface.getDeviceKeyStore(context);
    }

    /**
     * Returns our valid, server-signed server certificate as an Android KeyStore.
     * @param context The current context.
     * @return Our valid, server-signed device certificate, returned as an Android KeyStore.
     */
    public static KeyStore getServerKeyStore(Context context) {
        return KeyStoreInterface.getServerKeyStore(context);
    }

    /**
     * Gets our public key.
     * @param context The current context.
     * @return Our public key.
     */
    public static String getPublicKey(Context context) {
        return KeyStoreInterface.getPublicKey(context);
    }

    /**
     * Convenience method to print the PEM-encoded device certificate to the debug log.
     * @param context The current context.
     */
    public static void printPemEncodedDeviceCertificate(Context context) {
        KeyStoreInterface.printPemEncodedDeviceCertificate(context);
    }

    /**
     * Convenience method to print the PEM-encoded server certificate to the debug log.
     * @param context The current context.
     */
    public static void printPemEncodedServerCertificate(Context context) {
        KeyStoreInterface.printPemEncodedServerCertificate(context);
    }
}
