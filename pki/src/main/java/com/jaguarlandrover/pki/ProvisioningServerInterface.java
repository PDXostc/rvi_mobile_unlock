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

import org.spongycastle.util.encoders.Base64;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
//import java.net.URL;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import io.jsonwebtoken.Jwt;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;


public class ProvisioningServerInterface {
    private final static String TAG = "UnlockDemo:ProvServIntf";

    /**
     * The PKI manager listener interface.
     */
    public interface ProvisioningServerListener
    {
        /**
         * Called when the manager receives server-signed server certificate, server-signed device certificate, and list of server-signed jwt
         * privileges from the provisioning server (containing the server's public key).
         */
        void managerDidReceiveServerSignedStuff(KeyStore serverCertificateKeyStore, KeyStore deviceCertificateKeyStore, String deviceKeyStorePassword, ArrayList<String> defaultPrivileges);
    }

    public static void sendCSR(Context context, String commonName, String email) {
        new CSRSendTask(context, commonName, email).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void validateToken(Context context, String token, String certId, ProvisioningServerListener listener) {
        new TokenVerificationTask(context, token, certId, listener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static class CSRSendTask extends AsyncTask<Void, String, Void>
    {
        Context mContext;
        String  mCommonName;
        String  mEmail;

        CSRSendTask(Context context, String commonName, String email) {
            mContext    = context;
            mCommonName = commonName;
            mEmail      = email;
        }

        @Override
        protected Void doInBackground(Void... params) {
            byte [] csr = KeyManager.getCSR(mContext, mCommonName, mEmail);

            if (csr == null) return null;

            URL url;
            String response = "";

            try {
                String string = convertToPem(csr);

                Log.d(TAG, "CSR encoded: " + string);

                url = new URL("http://192.168.16.245:5000/csr");
                //url = new URL("https://rvi-test2.nginfotpdx.net:5000/csr");//"http://192.168.16.245:5000/csr");

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
            String cert_begin = "-----BEGIN CERTIFICATE REQUEST-----\n";
            String end_cert = "\n-----END CERTIFICATE REQUEST-----";

            String pemCertPre = new String(Base64.encode(derCert));
            return cert_begin + pemCertPre + end_cert;
        }
    }

    private class ProvisioningServerResponse
    {
        @SerializedName("signed_certificate")
        private String mDeviceCert;

        @SerializedName("server_certificate")
        private String mServerCert;

        @SerializedName("jwt")
        private ArrayList<String> mJwtPrivileges;

        /**
        * Default constructor
        */
        ProvisioningServerResponse() {

        }
    }

    private static class TokenVerificationTask extends AsyncTask<Void, String, ProvisioningServerResponse>
    {
        Context mContext;
        String  mToken;
        String  mCertId;
        ProvisioningServerListener mListener;

        TokenVerificationTask(Context context, String token, String certId, ProvisioningServerListener listener) {
            mContext  = context;
            mToken    = token;
            mCertId   = certId;
            mListener = listener;
        }

        @Override
        protected ProvisioningServerResponse doInBackground(Void... params) {
            Log.d(TAG, "Signing token...");

            String tokenJwt = KeyManager.getJwt(mContext, mToken, mCertId);

            if (tokenJwt == null) return null;

            URL url;
            String response = "";
            ProvisioningServerResponse serverResponse = null;

            try {

                url = new URL("http://192.168.16.245:5000/verification");
                //url = new URL("https://rvi-test2.nginfotpdx.net:5000/verification");//"http://192.168.16.245:5000/verification");


                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Log.d(TAG, "Sending verification: " + tokenJwt);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(tokenJwt);

                writer.flush();
                writer.close();
                os.close();

                Log.d(TAG, "Verification sent.");

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

                Gson gson = new Gson();
                serverResponse = gson.fromJson(response, ProvisioningServerResponse.class);

                Log.d(TAG, "Response from server: " + response);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return serverResponse;
        }

        @Override
        protected void onPostExecute(ProvisioningServerResponse serverResponse) {
            if (serverResponse != null) {

                try {
                    byte [] decodedServerCert  = Base64.decode(serverResponse.mServerCert.replaceAll("-----BEGIN CERTIFICATE-----", "").replaceAll("-----END CERTIFICATE-----", ""));
                    X509Certificate serverCert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(decodedServerCert));

                    byte [] decodedDeviceCert  = Base64.decode(serverResponse.mDeviceCert.replaceAll("-----BEGIN CERTIFICATE-----", "").replaceAll("-----END CERTIFICATE-----", ""));
                    X509Certificate deviceCert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(decodedDeviceCert));


                    KeyStore serverKeyStore = KeyStore.getInstance("BKS", "BC");
                    serverKeyStore.load(null, null);

                    serverKeyStore.setCertificateEntry("serverCert", serverCert);


//                    KeyStore deviceKeyStore = KeyStore.getInstance("PKCS12");
//                    deviceKeyStore.load(null, null);
//
//                    deviceKeyStore.setCertificateEntry("deviceCert", deviceCert);
//
//                    deviceKeyStore.load(new ByteArrayInputStream(decodedDeviceCert), "password".toCharArray());

                    //KeyStore serverKeyStore = KeyManager.addServerCertToKeyStore(serverCert);
                    KeyStore deviceKeyStore = KeyManager.addDeviceCertToKeyStore(deviceCert);

                    if (mListener != null) {
                        mListener.managerDidReceiveServerSignedStuff(serverKeyStore, deviceKeyStore, null, serverResponse.mJwtPrivileges);
                    }

                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        }
    }
}
