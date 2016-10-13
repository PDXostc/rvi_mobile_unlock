package com.jaguarlandrover.pki;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2016 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    ManagerHelper.java
 * Project: UnlockDemo
 *
 * Created by Lilli Szafranski on 9/27/16.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.spongycastle.util.encoders.Base64;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;

class ManagerHelper
{
    private final static String TAG = "PKI:ManagerHelper______";
//
//    static void generateKeyPairAndCertificateSigningRequest(Context context, PKIManager.CertificateSigningRequestGeneratorListener listener, Date startDate, Date endDate, String principalFormatterPattern, Object... principalFormatterArgs) {
//        new CertificateSigningRequestGeneratorTask(listener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, context, startDate, endDate, principalFormatterPattern, principalFormatterArgs);
//    }
//
//    private static class CertificateSigningRequestGeneratorTask extends AsyncTask<Object, String, Throwable>
//    {
//        PKIManager.CertificateSigningRequestGeneratorListener mListener = null;
//
//        CertificateSigningRequestGeneratorTask(PKIManager.CertificateSigningRequestGeneratorListener listener) {
//            mListener = listener;
//        }
//
//        @Override
//        protected Throwable doInBackground(Object... params) {
//            Context  context                   = (Context)  params[0];
//            Date     startDate                 = (Date)     params[1];
//            Date     endDate                   = (Date)     params[2];
//            String   principalFormatterPattern = (String)   params[3];
//            Object[] principalFormatterArgs    = (Object[]) params[4];//Arrays.copyOfRange(params, 4, params.length - 4); // TODO: Test all possibilities thoroughly!
//
//            // TODO: Validate parameters!
//
//            try {
//                String certificateSigningRequest =
//                        KeyStoreManager.generateCertificateSigningRequest(context, startDate, endDate, principalFormatterPattern, principalFormatterArgs);
//
//                publishProgress(certificateSigningRequest);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//
//                return e;
//            }
//
//            return null;
//        }
//
//        @Override
//        protected void onProgressUpdate(String... params) {
//            super.onProgressUpdate(params);
//
//            if (mListener != null) mListener.generateCertificateSigningRequestSucceeded(params[0]);
//        }
//
//        @Override
//        protected void onPostExecute(Throwable result) {
//            super.onPostExecute(result);
//
//            if (result != null) mListener.generateCertificateSigningRequestFailed(result);
//        }
//    }

//    static void sendCertificateSigningRequest(Context context, PKIManager.ProvisioningServerListener listener, String baseUrl, String requestUrl, String certificateSigningRequest, Boolean extraValidationExpected) {
//        Log.d(TAG, "CSR encoded: " + certificateSigningRequest);
//
//        new ProvisioningServerTask(context,
//                                   extraValidationExpected ?
//                                            ProvisioningServerRequestToken.SEND_CSR_EXTRA_VALIDATION :
//                                            ProvisioningServerRequestToken.SEND_CSR_NO_EXTRA_VALIDATION,
//                                   listener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, baseUrl, requestUrl, certificateSigningRequest);
//
//    }
//
//    static void sendTokenVerificationRequest(Context context, PKIManager.ProvisioningServerListener listener, String baseUrl, String requestUrl, String tokenVerificationString) {
//        Log.d(TAG, "Sending verification: " + tokenVerificationString);
//
//        String signedTokenVerification = KeyStoreManager.createJwt(context, tokenVerificationString);
//
//        new ProvisioningServerTask(context, ProvisioningServerRequestToken.VALIDATE_TOKEN, listener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, baseUrl, requestUrl, signedTokenVerification);
//
//    }
//
//    private enum ProvisioningServerRequestToken
//    {
//        UNKNOWN,
//        SEND_CSR_EXTRA_VALIDATION,
//        SEND_CSR_NO_EXTRA_VALIDATION,
//        VALIDATE_TOKEN,
//        OTHER
//    }
//
//    private static class ProvisioningServerTask extends AsyncTask<String, String, Throwable>
//    {
//        Context mContext = null;
//        ProvisioningServerRequestToken mRequestToken = ProvisioningServerRequestToken.UNKNOWN;
//        PKIManager.ProvisioningServerListener mListener = null;
//
//        ProvisioningServerTask(Context context, ProvisioningServerRequestToken token, PKIManager.ProvisioningServerListener listener) {
//            mContext      = context;
//            mRequestToken = token;
//            mListener     = listener;
//        }
//
//        @Override
//        protected Throwable doInBackground(String... params) {
//            String baseUrl     = params[0];
//            String requestUrl  = params[1];
//            String requestData = params[2];
//
//            // TODO: Validate parameters!
//
//            try {
//                String serverResponse = BackendServerInterface.sendStringToProvisioningServer(baseUrl, requestUrl, requestData);
//
//                publishProgress(serverResponse);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//
//                return e;
//            }
//
//            return null;
//        }
//
//        @Override
//        protected void onProgressUpdate(String... params) {
//            super.onProgressUpdate(params);
//
//            String serverResponse = params[0];
//
//            switch (mRequestToken) {
//
//                case UNKNOWN:
//                    break;
//                case SEND_CSR_EXTRA_VALIDATION:
//                    if (mListener != null) mListener.certificateSigningRequestSuccessfullyReceived();
//                    break;
//
//                case SEND_CSR_NO_EXTRA_VALIDATION:
//                case VALIDATE_TOKEN:
//                    // TODO: Handle if the response isn't what's expected
//
//                    if (mListener != null) mListener.certificateSigningRequestSuccessfullyReceived();
//
//                    processServerCertificateResponse(mContext, serverResponse, mListener);
//
//                    break;
//                case OTHER:
//                    break;
//            }
//
//
//            //if (mListener != null) mListener.generateCertificateSigningRequestSucceeded(params[0]);
//        }
//
//        @Override
//        protected void onPostExecute(Throwable result) {
//            super.onPostExecute(result);
//
//            //if (result != null) mListener.generateCertificateSigningRequestFailed(result);
//        }
//    }
//
//    private class ProvisioningServerCertificateResponse
//    {
//        @SerializedName("signed_certificate")
//        private String mDeviceCert;
//
//        @SerializedName("server_certificate")
//        private String mServerCert;
//
//        @SerializedName("jwt")
//        private ArrayList<String> mJwtPrivileges;
//
//        /**
//        * Default constructor
//        */
//        ProvisioningServerCertificateResponse() {
//
//        }
//    }
//
//    private static void processServerCertificateResponse(Context context, String serverCertificateResponse, PKIManager.ProvisioningServerListener listener) {
//        try {
//            Gson gson = new Gson();
//            ProvisioningServerCertificateResponse serverResponse = gson.fromJson(serverCertificateResponse, ProvisioningServerCertificateResponse.class);
//
//            byte [] decodedServerCert  = Base64.decode(serverResponse.mServerCert.replaceAll("-----BEGIN CERTIFICATE-----", "").replaceAll("-----END CERTIFICATE-----", ""));
//            X509Certificate serverCert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(decodedServerCert));
//
//            byte [] decodedDeviceCert  = Base64.decode(serverResponse.mDeviceCert.replaceAll("-----BEGIN CERTIFICATE-----", "").replaceAll("-----END CERTIFICATE-----", ""));
//            X509Certificate deviceCert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(decodedDeviceCert));
//
//            KeyStore serverKeyStore = KeyStoreManager.addServerCertToKeyStore(context, serverCert);
//            KeyStore deviceKeyStore = KeyStoreManager.addDeviceCertToKeyStore(context, deviceCert);
//
//            if (listener != null) {
//                listener.managerDidReceiveServerSignedStuff(serverKeyStore, deviceKeyStore, null, serverResponse.mJwtPrivileges);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//
//        }
//    }
}
