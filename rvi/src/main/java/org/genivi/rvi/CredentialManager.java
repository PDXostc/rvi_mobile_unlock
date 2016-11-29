package org.genivi.rvi;
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

import java.util.ArrayList;
import java.util.Calendar;

class CredentialManager {
    private final static String TAG = "RVI/CredentialManager__";

    private static CredentialManager ourInstance = new CredentialManager();

    private static Long nextRevalidationRemoteCredentials = 0L;

    private static Long nextRevalidationLocalCredentials = 0L;

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

    static Boolean remoteCredentialsRevalidationNeeded() {
        if (CredentialManager.nextRevalidationRemoteCredentials == 0L) return true;

        Long currentTime = System.currentTimeMillis() / 1000;

        if (currentTime > CredentialManager.nextRevalidationRemoteCredentials) {
            CredentialManager.nextRevalidationRemoteCredentials = 0L;
            return true;
        }

        return false;
    }

    static void updateRemoteCredentialsRevalidationTime(Long time) {
        if (time < CredentialManager.nextRevalidationRemoteCredentials || CredentialManager.nextRevalidationRemoteCredentials == 0L && time != 0L)
            CredentialManager.nextRevalidationRemoteCredentials = time;
    }

    static void clearRemoteCredentialsRevalidationTime() {
        CredentialManager.nextRevalidationRemoteCredentials = 0L;
    }

    static Boolean localCredentialsRevalidationNeeded() {
        if (CredentialManager.nextRevalidationLocalCredentials == 0L) return true;

        Long currentTime = System.currentTimeMillis() / 1000;

        if (currentTime > CredentialManager.nextRevalidationLocalCredentials) {
            CredentialManager.nextRevalidationLocalCredentials = 0L;
            return true;
        }

        return false;
    }

    static void updateLocalCredentialsRevalidationTime(Long time) {
        if (time < CredentialManager.nextRevalidationLocalCredentials || CredentialManager.nextRevalidationLocalCredentials == 0L && time != 0L)
            CredentialManager.nextRevalidationLocalCredentials = time;
    }

    static void clearLocalCredentialsRevalidationTime() {
        CredentialManager.nextRevalidationLocalCredentials = 0L;
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
