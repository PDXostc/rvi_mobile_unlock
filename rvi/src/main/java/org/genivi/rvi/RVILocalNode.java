package org.genivi.rvi;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2016 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    RVILocalNode.java
 * Project: RVI
 *
 * Created by Lilli Szafranski on 10/4/16.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static android.content.Context.MODE_PRIVATE;

/**
 * This class represents your local RVI node, sitting right here in your Android application. This class and @RVIRemoteNode
 * are the main points of entry for you to interact with the Android RVI SDK. There is only one single instance of the
 * RVILocalNode class, encapsulated behind static methods. You need to set this class up first, and then you can use
 * RVIRemoteNode to make the connection between other remote nodes on your network. The RVIRemoteNode can't interact
 * with remote nodes without getting some configuration stuff from the local node.
 *
 * To set up the remote node, you need to do some PKI stuff. You need to generate a public/private key pair and a certificate
 * signing request. The CSR needs to contain your public key and be signed by your private key. You need to send this CSR to
 * your provisioning server and have the provisioning server sign your certificate with its private key and send that back to you.
 * You need to add this signed certificate to your Android keystore (the one containing your public/private key pair),
 * and you also need the server-self-signed server certificate as well. The server-self-signed certificate
 * should contain the server's public key and be signed by the server's private key. These signed certificates are used in
 * both the TLS upgrade to remote RVI nodes and in authorizing nodes and validating privileges.
 *
 * You need to provide the RVILocalNode with server-signed privileges too, which you should get from the provisioning
 * server. These privileges will need to contain your server-signed device certificate and will contain lists of
 * services that your node will be able to invoke/receive. This privileges object will need to be encoded in a JWT
 * and signed by the provisioning server's private key. You will always need to pass these to the RVILocalNode when you
 * start using RVI. You can have the RVILocalNode save the privileges for you, if you'd like, but this doesn't happen
 * explicitly.
 *
 * How you implement your provisioning server is up to you, but a lot of this stuff has been done already in the RVI PKI
 * Example project, found here: https://github.com/GENIVI/rvi_pki_android. Feel free to use this code! It will help a lot!
 * Just make sure that your provisioning server is expecting/sending back the json objects I used in the example, or
 * change it if you'd like!
 *
 * Once you've set up the local node with some keys and privileges, you can create an RVIRemoteNode object, pass it a server
 * url and port, and then connect. The RVIRemoteNode will authorize your app with the remote node, sending the privileges,
 * and comparing all the certificates used in the TLS upgrade. It will get a list of services from the RVILocalNode and a list
 * of remote services from over the network, and it will use the privileges to figure out what can be invoked and received across
 * the wire. You invoke services and receive services through this class. You should have one instance of this class for
 * each remote node you are connecting to. You should not have two instances of this class connecting to the same remote
 * node. If you do, I have absolutely no idea what will happen or how anything will get routed or if there will be awful
 * infinite loops. You have been warned!
 */
public class RVILocalNode {
    private final static String TAG = "RVI/RVILocalNode_______";

    private final static String SAVED_PRIVILEGES_FILE = "org.genivi.rvi.saved_privileges";

    private static RVILocalNode ourInstance = new RVILocalNode();

    private static ArrayList<Privilege> localPrivileges = new ArrayList<>();

    private static KeyStore serverKeyStore = null;
    private static KeyStore deviceKeyStore = null;
    private static String   deviceKeyStorePassword = null;

    private static String rviDomain = null;

    private static HashMap<String, Service> allLocalServices = new HashMap<>();

    private static ArrayList<LocalNodeListener> localNodeListeners = new ArrayList<>();

    private static RVILocalNode getInstance() {
        return ourInstance;
    }

    private RVILocalNode() {
    }

    interface LocalNodeListener
    {
        /** Called when local services have been added or remove so that RVIRemoteNodes can check if they are authorized */
        void onLocalServicesUpdated();

        /** Called when local services have been added or remove so that RVIRemoteNodes can check if they are authorized */
        void onLocalPrivilegesUpdated();
    }

    /**
     * Sets the domain part of your fully qualified service identifier. Must be RFC1035 compliant. Can't be null or empty.
     * Cannot contain any '/'s. Must only contain 'a-z', 'A-Z', '0-9', and '-' characters. E.g., "genivi.org" or "jaguarlandrover.com".
     *
     * @param rviDomain The domain.
     */
    public static void setRviDomain(String rviDomain) {
        RVILocalNode.rviDomain = Util.rfc1035(rviDomain);
    }

