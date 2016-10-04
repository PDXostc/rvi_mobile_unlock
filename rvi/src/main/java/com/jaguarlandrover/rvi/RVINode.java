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
import android.util.Log;

import java.nio.ByteBuffer;
import java.security.KeyStore;
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

    private ArrayList<String>              mCredentials             = new ArrayList<>();
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

                mRemoteConnectionManager.sendPacket(new DlinkAuthPacket(mCredentials));
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

    /**
      * Method to pass the SDK your app's JWT-encoded json credentials for invoking services on a remote node and receiving service invocations from a remote node.
      *
      * @param jwtString a jwt-encoded credentials string
      */
    public void addJWTCredentials(String jwtString) {
        mCredentials.add(jwtString);
    }

    /**
     * Sets the server port of the remote RVI node, when using a TCP/IP link to interface with a remote node.
     *
     * @param serverKeyStore the KeyStore object that contains your server's self-signed certificate that the TLS connection should accept.
     *                 To make this KeyStore object, use BouncyCastle (http://www.bouncycastle.org/download/bcprov-jdk15on-146.jar), and
     *                 this command-line command:
     *                 $ keytool -import -v -trustcacerts -alias 0 \
     *                 -file [PATH_TO_SELF_CERT.PEM] \
     *                 -keystore [PATH_TO_KEYSTORE] \
     *                 -storetype BKS \
     *                 -provider org.bouncycastle.jce.provider.BouncyCastleProvider \
     *                 -providerpath [PATH_TO_bcprov-jdk15on-146.jar] \
     *                 -storepass [STOREPASS]
     * @param clientKeyStore the KeyStore object that contains your client's self-signed certificate that the TLS connection sends to the server.
     *                       // TODO: openssl pkcs12 -export -in insecure_device_cert.crt -inkey insecure_device_key.pem -out client.p12 -name "client-certs"
     * @param clientKeyStorePassword the password of the client key store
     */
    public void setKeyStores(KeyStore serverKeyStore, KeyStore clientKeyStore, String clientKeyStorePassword) {
        mRemoteConnectionManager.setKeyStores(serverKeyStore, clientKeyStore, clientKeyStorePassword);
    }

    /**
     * Sets the device address of the remote Bluetooth receiver on the remote RVI node, when using a Bluetooth link to interface with a remote node.
     *
     * @param deviceAddress the Bluetooth device address
     */
    public void setBluetoothDeviceAddress(String deviceAddress) {
        mRemoteConnectionManager.setBluetoothDeviceAddress(deviceAddress);
    }

    /**
     * Sets the Bluetooth service record identifier of the remote RVI node, when using a Bluetooth link to interface with a remote node.
     *
     * @param serviceRecord the service record identifier
     */
    public void setBluetoothServiceRecord(UUID serviceRecord) {
        /*RemoteConnectionManager.ourInstance.*/mRemoteConnectionManager.setBluetoothServiceRecord(serviceRecord);
    }

    /**
     * Sets the Bluetooth channel of the remote RVI node, when using a Bluetooth link to interface with a remote node.
     *
     * @param channel the channel
     */
    public void setBluetoothChannel(Integer channel) {
        /*RemoteConnectionManager.ourInstance.*/mRemoteConnectionManager.setBluetoothChannel(channel);
    }

    public boolean isConnected() {
        return mIsConnected;
    }

    private void connect(RemoteConnectionManager.ConnectionType type) {
        mRemoteConnectionManager.connect(type);//, RemoteConnection.Status.NA, RemoteConnection.Descriptor.NONE));
    }

    private void disconnect(RemoteConnectionManager.ConnectionType type) {
        mRemoteConnectionManager.disconnect(type);//, RemoteConnection.Status.NA, RemoteConnection.Descriptor.DISCONNECTED_APP_INITIATED));
    }

    /**
     * Tells the local RVI node to connect to the remote RVI node using a TCP/IP connection.
     */
    public void connectServer() {
        this.connect(RemoteConnectionManager.ConnectionType.SERVER);
    }

    /**
     * Tells the local RVI node to disconnect the TCP/IP connection to the remote RVI node.
     */
    public void disconnectServer() {
        this.disconnect(RemoteConnectionManager.ConnectionType.SERVER);
    }

    /**
     * Tells the local RVI node to connect to the remote RVI node using a Bluetooth connection.
     */
    public void connectBluetooth() {
       connect(RemoteConnectionManager.ConnectionType.BLUETOOTH);
    }

    /**
     * Tells the local RVI node to disconnect the Bluetooth to the remote RVI node.
     */
    public void disconnectBluetooth() {
        connect(RemoteConnectionManager.ConnectionType.BLUETOOTH);
    }

    /**
     * Tells the local RVI node to connect to the remote RVI node, letting the RVINode choose the best connection.
     */
    public void connect() {
        connect(RemoteConnectionManager.ConnectionType.GLOBAL);
    }

    /**
     * Tells the local RVI node to disconnect all connections to the remote RVI node.
     */
    public void disconnect() {
        disconnect(RemoteConnectionManager.ConnectionType.GLOBAL);
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
