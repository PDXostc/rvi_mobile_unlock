package com.jaguarlandrover.auto.remote.vehicleentry;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2015 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    RVIManager.java
 * Project: UnlockDemo
 *
 * Created by Lilli Szafranski on 10/28/15.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jaguarlandrover.rvi.RVINode;
import com.jaguarlandrover.rvi.ServiceBundle;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class ServerNode
{
    private final static String TAG = "UnlockDemo:ServerNode";

    /* Static objects */
    private static Context applicationContext = UnlockApplication.getContext();

    private static SharedPreferences        preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
    private static SharedPreferences.Editor editor      = preferences.edit();
    private static Gson                     gson        = new Gson();

    private static RVINode rviNode = new RVINode(null);

    /* SharedPreferences keys */
    private final static String NEW_CERTIFICATE_DATA_KEY        = "NEW_CERTIFICATE_DATA_KEY";
    private final static String CERTIFICATE_DATA_KEY            = "CERTIFICATE_DATA_KEY";
    private final static String NEW_USER_CREDENTIALS_KEY        = "NEW_USER_CREDENTIALS_KEY";
    private final static String USER_CREDENTIALS_KEY            = "USER_CREDENTIALS_KEY";
    private final static String NEW_REMOTE_CREDENTIALS_LIST_KEY = "NEW_REMOTE_CREDENTIALS_LIST_KEY";
    private final static String REMOTE_CREDENTIALS_LIST_KEY     = "REMOTE_CREDENTIALS_LIST_KEY";
    private final static String NEW_INVOKED_SERVICE_REPORT_KEY  = "NEW_INVOKED_SERVICE_REPORT_KEY";
    private final static String INVOKED_SERVICE_REPORT_KEY      = "INVOKED_SERVICE_REPORT_KEY";

    /* RVI fully-qualified service identifier parts */
    private final static String RVI_DOMAIN       = "jlr.com";
    private final static String CERT_PROV_BUNDLE = "dm";
    private final static String REPORTING_BUNDLE = "report";

    /* Remote service identifiers */
    private final static String CERT_REQUESTALL = "cert_requestall";
    private final static String CERT_CREATE     = "cert_create";
    private final static String CERT_MODIFY     = "cert_modify";

    /* Local service identifiers */
    private final static String CERT_PROVISION       = "cert_provision";
    private final static String CERT_RESPONSE        = "cert_response";
    private final static String CERT_ACCOUNT_DETAILS = "cert_accountdetails";

    private final static String SERVICE_INVOKED_BY_GUEST = "serviceinvokedbyguest";

    private final static ArrayList<String> certProvServiceIdentifiers =
            new ArrayList<>(Arrays.asList(
                    CERT_PROVISION,
                    CERT_RESPONSE,
                    CERT_ACCOUNT_DETAILS));

    private final static ArrayList<String> reportingServiceIdentifiers =
            new ArrayList<>(Arrays.asList(
                    SERVICE_INVOKED_BY_GUEST));

    /* Service bundles */
    private final static ServiceBundle certProvServiceBundle  = new ServiceBundle(applicationContext, RVI_DOMAIN, CERT_PROV_BUNDLE, certProvServiceIdentifiers);
    private final static ServiceBundle reportingServiceBundle = new ServiceBundle(applicationContext, RVI_DOMAIN, REPORTING_BUNDLE, reportingServiceIdentifiers);

    private static ServerNode ourInstance = new ServerNode();

    public static ServerNode getInstance() {
        return ourInstance;
    }

    private ServerNode() {
        certProvServiceBundle.setListener(serviceBundleListener);
        reportingServiceBundle.setListener(serviceBundleListener);

        rviNode.setListener(nodeListener);

        rviNode.addBundle(certProvServiceBundle);
        rviNode.addBundle(reportingServiceBundle);
    }

    public static void connect() {
        connectToServer();
    }

    private static void connectToServer() {
        if (rviNode.isConnected()) rviNode.disconnect();

        rviNode.setServerUrl(preferences.getString("pref_rvi_server", "38.129.64.40"));
        rviNode.setServerPort(Integer.parseInt(preferences.getString("pref_rvi_server_port", "8807")));

        rviNode.connect();
    }

    public void requestRemoteCredentials() {

    }

    public void modifyRemoteCredentials(UserCredentials remoteCredentials) {

    }

    public void createRemoteCredentials(UserCredentials remoteCredentials) {

    }


    public interface ServerNodeListener
    {
        void onReceivedCertificateResponse();

        void onReceiveCertificateProvisioning();

        void onReceivedCertificateAccountDetails();

        void onServiceInvokedByGuest();
    }

    public static Certificate getCertificate() {
        String certStr = preferences.getString(CERTIFICATE_DATA_KEY, null);

        if (certStr == null) return null;

        return gson.fromJson(certStr, Certificate.class);
    }

    public static void setCertificate(Certificate cert) {
        String certStr = gson.toJson(cert, Certificate.class);
        editor.putString(CERTIFICATE_DATA_KEY, certStr);
        editor.commit();

        ServerNode.setThereIsNewCertificateData(true);
    }

    public static UserCredentials getUserCredentials() {
        String userStr = preferences.getString(USER_CREDENTIALS_KEY, null);

        if (userStr == null) return null;

        return gson.fromJson(userStr, UserCredentials.class);
    }

    public static void setUserCredentials(UserCredentials userCredentials) {
        String userStr = gson.toJson(userCredentials, UserCredentials.class);
        editor.putString(USER_CREDENTIALS_KEY, userStr);
        editor.commit();

        ServerNode.setThereAreNewUserCredentials(true);
    }

    public static Collection<UserCredentials> getRemoteCredentialsList() {
        String credListStr = preferences.getString(REMOTE_CREDENTIALS_LIST_KEY, null);

        if (credListStr == null) return null;

        Type collectionType = new TypeToken<Collection<UserCredentials>>()
        {
        }.getType();
        return gson.fromJson(credListStr, collectionType);
    }

    public static void setRemoteCredentialsList(Collection<UserCredentials> keys) {
        Type collectionType = new TypeToken<Collection<UserCredentials>>()
        {
        }.getType();
        String credListStr = gson.toJson(keys, collectionType);
        editor.putString(REMOTE_CREDENTIALS_LIST_KEY, credListStr);
        editor.commit();

        ServerNode.setThereAreNewRemoteCredentials(true);
    }

    public static InvokedServiceReport getInvokedServiceReport() {
        String reportStr = preferences.getString(INVOKED_SERVICE_REPORT_KEY, null);

        if (reportStr == null) return null;

        return gson.fromJson(reportStr, InvokedServiceReport.class);
    }

    public static void setInvokedServiceReport(InvokedServiceReport report) {
        String reportStr = gson.toJson(report, InvokedServiceReport.class);
        editor.putString(INVOKED_SERVICE_REPORT_KEY, reportStr);
        editor.commit();

        ServerNode.setThereIsNewInvokedServiceReport(true);
    }

    public static Boolean thereIsNewCertificateData() {
        return preferences.getBoolean(NEW_CERTIFICATE_DATA_KEY, false);
    }

    public static void setThereIsNewCertificateData(Boolean isNewActivity) {
        editor.putBoolean(NEW_CERTIFICATE_DATA_KEY, isNewActivity);
        editor.commit();
    }

    public static Boolean thereAreNewUserCredentials() {
        return preferences.getBoolean(NEW_USER_CREDENTIALS_KEY, false);
    }

    public static void setThereAreNewUserCredentials(Boolean areNewCredentials) {
        editor.putBoolean(NEW_USER_CREDENTIALS_KEY, areNewCredentials);
        editor.commit();
    }

    public static Boolean thereAreNewRemoteCredentials() {
        return preferences.getBoolean(NEW_REMOTE_CREDENTIALS_LIST_KEY, false);
    }

    public static void setThereAreNewRemoteCredentials(Boolean areNewCredentials) {
        editor.putBoolean(NEW_REMOTE_CREDENTIALS_LIST_KEY, areNewCredentials);
        editor.commit();
    }

    public static Boolean thereIsNewInvokedServiceReport() {
        return preferences.getBoolean(NEW_INVOKED_SERVICE_REPORT_KEY, false);
    }

    public static void setThereIsNewInvokedServiceReport(Boolean isNewReport) {
        editor.putBoolean(NEW_INVOKED_SERVICE_REPORT_KEY, isNewReport);
        editor.commit();
    }

    /* Listeners */
    private static RVINode.RVINodeListener nodeListener = new RVINode.RVINodeListener()
    {
        @Override
        public void nodeDidConnect() {

        }

        @Override
        public void nodeDidFailToConnect() {

        }

        @Override
        public void nodeDidDisconnect() {

        }
    };

    private static ServiceBundle.ServiceBundleListener serviceBundleListener = new ServiceBundle.ServiceBundleListener()
    {
        @Override
        public void onServiceInvoked(ServiceBundle serviceBundle, String serviceIdentifier, Object parameters) {
            if (serviceBundle.getBundleIdentifier().equals(CERT_PROV_BUNDLE)) {
                if (serviceIdentifier.equals(CERT_RESPONSE)) {
                    // TODO: Copy updated code from RviService.java

                } else if (serviceIdentifier.equals(CERT_PROVISION)) {
                    // TODO: Copy updated code from RviService.java

                } else if (serviceIdentifier.equals(CERT_ACCOUNT_DETAILS)) {
                    // TODO: Copy updated code from RviService.java

                }
            } else if (serviceBundle.getBundleIdentifier().equals(REPORTING_BUNDLE)) {
                if (serviceIdentifier.equals(SERVICE_INVOKED_BY_GUEST)) {
                    // TODO: Copy updated code from RviService.java


                }
            }
        }
    };

}
