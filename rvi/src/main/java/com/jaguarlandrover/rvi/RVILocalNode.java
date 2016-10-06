package com.jaguarlandrover.rvi;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2016 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    RVILocalNode.java
 * Project: UnlockDemo
 *
 * Created by Lilli Szafranski on 10/4/16.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.xml.transform.sax.SAXTransformerFactory;

import static android.content.Context.MODE_PRIVATE;

public class RVILocalNode {
    private final static String TAG = "UnlockDemo:RVILocalNode";

    private final static String SAVED_CREDENTIALS_FILE = "org.genivi.rvi.saved_credentials";

    private static RVILocalNode ourInstance = new RVILocalNode();

    private static Boolean localNodeStarted = false;

    private static KeyStore serverKeyStore = null;
    private static KeyStore deviceKeyStore = null;
    private static String   deviceKeyStorePassword = null;

    private static ArrayList<Credential> credentialsList = null;

    private static RVILocalNode getInstance() {
        return ourInstance;
    }

    private RVILocalNode() {
    }

    public static void start(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context parameter must not be null.");
        }

        localNodeStarted = true;

        loadCredentials(context);
    }

    private static void checkIfReady() {
        if (!localNodeStarted) {
            throw new RuntimeException("The local RVI node has not yet been started.");
        }
    }

    static KeyStore getServerKeyStore() {
        return serverKeyStore;
    }

    public static void setServerKeyStore(KeyStore serverKeyStore) {
        checkIfReady();

        RVILocalNode.serverKeyStore = serverKeyStore;
    }

    static KeyStore getDeviceKeyStore() {
        return deviceKeyStore;
    }

    public static void setDeviceKeyStore(KeyStore deviceKeyStore) {
        checkIfReady();

        RVILocalNode.deviceKeyStore = deviceKeyStore;
    }

    static String getDeviceKeyStorePassword() {
        return deviceKeyStorePassword;
    }

    public static void setDeviceKeyStorePassword(String deviceKeyStorePassword) {
        checkIfReady();

        RVILocalNode.deviceKeyStorePassword = deviceKeyStorePassword;
    }

    private static void saveCredentials(Context context) {
        CredentialManager.saveCredentials(context);
    }

    private static void loadCredentials(Context context) {
        CredentialManager.loadCredentials(context);
    }

    static ArrayList<String> getCredentials() {
        checkIfReady();

        return CredentialManager.getCredentials();
    }

    public static void setCredentials(Context context, ArrayList<String> credentialStrings) {
        checkIfReady();

        CredentialManager.setCredentials(context, credentialStrings);
    }

    public static void removeAllCredentials(Context context) {
        checkIfReady();

        CredentialManager.removeAllCredentials(context);
    }

    private final static String SHARED_PREFS_STRING         = "com.rvisdk.settings";
    private final static String LOCAL_SERVICE_PREFIX_STRING = "localServicePrefix";

    // TODO: Test and verify this function
    private static String uuidB58String() {
        UUID uuid = UUID.randomUUID();
        String b64Str;

        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        b64Str = Base64.encodeToString(bb.array(), Base64.DEFAULT);
        b64Str = b64Str.split("=")[0];

        b64Str = b64Str.replace('+', 'P');
        b64Str = b64Str.replace('/', 'S'); /* Reduces likelihood of uniqueness but stops non-alphanumeric characters from screwing up any urls or anything */

        return b64Str;
    }

    /**
     * Gets the prefix of the local RVI node
     *
     * @param context the application context
     * @return the local prefix
     */
    public static String getLocalNodeIdentifier(Context context) { // TODO: There is no easy way to reset this once it's stored, is there? Maybe an app version check?
        SharedPreferences sharedPrefs = context.getSharedPreferences(SHARED_PREFS_STRING, MODE_PRIVATE);
        String localServicePrefix;

        if ((localServicePrefix = sharedPrefs.getString(LOCAL_SERVICE_PREFIX_STRING, null)) == null)
            localServicePrefix = "android/" + uuidB58String();

        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(LOCAL_SERVICE_PREFIX_STRING, localServicePrefix);
        editor.apply();

        return localServicePrefix;
    }
}


//    /**
//     * Sets the certs of the remote RVI node, when using a TCP/IP link to interface with a remote node.
//     *
//     * @param serverKeyStore the KeyStore object that contains your server's self-signed certificate that the TLS connection should accept.
//     *                 To make this KeyStore object, use BouncyCastle (http://www.bouncycastle.org/download/bcprov-jdk15on-146.jar), and
//     *                 this command-line command:
//     *                 $ keytool -import -v -trustcacerts -alias 0 \
//     *                 -file [PATH_TO_SELF_CERT.PEM] \
//     *                 -keystore [PATH_TO_KEYSTORE] \
//     *                 -storetype BKS \
//     *                 -provider org.bouncycastle.jce.provider.BouncyCastleProvider \
//     *                 -providerpath [PATH_TO_bcprov-jdk15on-146.jar] \
//     *                 -storepass [STOREPASS]
//     * @param clientKeyStore the KeyStore object that contains your client's self-signed certificate that the TLS connection sends to the server.
//     *                       // TODO: openssl pkcs12 -export -in insecure_device_cert.crt -inkey insecure_device_key.pem -out client.p12 -name "client-certs"
//     * @param clientKeyStorePassword the password of the client key store
//     */