    /**
     * Get the RVI domain.
     *
     * @return The rvi domain.
     */
    public static String getRviDomain() {
        return rviDomain;
    }

    /**
     * Adds new local services.
     *
     * @param context            The current context. This value cannot be null.
     *
     * @param serviceIdentifiers A list of the identifiers for all the local services. Two forward-slashes in a row is also forbidden.
     *                           Leading and trailing forward-slashes are not allowed. This value cannot be an empty string or null.
     *                           The strings can't contain '+', '#', or the null unicode character. Must be UTF-8.
     *
     *
     * @exception java.lang.IllegalArgumentException Throws an exception when the context is null or if any of the service identifiers are
     *                                               empty strings, contain illegal characters, or are null.
     */
    public static void addLocalServices(Context context, ArrayList<String> serviceIdentifiers) {
        if (context == null) throw new IllegalArgumentException("Context can't be null");
        if (rviDomain == null) throw new IllegalStateException("RVI Domain must be set to a valid domain");

        if (serviceIdentifiers == null) return;

        for (String serviceIdentifier : serviceIdentifiers) {
            String validatedServiceIdentifier = Util.validated(serviceIdentifier);

            if (!allLocalServices.containsKey(validatedServiceIdentifier)) {
                allLocalServices.put(validatedServiceIdentifier, new Service(rviDomain, getLocalNodeIdentifier(context), validatedServiceIdentifier));
            }
        }

        Log.d(TAG, "Adding local services (" + serviceIdentifiers.size() + " new service(s), " + allLocalServices.size() + " total)");

        for (LocalNodeListener listener : localNodeListeners)
            listener.onLocalServicesUpdated();
    }

    /**
     * Removes local services.
     *
     * @param context            The current context. This value cannot be null.
     *
     * @param serviceIdentifiers A list of the identifiers for all the local services. Two forward-slashes in a row is also forbidden.
     *                           Leading and trailing forward-slashes are not allowed. This value cannot be an empty string or null.
     *                           The strings can't contain '+', '#', or the null unicode character. Must be UTF-8.
     */
    public static void removeLocalServices(Context context, ArrayList<String> serviceIdentifiers) {
        if (context == null) throw new IllegalArgumentException("Context can't be null");
        if (rviDomain == null) throw new IllegalStateException("RVI Domain must be set to a valid domain");

        if (serviceIdentifiers == null) return;

        for (String serviceIdentifier : serviceIdentifiers) {
            String validatedServiceIdentifier = Util.validated(serviceIdentifier);

            if (allLocalServices.containsKey(validatedServiceIdentifier)) {
                allLocalServices.remove(validatedServiceIdentifier);
            }
        }

        Log.d(TAG, "Removing local services (" + serviceIdentifiers.size() + " removed service(s), " + allLocalServices.size() + " remaining)");

        for (LocalNodeListener listener : localNodeListeners)
            listener.onLocalServicesUpdated();
    }

    /**
     * Gets the list of local services.
     *
     * @return The list of local services.
     */
    static ArrayList<Service> getLocalServices() {
        return new ArrayList<Service>(allLocalServices.values());
    }

    /**
     * Gets the server keystore.
     *
     * @return The server keystore.
     */
    static KeyStore getServerKeyStore() {
        return serverKeyStore;
    }

    /**
     * Sets the BKS key store containing the server's certificate.
     *
     * The server's certificate should contain the public key of the root provisioning server containing and it must
     * be signed by the matching private key of the server. This must be the same private key used to signed the JWT
     * privileges. It is passed to the local rvi node so that it can be used as both a trust authority to complete the
     * upgrade to TLS and to validate the JWT privileges sent by the local node and received from the remote node.
     *
     * The keystore needs to be loaded and can only contain 1 entry.
     *
     * The provider for the Android key store that contains the server certificate is "BKS"/"BC" (Bouncy Castle).
     *
     * The certificate should be in X509 format.
     *
     * @param serverKeyStore The server key store.
     */
    public static void setServerKeyStore(KeyStore serverKeyStore) { // TODO: Confirm that the keystore has been loaded and that it only contains one entry
        RVILocalNode.serverKeyStore = serverKeyStore;
    }

