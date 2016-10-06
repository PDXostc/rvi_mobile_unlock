package com.jaguarlandrover.rvi;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2016 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    RVICredentialManager.java
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
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;

class CredentialManager {
    private final static String TAG = "UnlockDemo:RVICredentialManager";

    private static CredentialManager ourInstance = new CredentialManager();

    private final static String SAVED_CREDENTIALS_FILE = "org.genivi.rvi.saved_credentials";
    private static ArrayList<Credential> credentialsList = new ArrayList<>();

    private CredentialManager() {
    }

    private static ArrayList<String> toCredentialStringArray(ArrayList<Credential> credentialObjects) {
        ArrayList<String> credentialStrings = new ArrayList<>();

        if (credentialObjects != null)
            for (Credential credential : credentialObjects)
                credentialStrings.add(credential.getJwt());

        return credentialStrings;
    }

    private static ArrayList<Credential> fromCredentialStringArray(ArrayList<String> credentialStrings) {
        ArrayList<Credential> credentialObjects = new ArrayList<>();

        if (credentialStrings != null)
            for (String credential : credentialStrings)
                credentialObjects.add(new Credential(credential));

        return credentialObjects;
    }

    static void saveCredentials(Context context) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(toCredentialStringArray(credentialsList));

        try {
            FileOutputStream fileOutputStream = context.openFileOutput(SAVED_CREDENTIALS_FILE, Context.MODE_PRIVATE);
            fileOutputStream.write(jsonString.getBytes());
            fileOutputStream.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    static void loadCredentials(Context context) {
        Gson gson = new Gson();

        File file = context.getFileStreamPath(SAVED_CREDENTIALS_FILE);
        if (file != null && file.exists()) {
            try {
                FileInputStream fileInputStream = context.openFileInput(SAVED_CREDENTIALS_FILE);
                int c;
                String jsonString = "";

                while ((c = fileInputStream.read()) != -1) {
                    jsonString = jsonString + Character.toString((char)c);
                }

                // TODO: Handle all kinds of errors here
                credentialsList = fromCredentialStringArray((ArrayList<String>) gson.fromJson(jsonString, new TypeToken<ArrayList<String>>(){}.getType()));

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }

    static ArrayList<String> getCredentials() {
        return toCredentialStringArray(credentialsList);
    }

    static void setCredentials(Context context, ArrayList<String> credentialStrings) {
        credentialsList = fromCredentialStringArray(credentialStrings);

        saveCredentials(context);
    }

    static void removeAllCredentials(Context context) {
        credentialsList.clear();

        saveCredentials(context);
    }

    static void validateCredentials(KeyStore keyStore) {
        if (keyStore == null) return;

        try {
            Enumeration<String> aliases = keyStore.aliases();

            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();

                KeyStore.TrustedCertificateEntry entry = (KeyStore.TrustedCertificateEntry) keyStore.getEntry(alias, null);
                X509Certificate certificate = (X509Certificate) entry.getTrustedCertificate();

                Key key = certificate.getPublicKey();

                for (Credential credential : credentialsList)
                    credential.isValid(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
