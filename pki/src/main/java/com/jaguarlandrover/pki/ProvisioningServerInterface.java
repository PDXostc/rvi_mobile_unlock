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

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Base64;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
//import java.net.URL;
import java.net.URI;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

public class ProvisioningServerInterface {
    private final static String TAG = "UnlockDemo:ProvServIntr";

    public static void sendCSR(Context context) {
        new CSRSendTask(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static class CSRSendTask extends AsyncTask<Void, String, Void>
    {
        Context mContext;

        CSRSendTask(Context context) {
            mContext = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
            byte [] csr = KeyManager.getCSR(mContext, "test1");

            if (csr == null) return null;

            URL url;
            String response = "";

            try {
                String string = convertToPem(csr);

                Log.d(TAG, "CSR encoded: " + string);

                url = new URL("http://192.168.16.245:5000/csr");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Log.d(TAG, "Sending CSR...");

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(string);

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

        static String convertToPem(byte [] derCert) throws CertificateEncodingException {
            String cert_begin = "-----BEGIN CERTIFICATE-----\n";
            String end_cert = "-----END CERTIFICATE-----";

            String pemCertPre = new String(Base64.encode(derCert));
            return cert_begin + pemCertPre + end_cert;
        }
    }
}
