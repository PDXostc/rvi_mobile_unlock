package com.jaguarlandrover.auto.remote.vehicleentry;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2015 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    UnlockApplication.java
 * Project: UnlockDemo
 *
 * Created by Lilli Szafranski on 10/28/15.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import android.app.Application;
import android.content.Context;

public class UnlockApplication extends Application
{
    private final static String TAG = "UnlockDemo:UnlockApplication";

    private static Application instance;

        @Override
        public void onCreate() {
            super.onCreate();
            instance = this;
        }

        public static Context getContext() {
            return instance.getApplicationContext();
        }
}