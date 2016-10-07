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

import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * The remote RVI node.
 */
public class RVIRemoteNode implements RVILocalNode.LocalNodeListener {
    private final static String TAG = "RVI:RVINode";

    private RemoteConnectionManager mRemoteConnectionManager = new RemoteConnectionManager();

    private boolean mIsConnected = false;

    private HashMap<String, Service> mAuthorizedRemoteServices = new HashMap<>();
    private HashMap<String, Service> mAuthorizedLocalServices  = new HashMap<>();

    private HashMap<String, ArrayList<Service>> mPendingServiceInvocations = new HashMap<>();

    private ArrayList<Credential> mRemoteCredentials = new ArrayList<>();

    ArrayList<Credential> mValidRemoteCredentials = new ArrayList<Credential>();
    ArrayList<Credential> mValidLocalCredentials  = new ArrayList<Credential>();

    private Integer mRemotePort;
    private String  mRemoteAddr;

    public RVIRemoteNode(Context context) {
        mRemoteConnectionManager.setListener(new RemoteConnectionManagerListener()
        {
            @Override
            public void onRVIDidConnect() {
                Log.d(TAG, Util.getMethodName());

                openConnection();

                mIsConnected = true;
                if (mListener != null) mListener.nodeDidConnect(RVIRemoteNode.this);

                validateLocalCredentials();
                authorizeNode();
            }

            @Override
            public void onRVIDidFailToConnect(Throwable error) {
                Log.d(TAG, Util.getMethodName() + ": " + ((error == null) ? "(null)" : error.getLocalizedMessage()));

                closeConnection();

                mIsConnected = false;
                if (mListener != null) mListener.nodeDidFailToConnect(RVIRemoteNode.this, error);
            }

            @Override
            public void onRVIDidDisconnect(Throwable trigger) {
                Log.d(TAG, Util.getMethodName() + ": " + ((trigger == null) ? "(null)" : trigger.getLocalizedMessage()));

                closeConnection();

                mIsConnected = false;
                if (mListener != null) mListener.nodeDidDisconnect(RVIRemoteNode.this, trigger);
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

                // TODO: Get extra args and report to listener
            }

            @Override
            public void onRVIDidSendPacket(DlinkPacket packet) {
                if (packet == null) return;

                Log.d(TAG, Util.getMethodName() + ": " + packet.getClass().toString());

                //if (packet.getClass().equals(DlinkAuthPacket.class))
                //    announceServices(); // TODO: Not here....

                // TODO: Get extra args and report to listener
            }

            @Override
            public void onRVIDidFailToSendPacket(Throwable error) {
                Log.d(TAG, Util.getMethodName() + ": " + ((error == null) ? "(null)" : error.getLocalizedMessage()));

                // TODO: Get extra args and report to listener
            }
        });
    }

    private void openConnection() {

        RVILocalNode.addLocalNodeListener(RVIRemoteNode.this);
    }

    private void closeConnection() {

        mAuthorizedRemoteServices.clear();
        mAuthorizedLocalServices.clear();

        mValidRemoteCredentials.clear();
        mValidLocalCredentials.clear();

        mRemoteCredentials.clear();

        mRemotePort = null;
        mRemoteAddr = null;

        RVILocalNode.removeLocalNodeListener(RVIRemoteNode.this);
    }

    /**
     * Sets the @RVINodeListener listener.
     *
     * @param listener the listener
     */
    public void setListener(RVIRemoteNodeListener listener) {
        mListener = listener;
    }

    /**
     * The RVI node listener interface.
     */

    private RVIRemoteNodeListener mListener;

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
     * Gets a list of fully-qualified services names of all the local services.
     *
     * @return the local services
     */
    private ArrayList<String> getFullyQualifiedLocalServiceNames() {
        ArrayList<String> fullyQualifiedLocalServiceNames = new ArrayList<>(mAuthorizedLocalServices.size());
        for (Service service : mAuthorizedLocalServices.values())
            if (service.getFullyQualifiedServiceName() != null)
                fullyQualifiedLocalServiceNames.add(service.getFullyQualifiedServiceName());

        return fullyQualifiedLocalServiceNames;
    }

    /**
     * Gets the service object, given the service identifier. If one does not exist with that identifier,
     * it is created, but it is not added to the authorized remote services list.
     *
     * @param serviceIdentifier the service identifier
     * @return the service if it exists. If it does not exist, a new service object is created, but it is
     *         not added to the list of authorized remote services
     */
    private Service getRemoteService(String serviceIdentifier) {
        Service service;
        if (null != (service = mAuthorizedRemoteServices.get(serviceIdentifier)))
            return service;

        return new Service(null, null, null, serviceIdentifier);
    }

    private void queueServiceInvocation(String serviceIdentifier, Service service) {
        ArrayList<Service> pendingServiceInvocationList = mPendingServiceInvocations.get(serviceIdentifier);
        if (pendingServiceInvocationList != null) {
            pendingServiceInvocationList.add(service.copy());
        } else {
            mPendingServiceInvocations.put(serviceIdentifier, new ArrayList<>(Arrays.asList(service.copy())));
        }
    }

    /**
     * Add a remote service to the service bundle. If there is a pending service invocation with a matching service
     * identifier, this invocation is sent to the remote node.
     *
     * @param serviceIdentifier the identifier of the service
     */
    private void addRemoteService(String serviceIdentifier, Service service) {
        if (!mAuthorizedRemoteServices.containsKey(serviceIdentifier))
            mAuthorizedRemoteServices.put(serviceIdentifier, service);

        ArrayList<Service> pendingServiceInvocationList = mPendingServiceInvocations.get(serviceIdentifier);

        if (pendingServiceInvocationList != null) {
            for (Service pendingServiceInvocation : pendingServiceInvocationList) {
                if (pendingServiceInvocation.getTimeout() >= System.currentTimeMillis()) {
                    pendingServiceInvocation.setNodeIdentifier(service.getNodeIdentifier());
                    pendingServiceInvocation.setDomain(service.getDomain());

                    invokeService(service);
                }
            }

            mPendingServiceInvocations.remove(serviceIdentifier);
        }
    }

