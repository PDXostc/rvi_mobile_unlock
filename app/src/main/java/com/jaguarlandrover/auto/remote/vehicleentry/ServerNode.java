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
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.jaguarlandrover.pki.PKIManager;
import com.jaguarlandrover.rvi.RVILocalNode;
import com.jaguarlandrover.rvi.RVIRemoteNode;
import com.jaguarlandrover.rvi.RVIRemoteNodeListener;

import java.util.*;

class ServerNode
{
    private final static String TAG = "UnlockDemo/ServerNode__";

    interface Listener {
        void serverNodeDidConnect();
        void serverNodeDidDisconnect();
    }

    /* * * * * * * * * * * * * * * * * * * * Static variables * * * * * * * * * * * * * * * * * * **/
    private static Context applicationContext = UnlockApplication.getContext();

    private static SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
    private static Gson              gson        = new Gson();

    private static RVIRemoteNode rviNode = new RVIRemoteNode(null);

    private static ArrayList<Listener> listeners = new ArrayList<>();

    /* * * * * * * * * * * * * * * * * * SharedPreferences keys * * * * * * * * * * * * * * * * * **/
    private final static String NEW_USER_DATA_KEY               = "NEW_USER_DATA_KEY";
    private final static String USER_DATA_KEY                   = "USER_DATA_KEY";
    private final static String NEW_INVOKED_SERVICE_REPORT_KEY  = "NEW_INVOKED_SERVICE_REPORT_KEY";
    private final static String INVOKED_SERVICE_REPORT_KEY      = "INVOKED_SERVICE_REPORT_KEY";


    /* * * * * * * * * * * * * * * * * RVI service identifier parts * * * * * * * * * * * * * * * **/
    /* * * *  Service bundle * * * */
    private final static String CREDENTIAL_MANAGEMENT_BUNDLE = "credential_management";
    /* Local services */
    private final static String REVOKE_CREDENTIALS  = "revoke_credentials";
    private final static String UPDATE_CREDENTIALS  = "update_credentials";
    /* Remote services */
    private final static String REQUEST_CREDENTIALS = "request_credentials";

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /* * * *  Service bundle * * * */
    private final static String ACCOUNT_MANAGEMENT_BUNDLE = "account_management";
    /* Remote services */
    private final static String AUTHORIZE_SERVICES   = "authorize_services";
    private final static String REVOKE_AUTHORIZATION = "revoke_authorization";
    /* Local and remote services */
    private final static String GET_USER_DATA = "get_user_data";
    private final static String SET_USER_DATA = "set_user_data";

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /* * * *  Service bundle * * * */
    private final static String REPORTING_BUNDLE = "report";
    /* Remote services */
    private final static String SERVICE_INVOKED_BY_GUEST  = "service_invoked_by_guest";


    /* * * * * * * * * * * * * * * * Local service identifier lists * * * * * * * * * * * * * * * **/
    private final static ArrayList<String> credentialManagementBundleLocalServiceIdentifiers =
            new ArrayList<>(Arrays.asList(
                    CREDENTIAL_MANAGEMENT_BUNDLE + "/" + REVOKE_CREDENTIALS,
                    CREDENTIAL_MANAGEMENT_BUNDLE + "/" + UPDATE_CREDENTIALS));

    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    private final static ArrayList<String> accountManagementBundleLocalServiceIdentifiers =
            new ArrayList<>(Arrays.asList(
                    ACCOUNT_MANAGEMENT_BUNDLE + "/" + SET_USER_DATA));

    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    private final static ArrayList<String> reportingBundleLocalServiceIdentifiers =
            new ArrayList<>(Arrays.asList(
                    REPORTING_BUNDLE + "/" + SERVICE_INVOKED_BY_GUEST));

    /* * * * * * * * * * * * * * * * * * * * Other stuff * * * * * * * * * * * * * * * * * * * * **/
    private enum ConnectionStatus
    {
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }

    private static ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;
    private static boolean shouldTryAndReconnect = false;
    private static boolean needsToRequestNewCredentials = false;

