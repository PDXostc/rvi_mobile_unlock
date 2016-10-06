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
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * The local RVI node.
 */
public class RVINode
{
    private final static String TAG = "RVI:RVINode";


    private HashMap<String, ServiceBundle> mAllServiceBundles       = new HashMap<>();
    private RemoteConnectionManager        mRemoteConnectionManager = new RemoteConnectionManager();

    private boolean mIsConnected = false;

    public RVINode(Context context) {
        mRemoteConnectionManager.setListener(new RemoteConnectionManagerListener()
        {
            @Override
            public void onRVIDidConnect() {
                Log.d(TAG, Util.getMethodName());

                mIsConnected = true;
                if (mListener != null) mListener.nodeDidConnect();

                mRemoteConnectionManager.sendPacket(new DlinkAuthPacket(RVILocalNode.getCredentials()));
            }

            @Override
            public void onRVIDidFailToConnect(Throwable error) {
                Log.d(TAG, Util.getMethodName() + ": " + ((error == null) ? "(null)" : error.getLocalizedMessage()));

                mIsConnected = false;
                if (mListener != null) mListener.nodeDidFailToConnect(error);
            }

            @Override
            public void onRVIDidDisconnect(Throwable trigger) {
                Log.d(TAG, Util.getMethodName() + ": " + ((trigger == null) ? "(null)" : trigger.getLocalizedMessage()));

                mIsConnected = false;
                if (mListener != null) mListener.nodeDidDisconnect(trigger);
            }

            @Override
            public void onRVIDidReceivePacket(DlinkPacket packet) {
                if (packet == null) return;

                Log.d(TAG, Util.getMethodName() + ": " + packet.getClass().toString());

                if (packet.getClass().equals(DlinkReceivePacket.class)) {
                    handleReceivePacket((DlinkReceivePacket) packet);

                } else if (packet.getClass().equals(DlinkServiceAnnouncePacket.class)) {
                    handleServiceAnnouncePacket((DlinkServiceAnnouncePacket) packet);

                } else if (packet.getClass().equals(DlinkAuthPacket.class)) {
                    handleAuthPacket((DlinkAuthPacket) packet);

                }
            }

            @Override
            public void onRVIDidFailToReceivePacket(Throwable error) {
                Log.d(TAG, Util.getMethodName() + ": " + ((error == null) ? "(null)" : error.getLocalizedMessage()));
            }

            @Override
            public void onRVIDidSendPacket(DlinkPacket packet) {
                if (packet == null) return;

                Log.d(TAG, Util.getMethodName() + ": " + packet.getClass().toString());
                if (packet.getClass().equals(DlinkAuthPacket.class))
                    announceServices();
            }

            @Override
            public void onRVIDidFailToSendPacket(Throwable error) {
                Log.d(TAG, Util.getMethodName() + ": " + ((error == null) ? "(null)" : error.getLocalizedMessage()));
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
        void nodeDidFailToConnect(Throwable trigger);

        /**
         * Called when the local RVI node disconnects from a remote RVI node.
         */
        void nodeDidDisconnect(Throwable trigger);
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
        return mIsConnected;
    }

    /**
     * Tells the local RVI node to connect to the remote RVI node, letting the RVINode choose the best connection.
     */
    public void connect() {
        mRemoteConnectionManager.connect();
    }

    /**
     * Tells the local RVI node to disconnect all connections to the remote RVI node.
     */
    public void disconnect() {
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
}
