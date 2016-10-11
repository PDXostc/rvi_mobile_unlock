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
import android.widget.ArrayAdapter;

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
    private final static String TAG = "RVI/CredentialManager__";

    private static CredentialManager ourInstance = new CredentialManager();

    private CredentialManager() {
    }

    static ArrayList<String> toCredentialStringArray(ArrayList<Credential> credentialObjects) {
        ArrayList<String> credentialStrings = new ArrayList<>();

        if (credentialObjects != null)
            for (Credential credential : credentialObjects)
                credentialStrings.add(credential.getJwt());

        return credentialStrings;
    }

    static ArrayList<Credential> fromCredentialStringArray(ArrayList<String> credentialStrings) {
        ArrayList<Credential> credentialObjects = new ArrayList<>();

        if (credentialStrings != null)
            for (String credential : credentialStrings)
                credentialObjects.add(new Credential(credential));

        return credentialObjects;
    }

//    static void validateCredentials(KeyStore keyStore, ArrayList<Credential> credentialsList) {
//        if (keyStore == null) return;
//
//        try {
//            Enumeration<String> aliases = keyStore.aliases();
//
//            while (aliases.hasMoreElements()) {
//                String alias = aliases.nextElement();
//
//                KeyStore.TrustedCertificateEntry entry = (KeyStore.TrustedCertificateEntry) keyStore.getEntry(alias, null);
//                X509Certificate certificate = (X509Certificate) entry.getTrustedCertificate();
//
//                Key key = certificate.getPublicKey();
//
//                for (Credential credential : credentialsList)
//                    credential.isValid(key);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