    private static ServerNode ourInstance = new ServerNode();

    private ServerNode() {

        RVIRemoteNodeListener nodeListener = new RVIRemoteNodeListener()
        {
            @Override
            public void nodeDidConnect(RVIRemoteNode node) {
                Log.d(TAG, "Connected to RVI provisioning server!");
                connectionStatus = ConnectionStatus.CONNECTED;

                needsToRequestNewCredentials = true;

                stopRepeatingTask();

                for (Listener listener : listeners)
                    listener.serverNodeDidConnect();
            }

            @Override
            public void nodeDidFailToConnect(RVIRemoteNode node, Throwable reason) {
                Log.d(TAG, "Failed to connect to RVI provisioning server!");
                connectionStatus = ConnectionStatus.DISCONNECTED;

                //startRepeatingTasks();
            }

            @Override
            public void nodeDidDisconnect(RVIRemoteNode node, Throwable reason) {
                Log.d(TAG, "Disconnected from RVI provisioning server!");
                connectionStatus = ConnectionStatus.DISCONNECTED;

                /* Try and reconnect */
                if (shouldTryAndReconnect)
                    startRepeatingTask();

                for (Listener listener : listeners)
                    listener.serverNodeDidDisconnect();
            }

            @Override
            public void nodeSendServiceInvocationSucceeded(RVIRemoteNode node, String serviceIdentifier) {

            }

            @Override
            public void nodeSendServiceInvocationFailed(RVIRemoteNode node, String serviceIdentifier, Throwable reason) {

            }

            @Override
            public void nodeReceiveServiceInvocationSucceeded(RVIRemoteNode node, String serviceIdentifier, Object parameters) {
                String[] serviceParts = serviceIdentifier.split("/");

                if (serviceParts.length < 2) return;

                switch (serviceParts[0]) {
                    case CREDENTIAL_MANAGEMENT_BUNDLE:
                        switch (serviceParts[1]) {
                            case UPDATE_CREDENTIALS:
                                // TODO: Check this #amm
                                ArrayList<String> credentials = (ArrayList<String>) ((LinkedTreeMap<String, Object>) parameters).get("credentials");
                                RVILocalNode.setCredentials(applicationContext, credentials);
                                RVILocalNode.saveCredentials(applicationContext);

                                break;

                            case REVOKE_CREDENTIALS:
                                RVILocalNode.setCredentials(applicationContext, null);
                                RVILocalNode.saveCredentials(applicationContext);
                                break;
                        }

                        break;

                    case ACCOUNT_MANAGEMENT_BUNDLE:
                        switch (serviceParts[1]) {
                            case SET_USER_DATA:
                                ServerNode.setUserData(gson.toJson(parameters));

                                break;
                        }

                        break;

                    case REPORTING_BUNDLE:
                        switch (serviceParts[1]) {
                            case SERVICE_INVOKED_BY_GUEST:
                                ServerNode.setInvokedServiceReport(gson.toJson(parameters));

                                break;
                        }

                        break;

                }
            }

            @Override
            public void nodeReceiveServiceInvocationFailed(RVIRemoteNode node, String serviceIdentifier, Throwable reason) {

            }

            @Override
            public void nodeDidAuthorizeLocalServices(RVIRemoteNode node, Set<String> serviceIdentifiers) {
                Log.d(TAG, "Local services available: " + serviceIdentifiers.toString());
            }

            @Override
            public void nodeDidAuthorizeRemoteServices(RVIRemoteNode node, Set<String> serviceIdentifiers) {
                Log.d(TAG, "Remote services available: " + serviceIdentifiers.toString());

                for (String serviceIdentifier : serviceIdentifiers) {
                    if (serviceIdentifier.equals(CREDENTIAL_MANAGEMENT_BUNDLE + "/" + REQUEST_CREDENTIALS)) {
                        if (needsToRequestNewCredentials) {
                            needsToRequestNewCredentials = false;
                            requestCredentials();
                        }
                    } else if (serviceIdentifier.equals(ACCOUNT_MANAGEMENT_BUNDLE + "/" + GET_USER_DATA)) {
                        requestUserData();
                    }
                }
            }
        };

        rviNode.setListener(nodeListener);

        RVILocalNode.addLocalServices(UnlockApplication.getContext(), credentialManagementBundleLocalServiceIdentifiers);
        RVILocalNode.addLocalServices(UnlockApplication.getContext(), accountManagementBundleLocalServiceIdentifiers);
        RVILocalNode.addLocalServices(UnlockApplication.getContext(), reportingBundleLocalServiceIdentifiers);
    }

