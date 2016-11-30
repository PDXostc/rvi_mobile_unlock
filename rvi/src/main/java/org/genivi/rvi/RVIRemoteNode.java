package org.genivi.rvi;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2015 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    RVINode.java
 * Project: RVI
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
public class RVIRemoteNode implements RVILocalNode.LocalNodeListener
{
    private final static String TAG = "RVI/RVIRemoteNode______";

    private RemoteConnectionManager mRemoteConnectionManager = new RemoteConnectionManager();

    private HashMap<String, Service> mAuthorizedRemoteServices = new HashMap<>();
    private HashMap<String, Service> mAuthorizedLocalServices  = new HashMap<>();

    private HashMap<String, Service>            mAnnouncedRemoteServices   = new HashMap<>();
    private HashMap<String, ArrayList<Service>> mPendingServiceInvocations = new HashMap<>();

    private ArrayList<Credential> mRemoteCredentials = new ArrayList<>();

    private ArrayList<Credential> mValidRemoteCredentials = new ArrayList<Credential>();
    private ArrayList<Credential> mValidLocalCredentials  = new ArrayList<Credential>();

    private Integer mRemotePort;
    private String  mRemoteAddr;

    private State mState;

    public enum State
    {
        CONNECTING,
        CONNECTED,
        DISCONNECTING,
        DISCONNECTED
    }