    /**
     * Gets the device keystore.
     *
     * @return The device keystore.
     */
    static KeyStore getDeviceKeyStore() {
        return deviceKeyStore;
    }

    /**
     * Sets the device key store. The key store may or may not be password protected. If there is a password. Use
     * the method setDeviceKeyStorePassword(deviceKeyStorePassword) to set the password.
     *
     * The key store must contain a unique public/private key pair that is unique to this device. It must also
     * contain an X509 certificate that contains the device's public key and is signed by the device's private
     * key. This certificate is further signed with the private key of the root provisioning server. The server
     * key that signs this certificate must be the same private key that signs all privileges passed between the
     * remote and local rvi nodes and the same private key used to sign the trust authority certificate used
     * by RVI during upgrade to TLS.
     *
     * In detail, generate a public and private key pair. Generate an X509 certificate signing request. Embed
     * the device's public key in the certificate signing request. This public key is essentially this device's
     * identity. Sign the certificate signing request with the device's private key, which guarantees that the
     * certificate containing the device's public key does, in fact, belong to that device, as it was signed
     * by the device's private key. We now know that the certificate and public key have not been tampered with.
     *
     * Send the certificate signing request to the provisioning server over a secure connection (e.g., https).
     * In this case, the provisioning server should establish trust through an existing well-known Trust Authority.
     * This step is all done out-of-band of RVI, so it is up to the implementer to guarantee the security of
     * the certificate signing.
     *
     * Once the server receives the certificate singing request, it is now going to assume the role of root
     * trust authority. The server should sign the device's certificate signing request with its own private
     * key. The server must also create its own certificate which contains the server's public key corresponding
     * to the private key used in signing the device's certificate. This server will need to generate its own
     * certificate, containing the server's public key. The server's certificate will be self-signed by the same
     * private key used in all JWT privileges sent by the local node and received from the remote node.
     *
     * Much of this can been done for you by the Genivi RVI PKI Android reference module, found here:
     * https://github.com/GENIVI/rvi_pki_android.
     *
     * @param deviceKeyStore The device key store.
     */
    public static void setDeviceKeyStore(KeyStore deviceKeyStore) {
        RVILocalNode.deviceKeyStore = deviceKeyStore;
    }

    /**
     * Gets the device keystore password.
     *
     * @return The device keystore password.
     */
    static String getDeviceKeyStorePassword() {
        return deviceKeyStorePassword;
    }

    /**
     * Sets the device key store password, if the device key store requires a password.
     *
     * @param deviceKeyStorePassword The device key store password.
     */
    public static void setDeviceKeyStorePassword(String deviceKeyStorePassword) {
        RVILocalNode.deviceKeyStorePassword = deviceKeyStorePassword;
    }