    Handler  timerHandler  = new Handler();
    Runnable timerRunnable = new Runnable()
    {
        @Override
        public void run() {
            if (connectionStatus == ConnectionStatus.DISCONNECTED) connect();

            timerHandler.postDelayed(this, 60 * 1000);
        }
    };

    private void startRepeatingTask() {
        timerHandler.postDelayed(timerRunnable, 60 * 1000);
    }

    private void stopRepeatingTask() {
        timerHandler.removeCallbacks(timerRunnable);
    }

    static void connect() {
        Log.d(TAG, "Attempting to connect to RVI provisioning server.");

        rviNode.setServerUrl(preferences.getString("pref_rvi_server", "38.129.64.40"));
        rviNode.setServerPort(Integer.parseInt(preferences.getString("pref_rvi_server_port", "9010")));

        connectionStatus = ConnectionStatus.CONNECTING;
        shouldTryAndReconnect = true;

        rviNode.connect();
    }

    static void disconnect() {
        Log.d(TAG, "Disconnecting from the RVI provisioning server.");

        shouldTryAndReconnect = false;

        rviNode.disconnect();
    }

    static boolean isConnected() {
        return connectionStatus == ConnectionStatus.CONNECTED;
    }

    static void requestCredentials() {
        Log.d(TAG, "Requesting credentials from RVI provisioning server.");

        if (connectionStatus == ConnectionStatus.DISCONNECTED) connect();

        HashMap<String, String> parameters = new HashMap<>();

        try {
            parameters.put("node_identifier", RVILocalNode.getLocalNodeIdentifier(applicationContext).substring("android/".length()));
            parameters.put("public_key", PKIManager.getPublicKey(applicationContext));
        } catch (Exception e) {
            e.printStackTrace();
        }

        rviNode.invokeService(CREDENTIAL_MANAGEMENT_BUNDLE + "/" + REQUEST_CREDENTIALS, parameters, 60 * 1000);
    }

