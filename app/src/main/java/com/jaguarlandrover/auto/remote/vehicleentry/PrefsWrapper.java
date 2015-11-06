package com.jaguarlandrover.auto.remote.vehicleentry;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2015 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    PrefsWrapper.java
 * Project: UnlockDemo
 *
 * Created by Lilli Szafranski on 11/4/15.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class PrefsWrapper
{
    private final static String TAG                             = "UnlockDemo:PrefsWrapper";

    private final static String NEW_CERTIFICATE_DATA_KEY        = "NEW_CERTIFICATE_DATA_KEY";
    private final static String CERTIFICATE_DATA_KEY            = "CERTIFICATE_DATA_KEY";
    private final static String NEW_USER_CREDENTIALS_KEY        = "NEW_USER_CREDENTIALS_KEY";
    private final static String USER_CREDENTIALS_KEY            = "USER_CREDENTIALS_KEY";
    private final static String NEW_REMOTE_CREDENTIALS_LIST_KEY = "NEW_REMOTE_CREDENTIALS_LIST_KEY";
    private final static String REMOTE_CREDENTIALS_LIST_KEY     = "REMOTE_CREDENTIALS_LIST_KEY";
    private final static String NEW_INVOKED_SERVICE_REPORT_KEY  = "NEW_INVOKED_SERVICE_REPORT_KEY";
    private final static String INVOKED_SERVICE_REPORT_KEY      = "INVOKED_SERVICE_REPORT_KEY";

    private static PrefsWrapper             ourInstance = new PrefsWrapper();
    private static SharedPreferences        prefs       = PreferenceManager.getDefaultSharedPreferences(UnlockApplication.getContext());
    private static SharedPreferences.Editor editor      = prefs.edit();
    private static Gson                     gson        = new Gson();

    public static PrefsWrapper getInstance() {
        return ourInstance;
    }

    private PrefsWrapper() {
    }

    public static Certificate getCertificate() {
        String certStr = prefs.getString(CERTIFICATE_DATA_KEY, null);

        if (certStr == null) return null;

        return gson.fromJson(certStr, Certificate.class);
    }

    public static void setCertificate(Certificate cert) {
        String certStr = gson.toJson(cert, Certificate.class);
        editor.putString(CERTIFICATE_DATA_KEY, certStr);
        editor.commit();

        PrefsWrapper.setThereIsNewCertificateData(true);
    }

    public static UserCredentials getUserCredentials() {
        String userStr = prefs.getString(USER_CREDENTIALS_KEY, null);

        if (userStr == null) return null;

        return gson.fromJson(userStr, UserCredentials.class);
    }

    public static void setUserCredentials(UserCredentials userCredentials) {
        String userStr = gson.toJson(userCredentials, UserCredentials.class);
        editor.putString(USER_CREDENTIALS_KEY, userStr);
        editor.commit();

        PrefsWrapper.setThereAreNewUserCredentials(true);
    }

    public static Collection<UserCredentials> getRemoteCredentialsList() {
        String credListStr = prefs.getString(REMOTE_CREDENTIALS_LIST_KEY, null);

        if (credListStr == null) return null;

        Type collectionType = new TypeToken<Collection<UserCredentials>>(){}.getType();
        return gson.fromJson(credListStr, collectionType);
    }

    public static void setRemoteCredentialsList(Collection<UserCredentials> keys) {
        Type collectionType = new TypeToken<Collection<UserCredentials>>(){}.getType();
        String credListStr = gson.toJson(keys, collectionType);
        editor.putString(REMOTE_CREDENTIALS_LIST_KEY, credListStr);
        editor.commit();

        PrefsWrapper.setThereAreNewRemoteCredentials(true);
    }

    public static InvokedServiceReport getInvokedServiceReport() {
        String reportStr = prefs.getString(INVOKED_SERVICE_REPORT_KEY, null);

        if (reportStr == null) return null;

        return gson.fromJson(reportStr, InvokedServiceReport.class);
    }

    public static void setInvokedServiceReport(InvokedServiceReport report) {
        String reportStr = gson.toJson(report, InvokedServiceReport.class);
        editor.putString(INVOKED_SERVICE_REPORT_KEY, reportStr);
        editor.commit();

        PrefsWrapper.setThereIsNewInvokedServiceReport(true);
    }

    public static Boolean thereIsNewCertificateData() {
        return prefs.getBoolean(NEW_CERTIFICATE_DATA_KEY, false);
    }

    public static void setThereIsNewCertificateData(Boolean isNewActivity) {
        editor.putBoolean(NEW_CERTIFICATE_DATA_KEY, isNewActivity);
        editor.commit();
    }

    public static Boolean thereAreNewUserCredentials() {
        return prefs.getBoolean(NEW_USER_CREDENTIALS_KEY, false);
    }

    public static void setThereAreNewUserCredentials(Boolean areNewCredentials) {
        editor.putBoolean(NEW_USER_CREDENTIALS_KEY, areNewCredentials);
        editor.commit();
    }

    public static Boolean thereAreNewRemoteCredentials() {
        return prefs.getBoolean(NEW_REMOTE_CREDENTIALS_LIST_KEY, false);
    }

    public static void setThereAreNewRemoteCredentials(Boolean areNewCredentials) {
        editor.putBoolean(NEW_REMOTE_CREDENTIALS_LIST_KEY, areNewCredentials);
        editor.commit();
    }

    public static Boolean thereIsNewInvokedServiceReport() {
        return prefs.getBoolean(NEW_INVOKED_SERVICE_REPORT_KEY, false);
    }

    public static void setThereIsNewInvokedServiceReport(Boolean isNewReport) {
        editor.putBoolean(NEW_INVOKED_SERVICE_REPORT_KEY, isNewReport);
        editor.commit();
    }

}
