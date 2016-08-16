package com.jaguarlandrover.pki;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2016 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    KeyManager.java
 * Project: UnlockDemo
 *
 * Created by Lilli Szafranski on 8/8/16.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import android.content.Context;
import android.security.KeyPairGeneratorSpec;
import android.util.Log;

import org.spongycastle.asn1.ASN1ObjectIdentifier;

import org.spongycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x509.AlgorithmIdentifier;
import org.spongycastle.asn1.x509.BasicConstraints;
import org.spongycastle.asn1.x509.Certificate;
import org.spongycastle.asn1.x509.Extension;
import org.spongycastle.asn1.x509.ExtensionsGenerator;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.pkcs.PKCS10CertificationRequest;
import org.spongycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.spongycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.x500.X500Principal;

/* Code from here: http://stackoverflow.com/a/37898553 */
public class KeyManager {
    private final static String TAG = "UnlockDemo:KeyManager";

    private final static String KEYSTORE_ALIAS = "RVI_KEYPAIR_4096_2";
    private final static String DEFAULT_SIGNATURE_ALGORITHM = "SHA256withRSA";
    private final static String CN_PATTERN = "CN=%s, O=Aralink, OU=OrgUnit";

    private final static Integer KEY_SIZE = 4096;

    static byte [] getCSR(Context context, String commonName) {

        String   principal = String.format(CN_PATTERN, commonName);
        KeyStore keyStore  = null;
        KeyPair  keyPair   = null;

        java.security.cert.Certificate cert = null;

        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                end.add(Calendar.YEAR, 1);

                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                        .setAlias(KEYSTORE_ALIAS)
                        .setKeySize(KEY_SIZE)
                        .setSubject(new X500Principal(principal))
                        .setSerialNumber(BigInteger.ONE)
                        .setStartDate(start.getTime())
                        .setEndDate(end.getTime())
                        .build();
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
                generator.initialize(spec);

                keyPair = generator.generateKeyPair();
                cert = keyStore.getCertificate(KEYSTORE_ALIAS);

            } else {
                Key key = keyStore.getKey(KEYSTORE_ALIAS, null);

                cert = keyStore.getCertificate(KEYSTORE_ALIAS);
                PublicKey publicKey = cert.getPublicKey();

                keyPair = new KeyPair(publicKey, (PrivateKey) key);
            }

            return cert.getEncoded();


        } catch (Exception e) {
            e.printStackTrace();
        }




//        try {
////            keyGen = KeyPairGenerator.getInstance("RSA");
////            keyGen.initialize(KEY_SIZE, new SecureRandom());
//
//            Log.d(TAG, "Generating key pair...");
//
////            KeyPair keyPair = keyGen.generateKeyPair();
//
//            KeyPair keyPair = getKeyPair(context, principal);
//
//            Log.d(TAG, "Key pair generated: " + keyPair.getPrivate().toString() + "/" + keyPair.getPublic().toString());
//
//            Log.d(TAG, "Generating CSR...");
//
//            PKCS10CertificationRequest csr = generateCSR(keyPair, principal);
//
//            Log.d(TAG, "CSR generated: " + csr.toString());
//
//            //Generate CSR in PKCS#10 format encoded in DER
//
//            Log.d(TAG, "Encoding CSR...");
//
//            return csr.getEncoded();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        return null;
    }
//
//        //Generate KeyPair
//        KeyPairGenerator keyGen = null;
//        String principal = String.format(CN_PATTERN, commonName);
//
//        try {
////            keyGen = KeyPairGenerator.getInstance("RSA");
////            keyGen.initialize(KEY_SIZE, new SecureRandom());
//
//            Log.d(TAG, "Generating key pair...");
//
////            KeyPair keyPair = keyGen.generateKeyPair();
//
//            KeyPair keyPair = getKeyPair(context, principal);
//
//            Log.d(TAG, "Key pair generated: " + keyPair.getPrivate().toString() + "/" + keyPair.getPublic().toString());
//
//            Log.d(TAG, "Generating CSR...");
//
//            PKCS10CertificationRequest csr = generateCSR(keyPair, principal);
//
//            Log.d(TAG, "CSR generated: " + csr.toString());
//
//            //Generate CSR in PKCS#10 format encoded in DER
//
//            Log.d(TAG, "Encoding CSR...");
//
//            return csr.getEncoded();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }

    private static KeyPair getKeyPair(Context context, String principal) {
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                end.add(Calendar.YEAR, 1);

                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                        .setAlias(KEYSTORE_ALIAS)
                        .setKeySize(KEY_SIZE)
                        .setSubject(new X500Principal(principal))
                        .setSerialNumber(BigInteger.ONE)
                        .setStartDate(start.getTime())
                        .setEndDate(end.getTime())
                        .build();
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
                generator.initialize(spec);

                return generator.generateKeyPair();
            } else {
                final Key key = (PrivateKey) keyStore.getKey(KEYSTORE_ALIAS, null);

                final java.security.cert.Certificate cert = keyStore.getCertificate(KEYSTORE_ALIAS);
                final PublicKey publicKey = cert.getPublicKey();

                return new KeyPair(publicKey, (PrivateKey) key);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static class JCESigner implements ContentSigner {

        private static Map<String, AlgorithmIdentifier> ALGOS = new HashMap<String, AlgorithmIdentifier>();

        static {
            ALGOS.put("SHA256withRSA".toLowerCase(), new AlgorithmIdentifier(new ASN1ObjectIdentifier("1.2.840.113549.1.1.11")));
            ALGOS.put("SHA1withRSA".toLowerCase(), new AlgorithmIdentifier(new ASN1ObjectIdentifier("1.2.840.113549.1.1.5")));
        }

        private String mAlgo;
        private Signature signature;
        private ByteArrayOutputStream outputStream;

        public JCESigner(PrivateKey privateKey, String sigAlgo) {
            //Utils.throwIfNull(privateKey, sigAlgo);
            mAlgo = sigAlgo.toLowerCase();
            try {
                this.outputStream = new ByteArrayOutputStream();
                this.signature = Signature.getInstance(sigAlgo);
                this.signature.initSign(privateKey);
            } catch (GeneralSecurityException gse) {
                throw new IllegalArgumentException(gse.getMessage());
            }
        }

        @Override
        public AlgorithmIdentifier getAlgorithmIdentifier() {
            AlgorithmIdentifier id = ALGOS.get(mAlgo);
            if (id == null) {
                throw new IllegalArgumentException("Does not support algo: " + mAlgo);
            }
            return id;
        }

        @Override
        public OutputStream getOutputStream() {
            return outputStream;
        }

        @Override
        public byte[] getSignature() {
            try {
                signature.update(outputStream.toByteArray());
                return signature.sign();
            } catch (GeneralSecurityException gse) {
                gse.printStackTrace();
                return null;
            }
        }
    }

    //Create the certificate signing request (CSR) from private and public keys
    private static PKCS10CertificationRequest generateCSR(KeyPair keyPair, String principal) throws IOException, OperatorCreationException {
        ContentSigner signer = new JCESigner(keyPair.getPrivate(), DEFAULT_SIGNATURE_ALGORITHM);

        PKCS10CertificationRequestBuilder csrBuilder = new JcaPKCS10CertificationRequestBuilder(new X500Name(principal), keyPair.getPublic());
        ExtensionsGenerator extensionsGenerator = new ExtensionsGenerator();

        extensionsGenerator.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
        csrBuilder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest, extensionsGenerator.generate());

        PKCS10CertificationRequest csr = csrBuilder.build(signer);

        return csr;
    }
}
