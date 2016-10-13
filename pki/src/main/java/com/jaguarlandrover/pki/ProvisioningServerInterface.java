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
 * Created by Lilli Szafranski on 9/27/16.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import org.spongycastle.util.encoders.Base64;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;

class ProvisioningServerInterface
{
    private final static String TAG = "PKI/ServerInterface____";

    private static Gson gson = new Gson();

    static void sendProvisioningServerRequest(Context context, PKIManager.ProvisioningServerListener listener, String baseUrl, String requestUrl, PKIServerRequest request) {
        String requestString = "";

        try {
            requestString = gson.toJson(request);
        } catch (Exception e) {
            e.printStackTrace();

            if (listener != null)
                listener.managerDidReceiveResponseFromServer(new PKIErrorResponse("Problem parsing the json. Request never sent to server"));
        }

        new ProvisioningServerTask(context, listener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, baseUrl, requestUrl, requestString);
    }

    private static class ProvisioningServerTask extends AsyncTask<String, String, Throwable>
    {
        Context mContext = null;
        PKIManager.ProvisioningServerListener mListener = null;

        ProvisioningServerTask(Context context, PKIManager.ProvisioningServerListener listener) {
            mContext  = context;
            mListener = listener;
        }

        @Override
        protected Throwable doInBackground(String... params) {
            String baseUrl     = params[0];
            String requestUrl  = params[1];
            String requestData = params[2];

            try {
                String serverResponse = sendStringToProvisioningServer(baseUrl, requestUrl, requestData);

                publishProgress(serverResponse);

            } catch (Exception e) {
                e.printStackTrace();

                return e;
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... params) {
            super.onProgressUpdate(params);

            String serverResponse = params[0];

            try {
                PKIServerResponse response = gson.fromJson(serverResponse, PKIServerResponse.class);

                if (response.getStatus() == PKIServerResponse.Status.ERROR)
                    response = gson.fromJson(serverResponse, PKIErrorResponse.class);
                else if (response.getStatus() == PKIServerResponse.Status.VERIFICATION_NEEDED)
                    response = gson.fromJson(serverResponse, PKIVerificationNeededResponse.class);
                else if (response.getStatus() == PKIServerResponse.Status.CERTIFICATE_RESPONSE)
                    response = gson.fromJson(serverResponse, PKICertificateResponse.class);

                if (response.getStatus() == PKIServerResponse.Status.CERTIFICATE_RESPONSE)
                    processServerCertificateResponse(mContext, (PKICertificateResponse) response);

                if (mListener != null)
                    mListener.managerDidReceiveResponseFromServer(response);

            } catch (Exception e) {
                if (mListener != null)
                    mListener.managerDidReceiveResponseFromServer(new PKIErrorResponse("Problem parsing the json. Request never sent to server"));
            }
        }

        @Override
        protected void onPostExecute(Throwable result) {
            super.onPostExecute(result);

            if (result != null) mListener.managerDidReceiveResponseFromServer(new PKIErrorResponse(result.getLocalizedMessage()));
        }
    }

    private static void processServerCertificateResponse(Context context, PKICertificateResponse serverCertificateResponse) throws CertificateException {

        byte [] decodedServerCert  = Base64.decode(serverCertificateResponse.getServerCertificate().replaceAll("-----BEGIN CERTIFICATE-----", "").replaceAll("-----END CERTIFICATE-----", ""));
        X509Certificate serverCert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(decodedServerCert));

        byte [] decodedDeviceCert  = Base64.decode(serverCertificateResponse.getDeviceCertificate().replaceAll("-----BEGIN CERTIFICATE-----", "").replaceAll("-----END CERTIFICATE-----", ""));
        X509Certificate deviceCert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(decodedDeviceCert));

        KeyStore serverKeyStore = KeyStoreManager.addServerCertToKeyStore(context, serverCert);
        KeyStore deviceKeyStore = KeyStoreManager.addDeviceCertToKeyStore(context, deviceCert);

        serverCertificateResponse.setServerKeyStore(serverKeyStore);
        serverCertificateResponse.setDeviceKeyStore(deviceKeyStore);
    }

    static String sendStringToProvisioningServer(String baseUrl, String requestUrl, String data) {

        if (data == null)
            throw new RuntimeException("Request string is null.");

        Log.d(TAG, "Request to server: " + data);

        URL url;
        String response = "";

        try {

            url = new URL(baseUrl + requestUrl);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            Log.d(TAG, "Sending data...");

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(data);

            writer.flush();
            writer.close();
            os.close();

            Log.d(TAG, "Data sent.");

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

            return response;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
