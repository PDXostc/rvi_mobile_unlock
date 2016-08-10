package com.jaguarlandrover.pki;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2016 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    ProvisioningServerInterface.java
 * Project: UnlockDemo
 *
 * Created by Lilli Szafranski on 8/9/16.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;

import javax.net.SocketFactory;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

public class ProvisioningServerInterface {
    private final static String TAG = "UnlockDemo:ProvServIntr";

    public static void sendCSR() {
        new CSRSendTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static class CSRSendTask extends AsyncTask<Void, String, Void>
    {
        CSRSendTask() {

        }

        @Override
        protected Void doInBackground(Void... params) {
            byte [] csr = KeyManager.getCSR("test1");

            if (csr == null) return null;

            char[] chars = new String(csr).toCharArray();

            Log.d(TAG, "CSR encoded: " + new String(chars));

            URL url;
            String response = "";
            try {
                url = new URL("http://192.168.16.245:5000/csr");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Log.d(TAG, "Sending CSR...");

//                String hello = "hello";

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(chars);

                writer.flush();
                writer.close();
                os.close();

                Log.d(TAG, "CSR sent.");

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        response += line;
                    }
                } else {
                    response = "";
                }

                Log.d(TAG, "Response from server: " + response);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
