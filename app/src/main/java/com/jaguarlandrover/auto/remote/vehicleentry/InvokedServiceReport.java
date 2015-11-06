package com.jaguarlandrover.auto.remote.vehicleentry;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2015 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    InvokedServiceReport.java
 * Project: UnlockDemo
 *
 * Created by Lilli Szafranski on 11/4/15.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import com.google.gson.annotations.SerializedName;

public class InvokedServiceReport
{
    private final static String TAG = "UnlockDemo:InvokedServiceReport";

    @SerializedName("username")
    private String mUserName;

    @SerializedName("service")
    private String mServiceIdentifier;

    public InvokedServiceReport() {

    }

    public String getUserName() {
        return mUserName;
    }

    public String getServiceIdentifier() {
        return mServiceIdentifier;
    }
}
