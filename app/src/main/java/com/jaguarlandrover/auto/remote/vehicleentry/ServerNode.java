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
import com.jaguarlandrover.rvi.RVINode;
import com.jaguarlandrover.rvi.ServiceBundle;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class ServerNode
{
    private final static String TAG = "UnlockDemo:RVIManager";

    private final static String RVI_DOMAIN       = "jlr.com";
    private final static String CERT_PROV_BUNDLE = "dm";
    private final static String REPORTING_BUNDLE = "report";

    private final static String CERT_PROVISION       = "cert_provision";
    private final static String CERT_RESPONSE        = "cert_response";
    private final static String CERT_ACCOUNT_DETAILS = "cert_accountdetails";

    private final static String SERVICE_INVOKED_BY_GUEST = "serviceinvokedbyguest";


    private static ServerNode ourInstance = new ServerNode();

    private static Context applicationContext = UnlockApplication.getContext();

    private static SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);

    public static ServerNode getInstance() {
        return ourInstance;
    }

    private static       RVINode           rviNode                    = new RVINode(null);
    private final static ArrayList<String> certProvServiceIdentifiers =
            new ArrayList<>(Arrays.asList(
                    CERT_PROVISION,
                    CERT_RESPONSE,
                    CERT_ACCOUNT_DETAILS));

    private final static ArrayList<String> reportingServiceIdentifiers =
            new ArrayList<>(Arrays.asList(
                    SERVICE_INVOKED_BY_GUEST));

    private final static ServiceBundle certProvServiceBundle  = new ServiceBundle(applicationContext, RVI_DOMAIN, CERT_PROV_BUNDLE, certProvServiceIdentifiers);
    private final static ServiceBundle reportingServiceBundle = new ServiceBundle(applicationContext, RVI_DOMAIN, REPORTING_BUNDLE, reportingServiceIdentifiers);

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
//                    Log.i(TAG, "Received Cert Params : " + parameters);
//                    JSONObject p1 = parameters.getJSONObject(0);
//                    JSONObject p2 = parameters.getJSONObject(1);
//                    String certId = p1.getString("certid");
//                    String jwt = p2.getString("certificate");
//                    Log.i(TAG, "Received from Cloud Cert ID: " + certId);
//                    Log.i(TAG, "JWT = " + jwt);
//
//                    certs.put(certId, jwt);
//                    //Debug
//                    // Errors seen here on parseAndValidateJWT. Should be getting Base64
//                    // from backend, but sometimes getting errors that it's not.
//                    // Should be fixed now, backend is sending URL safe Base64,
//                    // parseAndValidateJWT now using Base64.URL_SAFE
//                    String[] token = RviProtocol.parseAndValidateJWT(jwt);
//                    JSONObject key = new JSONObject(token[1]);
//                    Log.d(TAG, "Token = " + key.toString(2));
//                    sendNotification(RviService.this, getResources().getString(R.string.not_new_key) + " : " + key.getString("id"),
//                            "dialog", "New Key", key.getString("id"));

                } else if (serviceIdentifier.equals(CERT_PROVISION)) {

//                    Log.i(TAG, "Received from Cloud Cert: " + parameters);
//
//                    SharedPreferences.Editor e = preferences.edit();
//                    e.putString("Certificates", parameters);
//                    e.putString("newKeyList", "true");
//                    e.apply();

                } else if (serviceIdentifier.equals(CERT_ACCOUNT_DETAILS)) {
//                    JSONObject p1 = parameters.getJSONObject(0);
//                    Log.i(TAG, "User Data:" + p1);
//
//                    SharedPreferences.Editor e = preferences.edit();
//                    e.putString("Userdata", p1.toString());
//                    e.putString("newdata", "true");
//                    e.commit();

                }
            } else if (serviceBundle.getBundleIdentifier().equals(REPORTING_BUNDLE)) {
                if (serviceIdentifier.equals(SERVICE_INVOKED_BY_GUEST)) {
//                    JSONArray params = data.getJSONArray("parameters");
//                    JSONObject p1 = params.getJSONObject(0);
//                    Log.i(TAG, "Service Invoked by Guest:" + p1);
//
//                    SharedPreferences.Editor e = preferences.edit();
//                    e.putString("guestInvokedService", p1.toString());
//                    e.putString("newguestactivity", "true");
//                    e.commit();

                }
            }
        }


    };


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

    public interface ServerNodeListener
    {
        void onReceivedCertificateResponse();

        void onReceiveCertificateProvisioning();

        void onReceivedCertificateAccountDetails();

        void onServiceInvokedByGuest();
    }
}