    static void requestUserData() {
        Log.d(TAG, "Requesting user data from RVI provisioning server.");

        if (connectionStatus == ConnectionStatus.DISCONNECTED) connect();

        HashMap<String, String> parameters = new HashMap<>();

        try {
            parameters.put("node_identifier", RVILocalNode.getLocalNodeIdentifier(applicationContext).substring("android/".length()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        rviNode.invokeService(ACCOUNT_MANAGEMENT_BUNDLE + "/" + GET_USER_DATA, parameters, 60 * 1000);
    }

    static void revokeAuthorization(User remoteUser) {
        Log.d(TAG, "Revoking authorization for user on RVI provisioning server.");

        if (connectionStatus == ConnectionStatus.DISCONNECTED) connect();

        rviNode.invokeService(ACCOUNT_MANAGEMENT_BUNDLE + "/" + REVOKE_AUTHORIZATION, remoteUser, 60 * 1000);
    }

    static void authorizeServices(User remoteUser) {
        Log.d(TAG, "Creating remote credentials on RVI provisioning server.");

        if (connectionStatus == ConnectionStatus.DISCONNECTED) connect();

        rviNode.invokeService(ACCOUNT_MANAGEMENT_BUNDLE + "/" + AUTHORIZE_SERVICES, remoteUser, 60 * 1000);
    }

    static User getUserData() {
        String userStr = preferences.getString(USER_DATA_KEY, null);

        User userData = new User();
        try {
            if (userStr != null)
                userData = gson.fromJson(userStr, User.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return userData;
    }

    private static void setUserData(String userCredsStr) {
        //userCredsStr = "  {  \"username\": \"pdxostc.android@gmail.com\",  \"first_name\": \"Pdxostc\",  \"last_name\": \"Android\",  \"guests\": [{   \"username\": \"admin\",   \"first_name\": \"Admin\",   \"last_name\": \"Overlord\"  }, {   \"username\": \"pdxostc.android@gmail.com\",   \"first_name\": \"Pdxostc\",   \"last_name\": \"Android\"  }, {   \"username\": \"android.pdxostc@gmail.com\",   \"first_name\": \"Android\",   \"last_name\": \"Pdxostc\"  }],  \"vehicles\": [{   \"vehicle_id\": \"fake1\",   \"valid_from\": \"2016-10-11T21:46:48.000Z\",   \"display_name\": \"fake1\",   \"user_type\": \"guest\",   \"valid_to\": \"2017-10-11T21:46:48.000Z\",   \"authorized_services\": {    \"engine\": \"false\",    \"windows\": \"false\",    \"lock\": \"True\",    \"hazard\": \"false\",    \"horn\": \"True\",    \"lights\": \"True\",    \"trunk\": \"True\"   }  }, {   \"vehicle_id\": \"fake2\",   \"valid_from\": \"2016-10-11T21:46:48.000Z\",   \"display_name\": \"fake2\",   \"user_type\": \"guest\",   \"valid_to\": \"2017-10-11T21:46:48.000Z\",   \"authorized_services\": {    \"engine\": \"True\",    \"windows\": \"True\",    \"lock\": \"True\",    \"hazard\": \"True\",    \"horn\": \"True\",    \"lights\": \"True\",    \"trunk\": \"True\"   }  }, {   \"vehicle_id\": \"genivi-amm-ftype\",   \"valid_from\": \"2016-10-11T21:46:48.000Z\",   \"display_name\": \"F-Type\",   \"user_type\": \"owner\",   \"valid_to\": \"2017-10-11T21:46:48.000Z\",   \"authorized_services\": {    \"engine\": \"True\",    \"windows\": \"True\",    \"lock\": \"True\",    \"hazard\": \"True\",    \"horn\": \"True\",    \"lights\": \"True\",    \"trunk\": \"True\"   }  }, {   \"vehicle_id\": \"DummyL405\",   \"valid_from\": \"2016-10-11T21:46:48.000Z\",   \"display_name\": \"L405-Test\",   \"user_type\": \"owner\",   \"valid_to\": \"2017-10-11T21:46:48.000Z\",   \"authorized_services\": {    \"engine\": \"True\",    \"windows\": \"True\",    \"lock\": \"True\",    \"hazard\": \"True\",    \"horn\": \"True\",    \"lights\": \"True\",    \"trunk\": \"True\"   }  }] }";

        User previousUserData = getUserData();

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(USER_DATA_KEY, userCredsStr);
        editor.commit();

        if (!previousUserData.equals(getUserData()))
            ServerNode.setThereIsNewUserData(true);
    }

    static void deleteUserData() {
        setUserData(new User().toString());
    }

    static InvokedServiceReport getInvokedServiceReport() {
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
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(INVOKED_SERVICE_REPORT_KEY, reportStr);
        editor.commit();

        ServerNode.setThereIsNewInvokedServiceReport(true);
    }

    static Boolean thereIsNewUserData() {
        return preferences.getBoolean(NEW_USER_DATA_KEY, false);
    }

    static void setThereIsNewUserData(Boolean thereIsNewUserData) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(NEW_USER_DATA_KEY, thereIsNewUserData);
        editor.commit();
    }

    static Boolean thereIsNewInvokedServiceReport() {
        return preferences.getBoolean(NEW_INVOKED_SERVICE_REPORT_KEY, false);
    }

    static void setThereIsNewInvokedServiceReport(Boolean isNewReport) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(NEW_INVOKED_SERVICE_REPORT_KEY, isNewReport);
        editor.commit();
    }

    static void addListener(Listener listener) {
        listeners.add(listener);
    }

    static void removeListener(Listener listener) {
        listeners.remove(listener);
    }
}
