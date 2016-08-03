package com.jaguarlandrover.rvi;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2015 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    BluetoothConnection.java
 * Project: RVI SDK
 *
 * Created by Lilli Szafranski on 5/19/15.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.UUID;

/**
 * The Bluetooth @RemoteConnectionInterface implementation
 */
class BluetoothConnection implements RemoteConnectionInterface
{
    private final static String TAG = "RVI:BluetoothConnection";

    private RemoteConnectionListener mRemoteConnectionListener;

    private String  mDeviceAddress;
    private UUID    mServiceRecord;
    private Integer mChannel;

    /**
     * The socket.
     */
    private BluetoothSocket mSocket;

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    public void sendRviRequest(DlinkPacket dlinkPacket) {
        if (!isConnected() || !isConfigured()) // TODO: Call error on listener
            return;

        new SendDataTask(dlinkPacket).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);//, dlinkPacket.toJsonString());
    }

    @Override
    public boolean isConnected() {
        return mSocket != null && mSocket.isConnected();
    }

    @Override
    public boolean isConfigured() {
        return !(mDeviceAddress == null || mDeviceAddress.isEmpty() || mServiceRecord == null || mChannel == 0);
    }

    @Override
    public void connect() {
        if (isConnected()) disconnect(null);

        connectSocket();
    }

    @Override
    public void disconnect(Throwable trigger) {
        try {
            if (mSocket != null)
                mSocket.close();

            mSocket = null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mRemoteConnectionListener != null && trigger != null) mRemoteConnectionListener.onRemoteConnectionDidDisconnect(trigger);
    }

    @Override
    public void setRemoteConnectionListener(RemoteConnectionListener remoteConnectionListener) {
        mRemoteConnectionListener = remoteConnectionListener;
    }

    private void connectSocket() {
        Log.d(TAG, "Connecting to device: " + mDeviceAddress + ":" + mServiceRecord + ":" + mChannel);

        ConnectTask connectAndAuthorizeTask = new ConnectTask(mDeviceAddress, mServiceRecord, mChannel);
        connectAndAuthorizeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class ConnectTask extends AsyncTask<Void, String, Throwable>
    {
        /**
         * The Bluetooth addrress
         */
        String btAddr;
        /**
         * The Bluetooth service record.
         */
        UUID   btServiceRecord;
        /**
         * The Bluetooth channel
         */
        int    btChannel;

        /**
         * Instantiates a new Connect task.
         *
         * @param serviceRecord the serviceRecord
         * @param channel the channel
         */
        ConnectTask(String addr, UUID serviceRecord, int channel) {
            btAddr = addr;
            btServiceRecord = serviceRecord;
            btChannel = channel;
        }

        @Override
        protected Throwable doInBackground(Void... params) {
            Log.d(TAG, "Connecting Bluetooth socket: Checking Bluetooth adapter is enabled...");

            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
            }

            Log.d(TAG, "Connecting Bluetooth socket: Getting remote device...");
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(btAddr);

            // TODO: Fix the raspi Bluetooth code so I can use the below code (and remove the java reflection hack)
            /*
            try {
                mSocket = device.createRfcommSocketToServiceRecord(btServiceRecord);

                //device.createBond(); // TODO: Needed?

                mSocket.connect();
            } catch (Exception e) {
                e.printStackTrace()

                return e;
            }
            */

            // TODO: Fix the raspi Bluetooth code so I can remove the java reflection hack below (and use the above code above)
            try {
                Log.d(TAG, "Connecting Bluetooth socket: Creating socket...");

                Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                mSocket = (BluetoothSocket) m.invoke(device, btChannel);

                // Added as part of BT reset connection issue. See,
                // https://github.com/PDXostc/rvi_mobile_unlock/issues/4
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

                Log.d(TAG, "Connecting Bluetooth socket: Connecting socket...");

                mSocket.connect();
            } catch (Exception e) {
                e.printStackTrace();

                return e;
            }

            Log.d(TAG, "Connecting Bluetooth socket: Async task complete...");
            return null;
        }

        @Override
        protected void onPostExecute(Throwable result) {
            super.onPostExecute(result);

            if (result == null) {
                // TODO: Does the input buffer stream cache data in the case that my async thread sends the auth command before the listener is set up?
                Log.d(TAG, "Connecting Bluetooth socket: Creating listener...");

                ListenTask listenTask = new ListenTask();
                listenTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                if (mRemoteConnectionListener != null)
                    mRemoteConnectionListener.onRemoteConnectionDidConnect();
            } else {
                Log.d(TAG, "Connecting Bluetooth socket: Socket connection failed...");

                try {
                    mSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mSocket = null;

                if (mRemoteConnectionListener != null)
                    mRemoteConnectionListener.onRemoteConnectionDidFailToConnect(result);
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
            } catch (IOException e) {
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

    public void setDeviceAddress(String deviceAddress) {
        mDeviceAddress = deviceAddress;
    }

    void setServiceRecord(UUID serviceRecord) {
        mServiceRecord = serviceRecord;
    }

    void setChannel(Integer channel) {
        mChannel = channel;
    }
}