    /**
     * Invoke/update a remote service on the remote RVI node
     *
     * @param serviceIdentifier the service identifier
     * @param parameters the parameters
     * @param timeout the timeout, in milliseconds. This is added to the current system time.
     */
    public void invokeService(String serviceIdentifier, Object parameters, Integer timeout) {
        Service service = getRemoteService(serviceIdentifier);

        service.setParameters(parameters);
        service.setTimeout(System.currentTimeMillis() + timeout);

        if (service.hasNodeIdentifier())
            invokeService(service);
        else
            queueServiceInvocation(serviceIdentifier, service);
    }

    /**
     * Have the local node announce all it's available services.
     */
    private void announceServices() {
        mRemoteConnectionManager.sendPacket(new DlinkServiceAnnouncePacket(getFullyQualifiedLocalServiceNames()));
    }

    private void authorizeNode() {
        mRemoteConnectionManager.sendPacket(new DlinkAuthPacket(CredentialManager.toCredentialStringArray(mValidLocalCredentials)));
    }

    private void invokeService(Service service) {
        mRemoteConnectionManager.sendPacket(new DlinkReceivePacket(service));
    }

    private void handleReceivePacket(DlinkReceivePacket packet) {
        Service service = packet.getService();

        if (mListener != null) mListener.nodeReceiveServiceInvocationSucceeded(this, service.getServiceIdentifier(), service.getParameters());
    }

    private void handleServiceAnnouncePacket(DlinkServiceAnnouncePacket packet) {
        for (String fullyQualifiedRemoteServiceName : packet.getServices()) {

            String[] serviceParts = fullyQualifiedRemoteServiceName.split("/");

            if (serviceParts.length < 4) return; // TODO: Handle error

            String domain = serviceParts[0];
            String nodeIdentifier = serviceParts[1] + "/" + serviceParts[2];

            StringBuilder builder = new StringBuilder();
            for (Integer i = 3; i < serviceParts.length; i++) {
                builder.append(serviceParts[i]);

                if (i < serviceParts.length - 1)
                    builder.append("/");
            }

            String serviceIdentifier = builder.toString();

            addRemoteService(serviceIdentifier, new Service(domain, nodeIdentifier, null, serviceIdentifier));
        }

        sortThroughRemoteServices();
    }

    private void handleAuthPacket(DlinkAuthPacket packet) {
        mRemoteCredentials = CredentialManager.fromCredentialStringArray(packet.getCreds());
        mRemoteAddr = packet.getAddr();
        mRemotePort = packet.getPort();

        validateRemoteCredentials();
        sortThroughLocalServices();
        sortThroughRemoteServices();
        announceServices();
    }

    private void validateLocalCredentials() {
        Certificate[] remoteCertificates = mRemoteConnectionManager.getRemoteCertificates();
        Certificate[] localCertificates  = mRemoteConnectionManager.getLocalCertificates();

        ArrayList<Credential> localCredentials  = RVILocalNode.getCredentials();

        // Blah blah blah, validate credentials

        mValidLocalCredentials.addAll(localCredentials);
    }

    private void validateRemoteCredentials() {
        Certificate[] remoteCertificates = mRemoteConnectionManager.getRemoteCertificates();
        Certificate[] localCertificates  = mRemoteConnectionManager.getLocalCertificates();

        ArrayList<Credential> remoteCredentials = mRemoteCredentials;

        // Blah blah blah, validate credentials

        mValidRemoteCredentials.addAll(remoteCredentials);
    }

    private void sortThroughLocalServices() {
        Certificate[] remoteCertificates = mRemoteConnectionManager.getRemoteCertificates();
        Certificate[] localCertificates  = mRemoteConnectionManager.getLocalCertificates();

        ArrayList<Service> allLocalServices  = RVILocalNode.getLocalServices();
        ArrayList<Service> authorizedLocalServices  = RVILocalNode.getLocalServices();

        // Blah blah blah, validate services against credentials

        authorizedLocalServices = allLocalServices;

        mAuthorizedLocalServices.clear();

        for (Service service : authorizedLocalServices)
            mAuthorizedLocalServices.put(service.getServiceIdentifier(), service);
    }

    private void sortThroughRemoteServices() {
        Certificate[] remoteCertificates = mRemoteConnectionManager.getRemoteCertificates();
        Certificate[] localCertificates  = mRemoteConnectionManager.getLocalCertificates();

        ArrayList<Service> allRemoteServices = new ArrayList<Service>(mAuthorizedRemoteServices.values());
        ArrayList<Service> authorizedRemoteServices = new ArrayList<Service>(mAuthorizedRemoteServices.values());

        // Blah blah blah, validate services against credentials

        authorizedRemoteServices = allRemoteServices;

        mAuthorizedRemoteServices.clear();

        for (Service service : authorizedRemoteServices)
            mAuthorizedRemoteServices.put(service.getServiceIdentifier(), service);
    }

    @Override
    public void onLocalServicesUpdated() {
        sortThroughLocalServices();
        announceServices();
    }

    @Override
    public void onLocalCredentialsUpdated() {
        validateLocalCredentials();
        authorizeNode();

        sortThroughLocalServices();
        sortThroughRemoteServices();
        announceServices();
    }
}
