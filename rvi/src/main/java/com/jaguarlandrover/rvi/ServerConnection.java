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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * The TCP/IP server @RemoteConnectionInterface implementation
 */
class ServerConnection implements RemoteConnectionInterface
{
    private final static String TAG = "RVI:ServerConnection";
    private RemoteConnectionListener mRemoteConnectionListener;

    private String  mServerUrl;
    private Integer mServerPort;

    /**
     * The socket.
     */
    Socket mSocket;

    @Override
    public void sendRviRequest(DlinkPacket dlinkPacket) {
        if (!isConnected() || !isEnabled()) // TODO: Call error on listener
            return;

        new SendDataTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, dlinkPacket.toJsonString());
    }

    @Override
    public boolean isConnected() {
        return mSocket != null && mSocket.isConnected();
    }

    @Override
    public boolean isEnabled() {
        return !(mServerUrl == null || mServerUrl.isEmpty() || mServerPort == 0);
    }

    @Override
    public void connect() {
        connectSocket();
    }

    @Override
    public void disconnect() {
        try {
            if (mSocket != null)
                mSocket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        if (mRemoteConnectionListener != null) mRemoteConnectionListener.onRemoteConnectionDidDisconnect();
    }

    @Override
    public void setRemoteConnectionListener(RemoteConnectionListener remoteConnectionListener) {
        mRemoteConnectionListener = remoteConnectionListener;
    }

    private void connectSocket() {
        Log.d(TAG, "Connecting the socket: " + mServerUrl + ":" + mServerPort);

        ConnectTask connectAndAuthorizeTask = new ConnectTask(mServerUrl, mServerPort);
        connectAndAuthorizeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private class ConnectTask extends AsyncTask<Void, String, Void> {
        /**
         * The destination address.
         */
        String dstAddress;
        /**
         * The destination port.
         */
        int    dstPort;

        /**
         * If the connection connected successfully or not.
         */
        boolean success = true;

        /**
         * Instantiates a new Connect task.
         *
         * @param addr the addr
         * @param port the port
         */
        ConnectTask(String addr, int port) {
            dstAddress = addr;
            dstPort = port;
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                mSocket = new Socket(dstAddress, dstPort);

            }
            catch (UnknownHostException e) {
                e.printStackTrace();

                success = false;

                if (mRemoteConnectionListener != null) mRemoteConnectionListener
                        .onRemoteConnectionDidFailToConnect(new Error("UnknownHostException: " + e.toString()));

            }
            catch (IOException e) {
                e.printStackTrace();

                success = false;

                if (mRemoteConnectionListener != null) mRemoteConnectionListener
                        .onRemoteConnectionDidFailToConnect(new Error("IOException: " + e.toString()));

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // TODO: Does the input buffer stream cache data in the case that my async thread sends the auth command before the listener is set up?
            ListenTask listenTask = new ListenTask();
            listenTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            if (success)
                if (mRemoteConnectionListener != null) mRemoteConnectionListener.onRemoteConnectionDidConnect();
        }
    }

    private class ListenTask extends AsyncTask<Void, String, Void>
    {

        @Override
        protected Void doInBackground(Void... params) {
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
            }
            catch (Exception e) {
                e.printStackTrace();

                publishProgress("Exception: " + e.toString());
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
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (mRemoteConnectionListener != null) mRemoteConnectionListener.onRemoteConnectionDidDisconnect();
        }
    }

    private class SendDataTask extends AsyncTask<String, Void, Void>
    {
        /**
         * If the connection sent the data successfully or not.
         */
        boolean success = true;

        @Override
        protected Void doInBackground(String... params) {

            String data = params[0];
            Log.d(TAG, "Sending packet: " + data);

            DataOutputStream wr = null;

            try {
                wr = new DataOutputStream(mSocket.getOutputStream());

                wr.writeBytes(data);
                wr.flush();
            }
            catch (IOException e) {
                e.printStackTrace();

                success = false;

                // TODO: Correctly catch and parse exceptions to detect if the socket is closed or if there was just an error
                // sending the packet, and report through remoteConnectionListener interface appropriately
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (success)
                if (mRemoteConnectionListener != null) mRemoteConnectionListener.onDidSendDataToRemoteConnection();
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

}
