package com.jaguarlandrover.rvi;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2016 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    RVICredentialManager.java
 * Project: UnlockDemo
 *
 * Created by Lilli Szafranski on 10/4/16.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

public class RVICredentialManager {
    private final static String TAG = "UnlockDemo:RVICredentialManager";

    private static RVICredentialManager ourInstance = new RVICredentialManager();

    private static RVICredentialManager getInstance() {
        return ourInstance;
    }

    private RVICredentialManager() {
    }



}
