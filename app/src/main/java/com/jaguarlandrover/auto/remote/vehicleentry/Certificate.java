package com.jaguarlandrover.auto.remote.vehicleentry;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2015 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    Certificate.java
 * Project: UnlockDemo
 *
 * Created by Lilli Szafranski on 10/28/15.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Certificate
{
    private final static String TAG = "UnlockDemo:Certificate";

    @SerializedName("keys")
    private ArrayList<Key> mKeys;

    @SerializedName("validity")
    private Validity mValidity;

    @SerializedName("sources")
    private ArrayList<String> mSources;

    @SerializedName("create_timestamp")
    private Integer mCreateTimestamp;

    @SerializedName("id")
    private String mId;

    @SerializedName("destinations")
    private ArrayList<String> mDestinations;

    public ArrayList<Key> getKeys() {
        return mKeys;
    }

    public void setKeys(ArrayList<Key> keys) {
        mKeys = keys;
    }

    public Validity getValidity() {
        return mValidity;
    }

    public void setValidity(Validity validity) {
        mValidity = validity;
    }

    public ArrayList<String> getSources() {
        return mSources;
    }

    public void setSources(ArrayList<String> sources) {
        mSources = sources;
    }

    public Integer getCreateTimestamp() {
        return mCreateTimestamp;
    }

    public void setCreateTimestamp(Integer createTimestamp) {
        mCreateTimestamp = createTimestamp;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public ArrayList<String> getDestinations() {
        return mDestinations;
    }

    public void setDestinations(ArrayList<String> destinations) {
        mDestinations = destinations;
    }

    Certificate() {}
}

class Key
{
    @SerializedName("use")
    private String mUse;// = "sig";

    @SerializedName("e")
    private String mE;//   = "AQAB";

    @SerializedName("kty")
    private String mKty;// = "rsa";

    @SerializedName("alg")
    private String mAlg;// = "RS256";

    @SerializedName("n")
    private String mN;

    @SerializedName("kid")
    private String mKid;

    public String getUse() {
        return mUse;
    }

    public void setUse(String use) {
        mUse = use;
    }

    public String getE() {
        return mE;
    }

    public void setE(String e) {
        mE = e;
    }

    public String getKty() {
        return mKty;
    }

    public void setKty(String kty) {
        mKty = kty;
    }

    public String getAlg() {
        return mAlg;
    }

    public void setAlg(String alg) {
        mAlg = alg;
    }

    public String getN() {
        return mN;
    }

    public void setN(String n) {
        mN = n;
    }

    public String getKid() {
        return mKid;
    }

    public void setKid(String kid) {
        mKid = kid;
    }

    Key() {}
}

class Validity
{
    @SerializedName("start")
    private Integer mStart;

    @SerializedName("stop")
    private Integer mStop;

    public Integer getStart() {
        return mStart;
    }

    public void setStart(Integer start) {
        mStart = start;
    }

    public Integer getStop() {
        return mStop;
    }

    public void setStop(Integer stop) {
        mStop = stop;
    }

    Validity() {}
}
