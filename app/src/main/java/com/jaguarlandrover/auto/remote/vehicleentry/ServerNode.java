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
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.jaguarlandrover.rvi.RVINode;
import com.jaguarlandrover.rvi.ServiceBundle;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.*;

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

//    private enum ConnectionStatus
//    {
//        DISCONNECTED,
//        CONNECTING,
//        CONNECTED;
//    }
//
//    private static ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;

    private static ServerNode ourInstance = new ServerNode();

    public static ServerNode getInstance() {
        return ourInstance;
    }

    private ServerNode() {
        /* Listeners */
        ServiceBundle.ServiceBundleListener serviceBundleListener = new ServiceBundle.ServiceBundleListener()
        {
            @Override
            public void onServiceInvoked(ServiceBundle serviceBundle, String serviceIdentifier, Object parameters) {
                if (serviceBundle.getBundleIdentifier().equals(CERT_PROV_BUNDLE)) {
                    switch (serviceIdentifier) {
                        case CERT_RESPONSE:
//                            Type collectionType = new TypeToken<Collection<UserCredentials>>() {}.getType();
//                            Collection<UserCredentials> remoteCredentials =
//                                    gson.fromJson(gson.toJson(gson.fromJson((String) parameters, HashMap.class).get("certificates")), collectionType);

                            ServerNode.setRemoteCredentialsList(gson.toJson(gson.fromJson((String) parameters, HashMap.class).get("certificates")));

                            break;
                        case CERT_PROVISION:
                            String certId = ((HashMap<String, String>) parameters).get("certid");
                            String certificateJwt = ((HashMap<String, String>) parameters).get("certificate");

                            // TODO: Need to handle certs?
                            //certs.put(certId, jwt);

                            //Debug
                            // Errors seen here on parseAndValidateJWT. Should be getting Base64
                            // from backend, but sometimes getting errors that it's not.
                            // Should be fixed now, backend is sending URL safe Base64,
                            // parseAndValidateJWT now using Base64.URL_SAFE
                            String[] token = RviProtocol.parseAndValidateJWT(certificateJwt);

                            ServerNode.setCertificate(token[1]);

                            // TODO: Set up notification system to notify UI that stuff is coming from the server instead of the current polling mechanism
//                            sendNotification(RviService.this, getResources().getString(R.string.not_new_key) + " : " + key.getString("id"),
//                                    "dialog", "New Key", key.getString("id"));
                            break;

                        case CERT_ACCOUNT_DETAILS:
                            ServerNode.setUserCredentials(gson.toJson(parameters));

                            break;
                        }

                } else if (serviceBundle.getBundleIdentifier().equals(REPORTING_BUNDLE)) {
                    if (serviceIdentifier.equals(SERVICE_INVOKED_BY_GUEST)) {
                        ServerNode.setInvokedServiceReport(gson.toJson(parameters));
                    }
                }
            }
        };

        RVINode.RVINodeListener nodeListener = new RVINode.RVINodeListener()
        {
            @Override
            public void nodeDidConnect() {
                Log.d(TAG, "Connected to RVI provisioning server!");
                // TODO: If pending service invocations, invoke.
            }

            @Override
            public void nodeDidFailToConnect(Throwable trigger) {
                Log.d(TAG, "Failed to connect to RVI provisioning server!");

            }

            @Override
            public void nodeDidDisconnect(Throwable trigger) {
                Log.d(TAG, "Disconnected from RVI provisioning server!");
//                connectionStatus = ConnectionStatus.DISCONNECTED;

                /* Try and reconnect */
                connect();
            }
        };

        certProvServiceBundle.setListener(serviceBundleListener);
        reportingServiceBundle.setListener(serviceBundleListener);


        rviNode.setListener(nodeListener);

        rviNode.addBundle(certProvServiceBundle);
        rviNode.addBundle(reportingServiceBundle);
    }

    public static void connect() {
        rviNode.setServerUrl(preferences.getString("pref_rvi_server", "38.129.64.40"));
        rviNode.setServerPort(Integer.parseInt(preferences.getString("pref_rvi_server_port", "8807")));

        rviNode.connect();
    }

    public static void requestRemoteCredentials() {
        Log.d(TAG, "Requesting remote credentials from RVI provisioning server.");

        HashMap<String, String> parameters = new HashMap<>();

        try {
            parameters.put("mobileUUID", RVINode.getLocalNodeIdentifier(applicationContext).substring("android/".length()));
            parameters.put("vehicleVIN", ServerNode.getUserCredentials().getVehicleVin());
        } catch (Exception e) {
            e.printStackTrace();
        }

        certProvServiceBundle.invokeService(CERT_REQUESTALL, parameters, 5000);
    }

    public static void modifyRemoteCredentials(UserCredentials remoteCredentials) {
        Log.d(TAG, "Modifying remote credentials on RVI provisioning server.");
        certProvServiceBundle.invokeService(CERT_MODIFY, remoteCredentials, 5000);
    }

    public static void createRemoteCredentials(UserCredentials remoteCredentials) {
        Log.d(TAG, "Creating remote credentials on RVI provisioning server.");
        certProvServiceBundle.invokeService(CERT_CREATE, remoteCredentials, 5000);
    }

    public static Certificate getCertificate() {
        String certStr = preferences.getString(CERTIFICATE_DATA_KEY, null);

        Certificate certificate = new Certificate();
        try {
            certificate = gson.fromJson(certStr, Certificate.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return certificate;
    }

    private static void setCertificate(String certStr) {
        editor.putString(CERTIFICATE_DATA_KEY, certStr);
        editor.commit();

        ServerNode.setThereIsNewCertificateData(true);
    }

    public static UserCredentials getUserCredentials() {
        String userStr = preferences.getString(USER_CREDENTIALS_KEY, null);

        UserCredentials userCreds = new UserCredentials();
        try {
            userCreds = gson.fromJson(userStr, UserCredentials.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return userCreds;
    }

    private static void setUserCredentials(String userCredsStr) {
        editor.putString(USER_CREDENTIALS_KEY, userCredsStr);
        editor.commit();

        ServerNode.setThereAreNewUserCredentials(true);
    }

    public static Collection<UserCredentials> getRemoteCredentialsList() {
        String credListStr = preferences.getString(REMOTE_CREDENTIALS_LIST_KEY, null);

        if (credListStr == null) return null;

        Collection<UserCredentials> credsList = null;
        Type collectionType = new TypeToken<Collection<UserCredentials>>() {}.getType();

        try {
            credsList = gson.fromJson(credListStr, collectionType);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return credsList;
    }

    private static void setRemoteCredentialsList(String credsListStr) {
        editor.putString(REMOTE_CREDENTIALS_LIST_KEY, credsListStr);
        editor.commit();

        ServerNode.setThereAreNewRemoteCredentials(true);
    }

    public static InvokedServiceReport getInvokedServiceReport() {
        String reportStr = preferences.getString(INVOKED_SERVICE_REPORT_KEY, null);

        InvokedServiceReport report = null;
        try {
            report = gson.fromJson(reportStr, InvokedServiceReport.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return report;
    }

    private static void setInvokedServiceReport(String reportStr) {
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
}
