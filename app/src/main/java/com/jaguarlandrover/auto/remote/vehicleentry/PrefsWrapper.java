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

import java.util.ArrayList;

public class PrefsWrapper
{
    private final static String TAG                        = "UnlockDemo:PrefsWrapper";
    private final static String NEW_CERTIFICATE_DATA_KEY   = "NEW_CERTIFICATE_DATA_KEY";
    private final static String CERTIFICATE_DATA_KEY       = "CERTIFICATE_DATA_KEY";
    private final static String NEW_USER_DATA_KEY          = "NEW_DATA_KEY";
    private final static String USER_DATA_KEY              = "USER_DATA_KEY";
    private final static String UPDATED_KEY_LIST_KEY       = "UPDATED_KEY_LIST_KEY";
    private final static String REMOTE_KEY_LIST_KEY        = "REMOTE_KEY_LIST_KEY";
    private final static String NEW_GUEST_ACTIVITY_KEY     = "NEW_ACTIVITY_KEY";
    private final static String INVOKED_SERVICE_REPORT_KEY = "INVOKED_SERVICE_REPORT_KEY";

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

    public static User getUser() {
        String userStr = prefs.getString(USER_DATA_KEY, null);

        if (userStr == null) return null;

        return gson.fromJson(userStr, User.class);
    }

    public static void setUser(User user) {
        String userStr = gson.toJson(user, User.class);
        editor.putString(USER_DATA_KEY, userStr);
        editor.commit();

        PrefsWrapper.setThereIsNewUserData(true);
    }

    public static ArrayList getRemoteKeys() {
        String keysStr = prefs.getString(REMOTE_KEY_LIST_KEY, null);

        if (keysStr == null) return null;

        return gson.fromJson(keysStr, ArrayList.class);
    }

    public static void setRemoteKeys(ArrayList keys) {
        String keysStr = gson.toJson(keys, ArrayList.class);
        editor.putString(REMOTE_KEY_LIST_KEY, keysStr);
        editor.commit();

        PrefsWrapper.setKeyListIsUpdated(true);
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

        PrefsWrapper.setThereIsNewGuestActivity(true);
    }

    public static Boolean thereIsNewCertificateData() {
        return prefs.getBoolean(NEW_CERTIFICATE_DATA_KEY, false);
    }

    public static void setThereIsNewCertificateData(Boolean isNewActivity) {
        editor.putBoolean(NEW_CERTIFICATE_DATA_KEY, isNewActivity);
        editor.commit();
    }

    public static Boolean thereIsNewUserData() {
        return prefs.getBoolean(NEW_USER_DATA_KEY, false);
    }

    public static void setThereIsNewUserData(Boolean isNewData) {
        editor.putBoolean(NEW_USER_DATA_KEY, isNewData);
        editor.commit();
    }

    public static Boolean keyListIsUpdated() {
        return prefs.getBoolean(UPDATED_KEY_LIST_KEY, false);
    }

    public static void setKeyListIsUpdated(Boolean isNewData) {
        editor.putBoolean(UPDATED_KEY_LIST_KEY, isNewData);
        editor.commit();
    }

    public static Boolean thereIsNewGuestActivity() {
        return prefs.getBoolean(NEW_GUEST_ACTIVITY_KEY, false);
    }

    public static void setThereIsNewGuestActivity(Boolean isNewActivity) {
        editor.putBoolean(NEW_GUEST_ACTIVITY_KEY, isNewActivity);
        editor.commit();
    }

}
