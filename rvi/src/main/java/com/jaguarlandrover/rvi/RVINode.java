package com.jaguarlandrover.rvi;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2015 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    RVINode.java
 * Project: RVI SDK
 *
 * Created by Lilli Szafranski on 7/1/15.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static android.content.Context.MODE_PRIVATE;

/**
 * The local RVI node.
 */
public class RVINode
{
    private final static String TAG = "RVI:RVINode";

    private HashMap<String, ServiceBundle> mAllServiceBundles       = new HashMap<>();
    private RemoteConnectionManager        mRemoteConnectionManager = new RemoteConnectionManager();

    private boolean mConnected = false;

    public RVINode(Context context) {
        mRemoteConnectionManager.setListener(new RemoteConnectionManagerListener()
        {
            @Override
            public void onRVIDidConnect() {
                mConnected = true;

                mRemoteConnectionManager.sendPacket(new DlinkAuthPacket());

                announceServices();

                if (mListener != null) mListener.nodeDidConnect();
            }

            @Override
            public void onRVIDidFailToConnect(Error error) {
                mConnected = false;

                if (mListener != null) mListener.nodeDidFailToConnect();
            }

            @Override
            public void onRVIDidDisconnect() {
                mConnected = false;

                if (mListener != null) mListener.nodeDidDisconnect();
            }

            @Override
            public void onRVIDidReceivePacket(DlinkPacket packet) {
                if (packet == null) return;

                if (packet.getClass().equals(DlinkReceivePacket.class)) {
                    handleReceivePacket((DlinkReceivePacket) packet);

                } else if (packet.getClass().equals(DlinkServiceAnnouncePacket.class)) {
                    handleServiceAnnouncePacket((DlinkServiceAnnouncePacket) packet);

                } else if (packet.getClass().equals(DlinkAuthPacket.class)) {
                    handleAuthPacket((DlinkAuthPacket) packet);

                }
            }

            @Override
            public void onRVIDidSendPacket() {

            }

            @Override
            public void onRVIDidFailToSendPacket(Error error) {

            }
        });
    }

    /**
     * Sets the @RVINodeListener listener.
     *
     * @param listener the listener
     */
    public void setListener(RVINodeListener listener) {
        mListener = listener;
    }


    /**
     * The RVI node listener interface.
     */
    public interface RVINodeListener
    {
        /**
         * Called when the local RVI node successfully connects to a remote RVI node.
         */
        void nodeDidConnect();

        /**
         * Called when the local RVI node failed to connect to a remote RVI node.
         */
        void nodeDidFailToConnect();

        /**
         * Called when the local RVI node disconnects from a remote RVI node.
         */
        void nodeDidDisconnect();

    }

    private RVINodeListener mListener;

    /**
     * Sets the server url to the remote RVI node, when using a TCP/IP link to interface with a remote node.
     *
     * @param serverUrl the server url
     */
    public void setServerUrl(String serverUrl) {
        mRemoteConnectionManager.setServerUrl(serverUrl);
    }

    /**
     * Sets the server port of the remote RVI node, when using a TCP/IP link to interface with a remote node.
     *
     * @param serverPort the server port
     */
    public void setServerPort(Integer serverPort) {
        mRemoteConnectionManager.setServerPort(serverPort);
    }

    public boolean isConnected() {
        return mConnected;
    }

    /**
     * Tells the local RVI node to connect to the remote RVI node.
     */
    public void connect() {
        // are we configured
        // connect
        mRemoteConnectionManager.connect();

    }

    /**
     * Tells the local RVI node to disconnect from the remote RVI node.
     */
    public void disconnect() {
        // disconnect

        mRemoteConnectionManager.disconnect();
    }

    /**
     * Add a service bundle to the local RVI node. Adding a service bundle triggers a service announce over the
     * network to the remote RVI node.
     *
     * @param bundle the bundle
     */
    public void addBundle(ServiceBundle bundle) {
        bundle.setNode(this);
        mAllServiceBundles.put(bundle.getDomain() + ":" + bundle.getBundleIdentifier(), bundle);
        announceServices();
    }

    /**
     * Remove a service bundle from the local RVI node. Removing a service bundle triggers a service announce over the
     * network to the remote RVI node.
     *
     * @param bundle the bundle
     */
    public void removeBundle(ServiceBundle bundle) {
        bundle.setNode(null);
        mAllServiceBundles.remove(bundle.getDomain() + ":" + bundle.getBundleIdentifier());
        announceServices();
    }

    /**
     * Have the local node announce all it's available services.
     */
    void announceServices() {
        ArrayList<String> allServices = new ArrayList<>();
        for (ServiceBundle bundle : mAllServiceBundles.values())
            allServices.addAll(bundle.getFullyQualifiedLocalServiceNames());

        mRemoteConnectionManager.sendPacket(new DlinkServiceAnnouncePacket(allServices));
    }

    /**
     * Invoke service.
     *
     * @param service the service
     */
    void invokeService(Service service) {
        mRemoteConnectionManager.sendPacket(new DlinkReceivePacket(service));
    }

    private void handleReceivePacket(DlinkReceivePacket packet) {
        Service service = packet.getService();

        ServiceBundle bundle = mAllServiceBundles.get(service.getDomain() + ":" + service.getBundleIdentifier());
        if (bundle != null)
            bundle.serviceInvoked(service);
    }

    private void handleServiceAnnouncePacket(DlinkServiceAnnouncePacket packet) {
        for (String fullyQualifiedRemoteServiceName : packet.getServices()) {

            String[] serviceParts = fullyQualifiedRemoteServiceName.split("/");

            if (serviceParts.length != 5) return;

            String domain = serviceParts[0];
            String nodeIdentifier = serviceParts[1] + "/" + serviceParts[2];
            String bundleIdentifier = serviceParts[3];
            String serviceIdentifier = serviceParts[4];

            ServiceBundle bundle = mAllServiceBundles.get(domain + ":" + bundleIdentifier);

            if (bundle != null)
                bundle.addRemoteService(serviceIdentifier, nodeIdentifier);
        }
    }

    private void handleAuthPacket(DlinkAuthPacket packet) {

    }

    private final static String SHARED_PREFS_STRING         = "com.rvisdk.settings";
    private final static String LOCAL_SERVICE_PREFIX_STRING = "localServicePrefix";

    // TODO: Test and verify this function
    private static String uuidB58String() {
        UUID uuid = UUID.randomUUID();
        String b64Str;

        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        b64Str = Base64.encodeToString(bb.array(), Base64.DEFAULT);
        b64Str = b64Str.split("=")[0];

        b64Str = b64Str.replace('+', 'P');
        b64Str = b64Str.replace('/', 'S'); /* Reduces likelihood of uniqueness but stops non-alphanumeric characters from screwing up any urls or anything */

        return b64Str;
    }

    /**
     * Gets the prefix of the local RVI node
     *
     * @param context the application context
     * @return the local prefix
     */
    public static String getLocalNodeIdentifier(Context context) { // TODO: There is no easy way to reset this once it's stored, is there? Maybe an app version check?
        SharedPreferences sharedPrefs = context.getSharedPreferences(SHARED_PREFS_STRING, MODE_PRIVATE);
        String localServicePrefix;

        if ((localServicePrefix = sharedPrefs.getString(LOCAL_SERVICE_PREFIX_STRING, null)) == null)
            localServicePrefix = "android/" + uuidB58String();

        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(LOCAL_SERVICE_PREFIX_STRING, localServicePrefix);
        editor.apply();

        return localServicePrefix;
    }
}
