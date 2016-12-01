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

import android.util.Log;

import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * This class represents a connection to a remote RVI node. This class and RVILocalNode are the main points of entry for you
 * to interact with the Android RVI SDK. There is only one single instance of the RVILocalNode class, encapsulated behind
 * static methods. The @RVILocalNode class represents, you, the Android application that is running RVI locally. You need
 * to set that up first. The RVIRemoteNode bridges the connection between your local node and another node on the network,
 * and it can't interact with that node without getting some configuration stuff from the local node.
 *
 * Once you've set up the local node, you can supply the remote node with some keys and privileges, a server url and port,
 * and then connect. The RVIRemoteNode will authorize your app with the remote node, sending the privileges, and comparing
 * all the certificates used in the TLS upgrade. It will get a list of services from the RVILocalNode and a list of remote
 * services from over the network, and it will use the privileges to figure out what can be invoked and received across the
 * wire. You invoke services and receive services through this class. You should have one instance of this class for
 * each remote node you are connecting to. You should not have two instances of this class connecting to the same remote
 * node. If you do, I have absolutely no idea what will happen or how anything will get routed or if there will be awful
 * infinite loops. You have been warned!
 */
public class RVIRemoteNode implements RVILocalNode.LocalNodeListener
{
    private final static String TAG = "RVI/RVIRemoteNode______";

    private RemoteConnectionManager mRemoteConnectionManager = new RemoteConnectionManager();

    private HashMap<String, Service> mAuthorizedRemoteServices = new HashMap<>();
    private HashMap<String, Service> mAuthorizedLocalServices  = new HashMap<>();

    private HashMap<String, Service>            mAnnouncedRemoteServices   = new HashMap<>();
    private HashMap<String, ArrayList<Service>> mPendingServiceInvocations = new HashMap<>();

    private ArrayList<Privilege> mRemotePrivileges = new ArrayList<>();

    private ArrayList<Privilege> mValidRemotePrivileges = new ArrayList<Privilege>();
    private ArrayList<Privilege> mValidLocalPrivileges  = new ArrayList<Privilege>();

    private Integer mRemotePort;
    private String  mRemoteAddr;

    private State mState;

    private RVIRemoteNodeListener mListener;

    public enum State
    {
        CONNECTING,
        CONNECTED,
        DISCONNECTING,
        DISCONNECTED
    }

