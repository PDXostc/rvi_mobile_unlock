package com.jaguarlandrover.rvi;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2015 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    ServerConnection.java
 * Project: RVI SDK
 *
 * Created by Lilli Szafranski on 5/19/15.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import android.os.AsyncTask;
import android.util.Log;

import javax.net.SocketFactory;
import javax.net.ssl.*;
import java.io.*;
import java.security.*;

/**
 * The TCP/IP server @RemoteConnectionInterface implementation
 */
class ServerConnection implements RemoteConnectionInterface
{
    private final static String TAG = "RVI:ServerConnection";
    private RemoteConnectionListener mRemoteConnectionListener;

    private String   mServerUrl;
    private Integer  mServerPort;
    private KeyStore mClientKeyStore;
    private KeyStore mServerKeyStore;
    private String   mClientKeyStorePassword;

    /**
     * The socket.
     */
    private SSLSocket mSocket;

    @Override
    public void sendRviRequest(DlinkPacket dlinkPacket) {
        if (!isConnected() || !isConfigured()) { // TODO: Call error on listener

            mRemoteConnectionListener.onDidFailToSendDataToRemoteConnection(new Throwable("RVI node is not connected")); // TODO: Provide better feedback mechanism for when service invocations fail because node isn't connected!
            return;
        }

        new SendDataTask(dlinkPacket).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);//, dlinkPacket.toJsonString());
    }

    @Override
    public boolean isConnected() {
        return mSocket != null && mSocket.isConnected();
    }

    @Override
    public boolean isConfigured() {
        return !(mServerUrl == null || mServerUrl.isEmpty() || mServerPort == 0 || mClientKeyStore == null || mServerKeyStore == null);
    }

    @Override
    public void connect() {
        if (isConnected()) disconnect(null); // TODO: If relaunching, but not rebuilding, this may already be connected, in which case disconnecting may be unnecessary?
                                             // TODO: Figure out exactly what the right behavior should be in this case
        connectSocket();
    }

    @Override
    public void disconnect(Throwable trigger) { // TODO: If we get exceptions on our sending/listening tasks, we disconnect, but maybe we don't need to? E.g., javax.net.ssl.SSLException: Read error: ssl=0x57f2cca0: I/O error during system call, Connection timed out
                                                // TODO, cont'd: 01-08 09:53:32.740 9743-9761/com.jaguarlandrover.hvacdemo W/System.err:     at com.android.org.conscrypt.NativeCrypto.SSL_read(Native Method)
        try {
            if (mSocket != null)
                mSocket.close(); // TODO: Put on background thread (and probably do in BluetoothConnection too)

            mSocket = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mRemoteConnectionListener != null && trigger != null) mRemoteConnectionListener.onRemoteConnectionDidDisconnect(trigger);
    }

    @Override
    public void setRemoteConnectionListener(RemoteConnectionListener remoteConnectionListener) {
        mRemoteConnectionListener = remoteConnectionListener;
    }

    private void connectSocket() {
        Log.d(TAG, "Connecting the socket: " + mServerUrl + ":" + mServerPort);

        ConnectTask connectAndAuthorizeTask = new ConnectTask(mServerUrl, mServerPort, mServerKeyStore, mClientKeyStore, mClientKeyStorePassword);
        connectAndAuthorizeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class ConnectTask extends AsyncTask<Void, String, Throwable>
    {
        /**
         * The destination address.
         */
        String   dstAddress;
        /**
         * The destination port.
         */
        int      dstPort;
        /**
         * The key store of the server certs.
         */
        KeyStore serverKeyStore;
        /**
         * The key store of the client certs.
         */
        KeyStore clientKeyStore;
        /**
         * The key store password of the client certs.
         */
        String   clientKeyStorePassword;

        /**
         * Instantiates a new Connect task.
         * @param addr the addr
         * @param port the port
         * @param sks the server key store
         * @param cks the client key store
         * @param cksPass the client key store password
         */
        ConnectTask(String addr, int port, KeyStore sks, KeyStore cks, String cksPass) {
            dstAddress = addr;
            dstPort = port;
            clientKeyStore = cks;
            serverKeyStore = sks;
            clientKeyStorePassword = cksPass;
        }


//        private KeyStore getKeyStore(String fileName, String type, String password) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException { // type = "jks"?
////            AssetManager assetManager = getAssetManager();
////            AssetFileDescriptor fileDescriptor = assetManager.openFd(fileName);
////            FileInputStream stream = fileDescriptor.createInputStream();
//            File file = new File(fileName);
//            FileInputStream fis = new FileInputStream(file);
//            KeyStore ks = KeyStore.getInstance(type);
//            ks.load(fis, password.toCharArray());
//            fis.close(); // Now we initialize the TrustManagerFactory with this KeyStore
//
//            return ks;
//        }
//
//        private KeyManager[] getKeyManagers(String keyStoreFileName, String keyStoreType, String keyStorePassword) throws IOException, GeneralSecurityException { // First, get the default KeyManagerFactory.
//            String alg = KeyManagerFactory.getDefaultAlgorithm();
//            KeyManagerFactory kmFact = KeyManagerFactory.getInstance(alg); // Next, set up the KeyStore to use. We need to load the file into // a KeyStore instance.
//            kmFact.init(getKeyStore(keyStoreFileName, keyStoreType, keyStorePassword), keyStorePassword.toCharArray()); // And now get the TrustManagers
//            KeyManager[] kms = kmFact.getKeyManagers();
//
//            return kms;
//        }


        @Override
        protected Throwable doInBackground(Void... params) {

            try {
                Log.d(TAG, "Creating socket factory");

                String trustManagerAlgorithm = "X509";//TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(trustManagerAlgorithm);
                trustManagerFactory.init(serverKeyStore);

                String keyManagerAlgorithm = "X509";//KeyManagerFactory.getDefaultAlgorithm();
                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(keyManagerAlgorithm);
                keyManagerFactory.init(clientKeyStore, clientKeyStorePassword.toCharArray());

                SSLContext context = SSLContext.getInstance("TLS");
                context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

                SocketFactory sf = context.getSocketFactory();

                Log.d(TAG, "Creating ssl socket");

                mSocket = (SSLSocket) sf.createSocket(dstAddress, dstPort);

                Log.d(TAG, "Creating ssl socket complete");

            } catch (Exception e) {
                e.printStackTrace();

                return e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Throwable result) {
            super.onPostExecute(result);

            if (result == null) {
                // TODO: Does the input buffer stream cache data in the case that my async thread sends the auth command before the listener is set up?
                ListenTask listenTask = new ListenTask();
                listenTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                if (mRemoteConnectionListener != null)
                    mRemoteConnectionListener.onRemoteConnectionDidConnect();
            } else {
                if (mRemoteConnectionListener != null)
                    mRemoteConnectionListener.onRemoteConnectionDidFailToConnect(result);

                mSocket = null;
            }
        }
    }

    private class ListenTask extends AsyncTask<Void, String, Throwable>
    {
        @Override
        protected Throwable doInBackground(Void... params) {
            Log.d(TAG, "Listening on socket...");

            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];

                int bytesRead;
                InputStream inputStream = mSocket.getInputStream();

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);

                    publishProgress(byteArrayOutputStream.toString("UTF-8"));
                    byteArrayOutputStream.reset();
                }
            } catch (Exception e) {
                e.printStackTrace();

                return e;
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... params) {
            super.onProgressUpdate(params);

            String data = params[0];

            if (mRemoteConnectionListener != null) mRemoteConnectionListener.onRemoteConnectionDidReceiveData(data);
        }

        @Override
        protected void onPostExecute(Throwable result) {
            super.onPostExecute(result);

            disconnect(result);
        }
    }

    // TODO: Extract SendDataTask and ListenTask to their own classes that can be called by both BluetoothConnection and ServerConnection
    private class SendDataTask extends AsyncTask<Void, Void, Throwable>
    {
        private DlinkPacket mPacket;
        SendDataTask(DlinkPacket packet) {
            mPacket = packet;
        }

        @Override
        protected Throwable doInBackground(Void... params) {

            String data = mPacket.toJsonString();//params[0];
            Log.d(TAG, "Sending packet: " + data);

            try {
                DataOutputStream wr = new DataOutputStream(mSocket.getOutputStream());

                wr.writeBytes(data);
                wr.flush();
            } catch (Exception e) {
                e.printStackTrace();

                return e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Throwable result) {
            if (result == null) {
                if (mRemoteConnectionListener != null) mRemoteConnectionListener.onDidSendDataToRemoteConnection(mPacket);
            } else {
                if (mRemoteConnectionListener != null) mRemoteConnectionListener.onDidFailToSendDataToRemoteConnection(result);

                disconnect(result);
            }
        }
    }

    //public String getServerUrl() {
    //    return mServerUrl;
    //}

    /**
     * Sets server url.
     *
     * @param serverUrl the server url
     */
    void setServerUrl(String serverUrl) {
        mServerUrl = serverUrl;
    }

    //public Integer getServerPort() {
    //    return mServerPort;
    //}

    /**
     * Sets server port.
     *
     * @param serverPort the server port
     */
    void setServerPort(Integer serverPort) {
        mServerPort = serverPort;
    }

    /**
     * Sets the key store of the server certs
     * @param serverKeyStore the key store with the trusted server's cert used in TLS handshake
     */
    public void setServerKeyStore(KeyStore serverKeyStore) {
        mServerKeyStore = serverKeyStore;
    }

    /**
     * Sets the key store of the client certs
     * @param clientKeyStore the key store with the trusted client's cert used in TLS handshake
     */
    public void setClientKeyStore(KeyStore clientKeyStore) {
        mClientKeyStore = clientKeyStore;
    }

    /**
     * Sets the key store of the client certs
     * @param clientKeyStorePassword the password of the key store with the trusted client's cert
     */
    void setClientKeyStorePassword(String clientKeyStorePassword) {
        mClientKeyStorePassword = clientKeyStorePassword;
    }
}