    public RVIRemoteNode(Context context) {
        mRemoteConnectionManager.setListener(new RemoteConnectionManagerListener()
        {
            @Override
            public void onRVIDidConnect() {
                Log.d(TAG, Util.getMethodName());

                openConnection();

                if (mState != State.CONNECTED) {
                    Log.d(TAG, "RVI REMOTE NODE CONNECTED");

                    mState = State.CONNECTED;
                    if (mListener != null) mListener.nodeDidConnect(RVIRemoteNode.this);
                }


                validateLocalCredentials();
                authorizeNode();
            }

            @Override
            public void onRVIDidFailToConnect(Throwable error) {
                Log.d(TAG, Util.getMethodName());

                closeConnection();

                if (mState != State.DISCONNECTED) {
                    Log.d(TAG, "RVI REMOTE NODE FAILED TO CONNECT" + ": " + ((error == null) ? "(null)" : error.getLocalizedMessage()));

                    mState = State.DISCONNECTED;
                    if (mListener != null) mListener.nodeDidFailToConnect(RVIRemoteNode.this, error);
                }
            }

            @Override
            public void onRVIDidDisconnect(Throwable trigger) {
                Log.d(TAG, Util.getMethodName());

                closeConnection();

                if (mState != State.DISCONNECTED) {
                    Log.d(TAG, "RVI REMOTE NODE DISCONNECTED" + ": " + ((trigger == null) ? "(null)" : trigger.getLocalizedMessage()));

                    mState = State.DISCONNECTED;
                    if (mListener != null) mListener.nodeDidDisconnect(RVIRemoteNode.this, trigger);
                }
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
            public void onRVIDidFailToReceivePacket(DlinkPacket packet, Throwable error) {
                Log.e(TAG, Util.getMethodName() + ": " + ((error == null) ? "(null)" : error.getLocalizedMessage()));

                if (packet.getClass() == DlinkReceivePacket.class) {
                    if (mListener != null) mListener.nodeReceiveServiceInvocationFailed(RVIRemoteNode.this, ((DlinkReceivePacket) packet).getService().getServiceIdentifier(), error);
                } else if (packet.getClass() == DlinkPacket.class && packet.mCmd == DlinkPacket.Command.RECEIVE) {
                    if (mListener != null) mListener.nodeReceiveServiceInvocationFailed(RVIRemoteNode.this, null, error);
                }
            }

            @Override
            public void onRVIDidSendPacket(DlinkPacket packet) {
                if (packet == null) return;

                if (packet.getClass() == DlinkReceivePacket.class)
                    if (mListener != null) mListener.nodeSendServiceInvocationSucceeded(RVIRemoteNode.this, ((DlinkReceivePacket) packet).getService().getServiceIdentifier());
            }

            @Override
            public void onRVIDidFailToSendPacket(DlinkPacket packet, Throwable error) {
                Log.e(TAG, Util.getMethodName() + ": " + ((error == null) ? "(null)" : error.getLocalizedMessage()));

                if (packet == null) {
                    if (mListener != null) mListener.nodeSendServiceInvocationFailed(RVIRemoteNode.this, null, error);
                } else if (packet.getClass() == DlinkReceivePacket.class) {
                    if (mListener != null) mListener.nodeSendServiceInvocationFailed(RVIRemoteNode.this, ((DlinkReceivePacket) packet).getService().getServiceIdentifier(), error);
                } else if (packet.getClass() == DlinkPacket.class && packet.mCmd == DlinkPacket.Command.RECEIVE) {
                    if (mListener != null) mListener.nodeSendServiceInvocationFailed(RVIRemoteNode.this, null, error);
                }
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
        return mState == State.CONNECTED;
    }

    /**
     * Tells the local RVI node to connect to the remote RVI node, letting the RVINode choose the best connection.
     */
    public void connect() {
        Log.d(TAG, "RVI REMOTE NODE CONNECTING...");

        mState = State.CONNECTING;

        mRemoteConnectionManager.setKeyStores(RVILocalNode.getServerKeyStore(), RVILocalNode.getDeviceKeyStore(), RVILocalNode.getDeviceKeyStorePassword());
        mRemoteConnectionManager.connect();
    }

    /**
     * Tells the local RVI node to disconnect all connections to the remote RVI node.
     */
    public void disconnect() {
        Log.d(TAG, "RVI REMOTE NODE DISCONNECTING...");

        mState = State.DISCONNECTING;

        mRemoteConnectionManager.disconnect();
    }

    public State getState() {
        return mState;
    }

    /**
     * Invoke/update a remote service on the remote RVI node
     *
     * @param serviceIdentifier the service identifier
     * @param parameters the parameters; must be a json-serializable object, that serializes into a json object
     * @param timeout the timeout, in milliseconds. This is added to the current system time.
     */
    public void invokeService(String serviceIdentifier, Object parameters, Integer timeout) {
        credentialsRevalidationCheck();

        Service service = getRemoteService(serviceIdentifier);

        service.setParameters(parameters);
        service.setTimeout(System.currentTimeMillis() + timeout);

        if (service.hasNodeIdentifier())
            invokeService(service);
        else
            queueServiceInvocation(serviceIdentifier, service);
    }

    /**
     * Returns whether or not remote service is authorized for invocation on the remote node from the local node
     * @param serviceIdentifier the service identifier
     * @return if it's authorized
     */
    public boolean isRemoteServiceAuthorized(String serviceIdentifier) {
        return mAuthorizedRemoteServices.containsKey(serviceIdentifier);
    }

    /**
     * Returns whether or not remote service is authorized for invocation on the local node from the remote node
     * @param serviceIdentifier the service identifier
     * @return if it's authorized
     */
    public boolean isLocalServiceAuthorized(String serviceIdentifier) {
        return mAuthorizedLocalServices.containsKey(serviceIdentifier);
    }

    /**
     * Gets a list of fully-qualified services names of all the local services.
     *
     * @return the local services
     */
    private ArrayList<String> getFullyQualifiedLocalServiceNames(HashMap<String, Service> services) {
        ArrayList<String> fullyQualifiedLocalServiceNames = new ArrayList<>(services.size());
        for (Service service : services.values())
            if (service.getFullyQualifiedServiceIdentifier() != null)
                fullyQualifiedLocalServiceNames.add(service.getFullyQualifiedServiceIdentifier());

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

        return new Service(null, null, serviceIdentifier);
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

                    invokeService(pendingServiceInvocation);
                }
            }

            mPendingServiceInvocations.remove(serviceIdentifier);
        }
    }

    private void credentialsRevalidationCheck() {
        if (CredentialManager.localCredentialsRevalidationNeeded()) {
            validateLocalCredentials();
            authorizeNode();
            sortThroughLocalServices();

            //sortThroughRemoteServices();
            //announceServices();
        }

        if (CredentialManager.remoteCredentialsRevalidationNeeded()) {
            validateRemoteCredentials();
            sortThroughLocalServices();
            sortThroughRemoteServices();

            //announceServices();
        }
    }

    /**
     * Have the local node announce all it's available services.
     */
    private void announceServices(HashMap<String, Service> services, DlinkServiceAnnouncePacket.Status status) {
        mRemoteConnectionManager.sendPacket(new DlinkServiceAnnouncePacket(getFullyQualifiedLocalServiceNames(services), status));
    }

    private void authorizeNode() {
        mRemoteConnectionManager.sendPacket(new DlinkAuthPacket(CredentialManager.toCredentialStringArray(mValidLocalCredentials)));
    }

    private void invokeService(Service service) {
        mRemoteConnectionManager.sendPacket(new DlinkReceivePacket(service));
    }

    private void handleReceivePacket(DlinkReceivePacket packet) {
        Service service = packet.getService();

        credentialsRevalidationCheck();

        if (!mAuthorizedLocalServices.containsKey(service.getServiceIdentifier())) {
            if (mListener != null) mListener.nodeReceiveServiceInvocationFailed(this, null, new Throwable("Service invocation packet did not contain a valid service identifier."));
            return;
        }

        if (!mAuthorizedLocalServices.get(service.getServiceIdentifier()).getFullyQualifiedServiceIdentifier().equals(service.getFullyQualifiedServiceIdentifier())) {
            if (mListener != null) mListener.nodeReceiveServiceInvocationFailed(this, service.getServiceIdentifier(), new Throwable("Local node is not authorized to receive this service."));
            return;
        }

        if (mListener != null) mListener.nodeReceiveServiceInvocationSucceeded(this, service.getServiceIdentifier(), service.getParameters());
    }

    private void handleServiceAnnouncePacket(DlinkServiceAnnouncePacket packet) {
        mAnnouncedRemoteServices.clear();

        for (String fullyQualifiedRemoteServiceName : packet.getServices()) {

            String[] serviceParts = fullyQualifiedRemoteServiceName.split("/", -1);

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

            mAnnouncedRemoteServices.put(serviceIdentifier, new Service(domain, nodeIdentifier, serviceIdentifier));
            //addRemoteService(serviceIdentifier, new Service(domain, nodeIdentifier, null, serviceIdentifier));
        }

        //if (mListener != null) mListener.nodeDidAuthorizeRemoteServices(this, mAuthorizedRemoteServices.keySet());

        if (packet.getStatus() == DlinkServiceAnnouncePacket.Status.AVAILABLE) {
            sortThroughRemoteServices();
        } else {
            for (String serviceIdentifier : mAnnouncedRemoteServices.keySet())
                mAuthorizedRemoteServices.remove(serviceIdentifier);

            if (mListener != null) mListener.nodeDidAuthorizeRemoteServices(this, mAuthorizedRemoteServices.keySet());
        }

    }

    private void handleAuthPacket(DlinkAuthPacket packet) {
        mRemoteCredentials = CredentialManager.fromCredentialStringArray(packet.getCreds());
        mRemoteAddr = packet.getAddr();
        mRemotePort = packet.getPort();

        validateRemoteCredentials();
        sortThroughLocalServices();

        mAuthorizedRemoteServices.clear(); // TODO: Check w Ulf about assumptions made wrt remote services and creds/auth and deleting list vs. adding to list and not checking against own creds
        if (mListener != null) mListener.nodeDidAuthorizeRemoteServices(this, mAuthorizedRemoteServices.keySet());
    }

    private void validateLocalCredentials() {
        Log.d(TAG, "Validating local credentials...");

        Certificate localCertificate  = mRemoteConnectionManager.getLocalDeviceCertificate();
        Certificate serverCertificate = mRemoteConnectionManager.getServerCertificate();

        if (localCertificate == null || serverCertificate == null) return; // TODO: There's a problem, but have we already handled it and disconnected?

        ArrayList<Credential> localCredentials = RVILocalNode.getCredentials();

        mValidLocalCredentials.clear();
        CredentialManager.clearLocalCredentialsRevalidationTime();

        for (Credential credential : localCredentials) {
            if (credential.parse(serverCertificate.getPublicKey()) && credential.deviceCertificateMatches(localCertificate)) {
                if (credential.getValidity().getStatus() == ValidityStatus.PENDING) {
                    CredentialManager.updateLocalCredentialsRevalidationTime(credential.getValidity().getStart());

                } else if (credential.getValidity().getStatus() == ValidityStatus.VALID) {
                    CredentialManager.updateLocalCredentialsRevalidationTime(credential.getValidity().getStop());

                    mValidLocalCredentials.add(credential);
                }
            }
        }

        Log.d(TAG, "Validated local credentials (valid credentials: " + mValidLocalCredentials.size() + " of " + localCredentials.size() + " total local credentials).");
    }

    private void validateRemoteCredentials() {
        Log.d(TAG, "Validating remote credentials...");

        Certificate remoteCertificate = mRemoteConnectionManager.getRemoteDeviceCertificate();
        Certificate serverCertificate = mRemoteConnectionManager.getServerCertificate();

        if (remoteCertificate == null || serverCertificate == null) return; // TODO: There's a problem, but have we already handled it and disconnected?

        ArrayList<Credential> remoteCredentials = mRemoteCredentials;

        mValidRemoteCredentials.clear();
        CredentialManager.clearRemoteCredentialsRevalidationTime();

        for (Credential credential : remoteCredentials) {
            if (credential.parse(serverCertificate.getPublicKey()) && credential.deviceCertificateMatches(remoteCertificate))
                if (credential.getValidity().getStatus() == ValidityStatus.PENDING) {
                    CredentialManager.updateRemoteCredentialsRevalidationTime(credential.getValidity().getStart());

                } else if (credential.getValidity().getStatus() == ValidityStatus.VALID) {
                    CredentialManager.updateRemoteCredentialsRevalidationTime(credential.getValidity().getStop());

                    mValidRemoteCredentials.add(credential);
            }
        }

        Log.d(TAG, "Validated remote credentials (valid credentials: " + mValidRemoteCredentials.size() + " of " + remoteCredentials.size() + " total remote credentials).");
    }

    private void sortThroughLocalServices() {
        Log.d(TAG, "Authorizing local services...");

        HashMap<String, Service> previouslyAuthorizedLocalServices = new HashMap<>(mAuthorizedLocalServices);

        ArrayList<Service> allLocalServices = RVILocalNode.getLocalServices();
        ArrayList<Service> authorizedToReceive = new ArrayList<>();
        ArrayList<Service> authorizedLocalServices = new ArrayList<>();

        for (Credential credential : mValidLocalCredentials) {
            for (Service service : allLocalServices) {
                if (credential.grantsRightToReceive(service.getFullyQualifiedServiceIdentifier()))
                    authorizedToReceive.add(service);
            }
        }

        for (Credential credential : mValidRemoteCredentials) {
            for (Service service : authorizedToReceive) {
                if (credential.grantsRightToInvoke(service.getFullyQualifiedServiceIdentifier()))
                    authorizedLocalServices.add(service);
            }
        }

        mAuthorizedLocalServices.clear();

        for (Service service : authorizedLocalServices)
            mAuthorizedLocalServices.put(service.getServiceIdentifier(), service);

        if (mListener != null) mListener.nodeDidAuthorizeLocalServices(this, mAuthorizedLocalServices.keySet());

        for (String serviceIdentifier: mAuthorizedLocalServices.keySet())
            previouslyAuthorizedLocalServices.remove(serviceIdentifier);

        Log.d(TAG, "Authorized local services (valid services: " + mAuthorizedLocalServices.size() + " of " + allLocalServices.size() + " total local services, "
                + previouslyAuthorizedLocalServices.size() + " previously authorized service(s) being removed).");

        announceServices(mAuthorizedLocalServices, DlinkServiceAnnouncePacket.Status.AVAILABLE);
        announceServices(previouslyAuthorizedLocalServices, DlinkServiceAnnouncePacket.Status.UNAVAILABLE);
    }

    private void sortThroughRemoteServices() {
        /* Combine any newly-announced services (if any) with existing services, and reparse the whole list */
        for (Service service : mAuthorizedRemoteServices.values())
            mAnnouncedRemoteServices.put(service.getServiceIdentifier(), service);

        ArrayList<Service> allRemoteServices = new ArrayList<Service>(mAnnouncedRemoteServices.values());
        ArrayList<Service> authorizedToInvoke = new ArrayList<>();
        ArrayList<Service> authorizedRemoteServices = new ArrayList<>();

        for (Credential credential : mValidLocalCredentials) {
            for (Service service : allRemoteServices) {
                if (credential.grantsRightToInvoke(service.getFullyQualifiedServiceIdentifier()))
                    authorizedToInvoke.add(service);
            }
        }

        for (Credential credential : mValidRemoteCredentials) {
            for (Service service : authorizedToInvoke) {
                if (credential.grantsRightToReceive(service.getFullyQualifiedServiceIdentifier()))
                    authorizedRemoteServices.add(service);
            }
        }

        mAuthorizedRemoteServices.clear();

        for (Service service : authorizedRemoteServices)
            addRemoteService(service.getServiceIdentifier(), service);

        Log.d(TAG, "Authorized remote services (valid services: " + mAuthorizedRemoteServices.size() + " of " + allRemoteServices.size() + " total remote services).");

        if (mListener != null) mListener.nodeDidAuthorizeRemoteServices(this, mAuthorizedRemoteServices.keySet());
    }

    @Override
    public void onLocalServicesUpdated() {
        Log.d(TAG, "Local services updated.");

        sortThroughLocalServices();
    }

    @Override
    public void onLocalCredentialsUpdated() {
        Log.d(TAG, "Local credentials updated.");

        validateLocalCredentials();
        authorizeNode();

        sortThroughLocalServices();

        sortThroughRemoteServices();
    }
}
