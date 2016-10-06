package com.jaguarlandrover.rvi;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2016 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    Credentials.java
 * Project: UnlockDemo
 *
 * Created by Lilli Szafranski on 10/4/16.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.security.Key;
import java.util.ArrayList;

import io.jsonwebtoken.Jwts;

class Credential {
    private final static String TAG = "UnlockDemo:Credentials";

    @SerializedName("right_to_invoke")
    private ArrayList<String> mRightToInvoke = null;

    @SerializedName("right_to_receive")
    private ArrayList<String> mRightToReceive = null;

    @SerializedName("iss")
    private String mIssuer = null;

    @SerializedName("device_cert")
    private String mEncodedDeviceCertificate = null;

    @SerializedName("validity")
    private Validity mValidity = null;

    @SerializedName("id")
    private String mId = null;

    private String mJwt = null;

    public Credential() {
    }

    public Credential(String jwt) {
//        Gson gson = new Gson();
//        Credential credentials = gson.fromJson((String) Jwts.parser().parse(jwt).getBody(), Credential.class);
//
//        this.mRightToInvoke            = credentials.mRightToInvoke;
//        this.mRightToReceive           = credentials.mRightToReceive;
//        this.mIssuer                   = credentials.mIssuer;
//        this.mEncodedDeviceCertificate = credentials.mEncodedDeviceCertificate;
//        this.mValidity                 = credentials.mValidity;
//        this.mIssuer                   = credentials.mIssuer;
        this.mJwt                      = jwt;
    }

    Boolean isValid(Key serverKey) {
        try {
            Jwts.parser().setSigningKey(serverKey).parseClaimsJws(getJwt());
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public String getJwt() {
        return mJwt;
    }

    public void setJwt(String jwt) {
        mJwt = jwt;
    }
}

class Validity {
    private final static String PRETTY_DATE_TIME_FORMATTER = "MM/dd/yyyy h:mm a z";

    @SerializedName("start")
    private Integer mStart;

    @SerializedName("stop")
    private Integer mStop;
}
