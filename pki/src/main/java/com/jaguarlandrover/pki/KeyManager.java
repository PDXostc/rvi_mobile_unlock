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

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import java.security.Key;

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

    private final static String KEYSTORE_ALIAS = "RVI_KEYPAIR_4096_6";
    private final static String DEFAULT_SIGNATURE_ALGORITHM = "SHA256withRSA";
    private final static String CN_PATTERN = "CN=%s, O=Genivi, OU=OrgUnit, EMAILADDRESS=%s";

    private final static Integer KEY_SIZE = 4096;

    static byte [] getCSR(Context context, String commonName, String email) {

        String   principal = String.format(CN_PATTERN, commonName, email);
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

        return null;
    }

    static String  getJwt(Context context, String token, String certId) {
        String json = "{ \"token\":\"" + token + "\", \"certId\":\"" + certId + "\"}";

        Log.d(TAG, "token json: " + json);

        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            Key key = keyStore.getKey(KEYSTORE_ALIAS, null);

            return Jwts.builder().setSubject(json).signWith(SignatureAlgorithm.RS256, key).compact();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