    /**
     * The constructor of this class.
     */
    public RVIRemoteNode() {
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


                validateLocalPrivileges();
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

    /**
     * Finishes opening the connection to a remote rvi node.
     */
    private void openConnection() {

        RVILocalNode.addLocalNodeListener(RVIRemoteNode.this);
    }

    /**
     * Finishes tearing down a connection when disconnecting from a remote rvi node.
     */
    private void closeConnection() {

        mAuthorizedRemoteServices.clear();
        mAuthorizedLocalServices.clear();

        mValidRemotePrivileges.clear();
        mValidLocalPrivileges.clear();

        mRemotePrivileges.clear();

        mRemotePort = null;
        mRemoteAddr = null;

        RVILocalNode.removeLocalNodeListener(RVIRemoteNode.this);
    }

    /**
     * Sets the @RVINodeListener listener. This is how your calling application receives events and stuff from the remote node.
     *
     * @param listener The listener.
     */
    public void setListener(RVIRemoteNodeListener listener) {
        mListener = listener;
    }

    /**
     * Sets the server url to the remote RVI node, when using a TCP/IP link to interface with a remote node. You need to set this
     * before you can connect to the remote node (obviously).
     *
     * @param serverUrl The server url.
     */
    public void setServerUrl(String serverUrl) {
        mRemoteConnectionManager.setServerUrl(serverUrl);
    }

    /**
     * Sets the server port of the remote RVI node, when using a TCP/IP link to interface with a remote node. You need to set this
     * before you can connect to the remote node.
     *
     * @param serverPort The server port.
     */
    public void setServerPort(Integer serverPort) {
        mRemoteConnectionManager.setServerPort(serverPort);
    }

    /**
     * Tells you if the RVIRemoteNode class is currently connected to a remote node over the network.
     *
     * @return True if connected, false if not.
     */
    public boolean isConnected() {
        return mState == State.CONNECTED;
    }

    /**
     * Tells the RVIRemoteNode class to connect to the remote RVI node.
     *
     * This starts the upgrade to TLS, using the passed in server and device key stores. Once connected, the
     * RVIRemoteNode will authorize with the remote node, check everyone's privileges, announce available services,
     * and filter through the remote node's services. Any pending service invocations that haven't timed-out will
     * be invoked, if allowed by the privileges.
     */
    public void connect() {
        Log.d(TAG, "RVI REMOTE NODE CONNECTING...");

        mState = State.CONNECTING;

        mRemoteConnectionManager.setKeyStores(RVILocalNode.getServerKeyStore(), RVILocalNode.getDeviceKeyStore(), RVILocalNode.getDeviceKeyStorePassword());
        mRemoteConnectionManager.connect();
    }

    /**
     * Tells the RVIRemoteNode to disconnect its connection to the remote RVI node.
     */
    public void disconnect() {
        Log.d(TAG, "RVI REMOTE NODE DISCONNECTING...");

        mState = State.DISCONNECTING;

        mRemoteConnectionManager.disconnect();
    }

    /**
     * Returns the state of the connection.
     *
     * @return The state of the connection.
     */
    public State getState() {
        return mState;
    }

    /**
     * Invoke/update a remote service on the remote RVI node
     *
     * @param serviceIdentifier The service identifier of the service. You don't need to know the domain or the
     *                          node identifier, just the service identifier components. E.g., "hvac/temp" or "radio/cd/playlist"
     * @param parameters The parameters. Must be a json-serializable object, that serializes into a json object.
     * @param timeout The timeout, in milliseconds. This is added to the current system time. If the remote service is not currently
     *                available, the RVIRemoteNode will cash the request until the service does become available or the timeout is
     *                reached. If the timeout is reached, the service invocation will not go through.
     */
    public void invokeService(String serviceIdentifier, Object parameters, Integer timeout) {
        privilegesRevalidationCheck();

        Service service = getRemoteService(serviceIdentifier);

        service.setParameters(parameters);
        service.setTimeout(System.currentTimeMillis() + timeout);

        if (service.hasNodeIdentifier())
            invokeService(service);
        else
            queueServiceInvocation(serviceIdentifier, service);
    }

    /**
     * Returns whether or not remote service is authorized for invocation on the remote node from the local node.
     *
     * @param serviceIdentifier The service identifier.
     * @return If it's authorized.
     */
    public boolean isRemoteServiceAuthorized(String serviceIdentifier) {
        return mAuthorizedRemoteServices.containsKey(serviceIdentifier);
    }

    /**
     * Returns whether or not remote service is authorized for invocation on the local node from the remote node.
     *
     * @param serviceIdentifier The service identifier.
     * @return If it's authorized.
     */
    public boolean isLocalServiceAuthorized(String serviceIdentifier) {
        return mAuthorizedLocalServices.containsKey(serviceIdentifier);
    }

    /**
     * Gets a list of fully-qualified services names of all the local services.
     *
     * @return The local services.
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
     * @param serviceIdentifier The service identifier.
     * @return the service if it exists. If it does not exist, a new service object is created, but it is
     *         not added to the list of authorized remote services
     */
    private Service getRemoteService(String serviceIdentifier) {
        Service service;
        if (null != (service = mAuthorizedRemoteServices.get(serviceIdentifier)))
            return service;

        return new Service(null, null, serviceIdentifier);
    }

    /**
     * Queues a service invocation for when it becomes available.
     *
     * @param serviceIdentifier The service identifier.
     * @param service The service object with the parameters and stuff.
     */
    private void queueServiceInvocation(String serviceIdentifier, Service service) {
        ArrayList<Service> pendingServiceInvocationList = mPendingServiceInvocations.get(serviceIdentifier);
        if (pendingServiceInvocationList != null) {
            pendingServiceInvocationList.add(service.copy());
        } else {
            mPendingServiceInvocations.put(serviceIdentifier, new ArrayList<>(Arrays.asList(service.copy())));
        }
    }

    /**
     * Add a remote service to the list of remote services. If there is a pending service invocation
     * with a matching service identifier, this invocation is sent to the remote node.
     *
     * @param serviceIdentifier The service identifier.
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

    /**
     * Checks if the validity times of any of the remote/local privileges have been passed.
     */
    private void privilegesRevalidationCheck() {
        if (PrivilegeManager.localPrivilegesRevalidationNeeded()) {
            validateLocalPrivileges();
            authorizeNode();
            sortThroughLocalServices();
        }

        if (PrivilegeManager.remotePrivilegesRevalidationNeeded()) {
            validateRemotePrivileges();
            sortThroughLocalServices();
            sortThroughRemoteServices();
        }
    }

    /**
     * Have the node authorize itself.
     */
    private void authorizeNode() {
        mRemoteConnectionManager.sendPacket(new DlinkAuthPacket(PrivilegeManager.toPrivilegeStringArray(mValidLocalPrivileges)));
    }

    /**
     * Have the node announce/unannounce all it's available services.
     *
     * @param services The list of FQSIs.
     * @param status If the node is announcing services ("av") or unannouncing services ("un").
     */
    private void announceServices(HashMap<String, Service> services, DlinkServiceAnnouncePacket.Status status) {
        mRemoteConnectionManager.sendPacket(new DlinkServiceAnnouncePacket(getFullyQualifiedLocalServiceNames(services), status));
    }

    /**
     * Invoke a service.
     *
     * @param service The service object with all the parameters and stuff.
     */
    private void invokeService(Service service) {
        mRemoteConnectionManager.sendPacket(new DlinkReceivePacket(service));
    }

    /**
     * Handle a receive packet.
     *
     * @param packet The packet.
     */
    private void handleReceivePacket(DlinkReceivePacket packet) {
        Service service = packet.getService();

        privilegesRevalidationCheck();

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

    /**
     * Handle a service announce packet.
     *
     * @param packet The packet.
     */
    private void handleServiceAnnouncePacket(DlinkServiceAnnouncePacket packet) {
        mAnnouncedRemoteServices.clear();

        for (String fullyQualifiedRemoteServiceName : packet.getServices()) {

            String[] serviceParts = fullyQualifiedRemoteServiceName.split("/", -1);

            if (serviceParts.length < 4) continue;

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

        }

        if (packet.getStatus() == DlinkServiceAnnouncePacket.Status.AVAILABLE) {
            sortThroughRemoteServices();
        } else {
            for (String serviceIdentifier : mAnnouncedRemoteServices.keySet())
                mAuthorizedRemoteServices.remove(serviceIdentifier);

            if (mListener != null) mListener.nodeDidAuthorizeRemoteServices(this, mAuthorizedRemoteServices.keySet());
        }

    }

    /**
     * Handle an auth packet.
     *
     * @param packet The packet.
     */
    private void handleAuthPacket(DlinkAuthPacket packet) {
        mRemotePrivileges = PrivilegeManager.fromPrivilegeStringArray(packet.getPrivileges());
        mRemoteAddr = packet.getAddr();
        mRemotePort = packet.getPort();

        validateRemotePrivileges();
        sortThroughLocalServices();

        mAuthorizedRemoteServices.clear();
        if (mListener != null) mListener.nodeDidAuthorizeRemoteServices(this, mAuthorizedRemoteServices.keySet());
    }

    /**
     * Validate the local privileges. Make sure they are signed by the correct server key and contain the same server-signed device key used
     * in the TLS upgrade. Keep track of their validity start and stop times. And if they look good, put them in our valid privileges list.
     *
     * I wouldn't go messing around with this function too much!! Could break things unknowingly!
     */
    private void validateLocalPrivileges() {
        Log.d(TAG, "Validating local privileges...");

        Certificate localCertificate  = mRemoteConnectionManager.getLocalDeviceCertificate();
        Certificate serverCertificate = mRemoteConnectionManager.getServerCertificate();

        if (localCertificate == null || serverCertificate == null) return;

        ArrayList<Privilege> localPrivileges = RVILocalNode.getPrivileges();

        mValidLocalPrivileges.clear();
        PrivilegeManager.clearLocalPrivilegesRevalidationTime();

        for (Privilege privilege : localPrivileges) {
            if (privilege.parse(serverCertificate.getPublicKey()) && privilege.deviceCertificateMatches(localCertificate)) {
                if (privilege.getValidity().getStatus() == ValidityStatus.PENDING) {
                    PrivilegeManager.updateLocalPrivilegesRevalidationTime(privilege.getValidity().getStart());

                } else if (privilege.getValidity().getStatus() == ValidityStatus.VALID) {
                    PrivilegeManager.updateLocalPrivilegesRevalidationTime(privilege.getValidity().getStop());

                    mValidLocalPrivileges.add(privilege);
                }
            }
        }

        Log.d(TAG, "Validated local privileges (valid privileges: " + mValidLocalPrivileges.size() + " of " + localPrivileges.size() + " total local privileges).");
    }

    /**
     * Validate the remote privileges. Make sure they are signed by the correct server key and contain the same server-signed device key used
     * in the TLS upgrade. Keep track of their validity start and stop times. And if they look good, put them in our valid privileges list.
     *
     * I wouldn't go messing around with this function too much!! Could break things unknowingly!
     */
    private void validateRemotePrivileges() {
        Log.d(TAG, "Validating remote privileges...");

        Certificate remoteCertificate = mRemoteConnectionManager.getRemoteDeviceCertificate();
        Certificate serverCertificate = mRemoteConnectionManager.getServerCertificate();

        if (remoteCertificate == null || serverCertificate == null) return;

        ArrayList<Privilege> remotePrivileges = mRemotePrivileges;

        mValidRemotePrivileges.clear();
        PrivilegeManager.clearRemotePrivilegesRevalidationTime();

        for (Privilege privilege : remotePrivileges) {
            if (privilege.parse(serverCertificate.getPublicKey()) && privilege.deviceCertificateMatches(remoteCertificate))
                if (privilege.getValidity().getStatus() == ValidityStatus.PENDING) {
                    PrivilegeManager.updateRemotePrivilegesRevalidationTime(privilege.getValidity().getStart());

                } else if (privilege.getValidity().getStatus() == ValidityStatus.VALID) {
                    PrivilegeManager.updateRemotePrivilegesRevalidationTime(privilege.getValidity().getStop());

                    mValidRemotePrivileges.add(privilege);
            }
        }

        Log.d(TAG, "Validated remote privileges (valid privileges: " + mValidRemotePrivileges.size() + " of " + remotePrivileges.size() + " total remote privileges).");
    }

    /**
     * Go through the list of all available local services and compare them to the right_to_receive strings in all our local privileges
     * and the right_to_invoke string in all our remote privileges. If they match both, put them in our valid local services list.
     *
     * I wouldn't go messing around with this function too much!! Could break things unknowingly!
     */
    private void sortThroughLocalServices() {
        Log.d(TAG, "Authorizing local services...");

        HashMap<String, Service> previouslyAuthorizedLocalServices = new HashMap<>(mAuthorizedLocalServices);

        ArrayList<Service> allLocalServices = RVILocalNode.getLocalServices();
        ArrayList<Service> authorizedToReceive = new ArrayList<>();
        ArrayList<Service> authorizedLocalServices = new ArrayList<>();

        for (Privilege privilege : mValidLocalPrivileges) {
            for (Service service : allLocalServices) {
                if (privilege.grantsRightToReceive(service.getFullyQualifiedServiceIdentifier()))
                    authorizedToReceive.add(service);
            }
        }

        for (Privilege privilege : mValidRemotePrivileges) {
            for (Service service : authorizedToReceive) {
                if (privilege.grantsRightToInvoke(service.getFullyQualifiedServiceIdentifier()))
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

    /**
     * Go through the list of all available remote services and compare them to the right_to_receive strings in all our remote privileges
     * and the right_to_invoke string in all our local privileges. If they match both, put them in our valid local services list.
     *
     * I wouldn't go messing around with this function too much!! Could break things unknowingly!
     */
    private void sortThroughRemoteServices() {
        /* Combine any newly-announced services (if any) with existing services, and reparse the whole list */
        for (Service service : mAuthorizedRemoteServices.values())
            mAnnouncedRemoteServices.put(service.getServiceIdentifier(), service);

        ArrayList<Service> allRemoteServices = new ArrayList<Service>(mAnnouncedRemoteServices.values());
        ArrayList<Service> authorizedToInvoke = new ArrayList<>();
        ArrayList<Service> authorizedRemoteServices = new ArrayList<>();

        for (Privilege privilege : mValidLocalPrivileges) {
            for (Service service : allRemoteServices) {
                if (privilege.grantsRightToInvoke(service.getFullyQualifiedServiceIdentifier()))
                    authorizedToInvoke.add(service);
            }
        }

        for (Privilege privilege : mValidRemotePrivileges) {
            for (Service service : authorizedToInvoke) {
                if (privilege.grantsRightToReceive(service.getFullyQualifiedServiceIdentifier()))
                    authorizedRemoteServices.add(service);
            }
        }

        mAuthorizedRemoteServices.clear();

        for (Service service : authorizedRemoteServices)
            addRemoteService(service.getServiceIdentifier(), service);

        Log.d(TAG, "Authorized remote services (valid services: " + mAuthorizedRemoteServices.size() + " of " + allRemoteServices.size() + " total remote services).");

        if (mListener != null) mListener.nodeDidAuthorizeRemoteServices(this, mAuthorizedRemoteServices.keySet());
    }

    /**
     * When local services are updated in the @RVILocalNode class, we have to do a bunch of things over again.
     */
    @Override
    public void onLocalServicesUpdated() {
        Log.d(TAG, "Local services updated.");

        sortThroughLocalServices();
    }

    /**
     * When local privileges are updated in the @RVILocalNode class, we have to do a bunch of things over again.
     */
    @Override
    public void onLocalPrivilegesUpdated() {
        Log.d(TAG, "Local privileges updated.");

        validateLocalPrivileges();
        authorizeNode();

        sortThroughLocalServices();

        sortThroughRemoteServices();
    }
}