    /**
     * Tells the RVILocalNode to save its list of privileges to disk. This must be called explicitly if you want the
     * convenience of the RVILocalNode saving your JWT privileges to disk. Otherwise, it is up to you to save and load
     * them yourself and hand them to the local node every time you start up your application.
     *
     * @param context The current context.
     */
    public static void savePrivileges(Context context) {
        Log.d(TAG, "Saving local privileges (count: " + localPrivileges.size() + ")");

        Gson gson = new Gson();
        String jsonString = gson.toJson(PrivilegeManager.toPrivilegeStringArray(localPrivileges));

        try {
            FileOutputStream fileOutputStream = context.openFileOutput(SAVED_PRIVILEGES_FILE, Context.MODE_PRIVATE);
            fileOutputStream.write(jsonString.getBytes());
            fileOutputStream.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    /**
     * Tells the RVILocalNode to load its list of privileges from disk. This must be called explicitly if you want the
     * convenience of the RVILocalNode lading your saved JWT privileges from disk. Otherwise, it is up to you to save
     * and load them yourself and hand them to the local node every time you start up your application.
     *
     * @param context The current context.
     */
    public static void loadPrivileges(Context context) {
        Gson gson = new Gson();

        File file = context.getFileStreamPath(SAVED_PRIVILEGES_FILE);
        if (file != null && file.exists()) {
            try {
                FileInputStream fileInputStream = context.openFileInput(SAVED_PRIVILEGES_FILE);
                int c;
                String jsonString = "";

                while ((c = fileInputStream.read()) != -1) {
                    jsonString = jsonString + Character.toString((char)c);
                }

                // TODO: Handle all kinds of errors here
                localPrivileges = PrivilegeManager.fromPrivilegeStringArray((ArrayList<String>) gson.fromJson(jsonString, new TypeToken<ArrayList<String>>(){}.getType()));

                Log.d(TAG, "Loading saved privileges (count: " + localPrivileges.size() + ")");

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }

    /**
     * Returns the list of local privileges.
     *
     * @return The list of local privileges.
     */
    static ArrayList<Privilege> getPrivileges() {
        return localPrivileges;
    }

    /**
     * Sets the privileges for the local rvi node. Replaces the current list of privileges. If you want the node to save the privileges
     * you must call the savePrivileges(context) method explicitly. If you want the node to load privileges you have previously saved, you
     * must call loadPrivileges(context) explicitly.
     *
     * Calling this method while a remote RVI node is connected will re-initiate RVI authorization. When privileges change, the list
     * of valid remote and local services will also be updated.
     *
     * @param context The current context.
     * @param privilegeStrings A list of strings, where each string is a JWT encoded privilege. The JWTs should be signed by the provisioning
     *                         server and contain the local device's signed certificate. This certificate should be signed by the private
     *                         key of the device that corresponds to the public key that is embedded in the device's certificate. The
     *                         same public/private key pair needs to be made available to the local rvi node for use in the upgrade to
     *                         TLS when connecting to remote nodes. The server's certificate, containing the server's public key and signed
     *                         by the same private key used to signed the JWTs, must also be passed to the local rvi node so that it can
     *                         be used as both a trust authority to complete the upgrade to TLS and to validate the JWT privileges both
     *                         sent by the local node and received from the remote node.
     */
    public static void setPrivileges(Context context, ArrayList<String> privilegeStrings) {
        Log.d(TAG, "Setting new local privileges (count: " + privilegeStrings.size() + ")");

        localPrivileges = PrivilegeManager.fromPrivilegeStringArray(privilegeStrings);

        for (LocalNodeListener listener : localNodeListeners)
            listener.onLocalPrivilegesUpdated();
    }


    /**
     * Removes all the privileges from the local rvi node. If you want the node to save the now-empty privileges you must call the
     * savePrivileges(context) method explicitly.
     *
     * Calling this method while a remote RVI node is connected will re-initiate RVI authorization. When privileges change, the list
     * of valid remote and local services will also be updated.
     *
     * @param context The current context.
     */
    public static void removeAllPrivileges(Context context) {
        Log.d(TAG, "Removing all local privileges (count: " + localPrivileges.size() + ")");

        localPrivileges.clear();

        for (LocalNodeListener listener : localNodeListeners)
            listener.onLocalPrivilegesUpdated();
    }

    /**
     * Adds an RVIRemoteNode to the list of LocalNodeListeners. Used by all RVIRemoteNodes so that they can properly and dynamically
     * respond to changes in privileges and available services. Called when an RVIRemoteNode connects to a remote node over the network.
     *
     * @param listener The LocalNodeListener.
     */
    static void addLocalNodeListener(LocalNodeListener listener) {
        localNodeListeners.add(listener);
    }

    /**
     * Removes an RVIRemoteNode to the list of LocalNodeListeners. Called when an RVIRemoteNode disconnects from a remote node over the network.
     *
     * @param listener The LocalNodeListener.
     */
    static void removeLocalNodeListener(LocalNodeListener listener) {
        localNodeListeners.remove(listener);
    }

    private final static String SHARED_PREFS_STRING         = "com.rvisdk.settings";
    private final static String LOCAL_SERVICE_PREFIX_STRING = "localServicePrefix";
    
    /**
     * Creates the UUID. Uses the fact that the UUID is a bunch of base-16 characters to shorten the string through base 64 encoding.
     *
     * @return A uuid.
     */
    // TODO: Test and verify this function
    static String uuidB58String() {
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
     * Gets the node identifier of the local RVI node. The node identifier consists of the device type id (i.e., "android") and a universally unique identifier
     * for this device. The node identifier is used by RVI for routing service invocations to the correct remote nodes.
     *
     * @param context The application context.
     * @return The local prefix.
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

