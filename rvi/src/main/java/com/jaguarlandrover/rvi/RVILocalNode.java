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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.util.ArrayList;

public class RVILocalNode {
    private final static String TAG = "UnlockDemo:RVILocalNode";

    private final static String SAVED_CREDENTIALS_FILE = "org.genivi.rvi.saved_credentials";

    private static RVILocalNode ourInstance        = new RVILocalNode();
//    private static Context      applicationContext = null;

    private static Boolean      localNodeStarted   = false;

    private static KeyStore serverKeyStore = null;
    private static KeyStore deviceKeyStore = null;
    private static String   deviceKeyStorePassword = null;

    private static ArrayList<String> credentialsList = null;

    private static RVILocalNode getInstance() {
        return ourInstance;
    }

    private RVILocalNode() {
    }

    public static void start(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context parameter must not be null.");
        }

//        applicationContext = context;
        localNodeStarted = true;

        loadCredentials(context);
    }

    private static void checkIfReady() {
        if (/*applicationContext == null || */!localNodeStarted) {
            throw new RuntimeException("The local RVI node has not yet been started.");
        }
    }

    private static void saveCredentials(Context context) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(credentialsList);

        try {
            FileOutputStream fileOutputStream = context.openFileOutput(SAVED_CREDENTIALS_FILE, Context.MODE_PRIVATE);
            fileOutputStream.write(jsonString.getBytes());
            fileOutputStream.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private static void loadCredentials(Context context) {
        Gson gson = new Gson();

        File file = context.getFileStreamPath(SAVED_CREDENTIALS_FILE);
        if (file == null || !file.exists()) {
            credentialsList = new ArrayList<>();

        } else {
            try {
                FileInputStream fileInputStream = context.openFileInput(SAVED_CREDENTIALS_FILE);
                int c;
                String jsonString = "";

                while ((c = fileInputStream.read()) != -1) {
                    jsonString = jsonString + Character.toString((char)c);
                }

                credentialsList = gson.fromJson(jsonString, new TypeToken<ArrayList<String>>(){}.getType());

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }

    public static void addCredentials(Context context, ArrayList<String> newCredentialsList) {
        checkIfReady();

        if (newCredentialsList == null) return;

        for (String credentials : newCredentialsList)
            credentialsList.add(credentials);

        saveCredentials(context);
    }

    static ArrayList<String>getCredentials() {
        checkIfReady();

        return credentialsList;
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
}
