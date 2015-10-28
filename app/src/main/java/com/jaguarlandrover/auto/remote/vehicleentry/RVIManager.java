package com.jaguarlandrover.auto.remote.vehicleentry;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2015 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    RVIManager.java
 * Project: UnlockDemo
 *
 * Created by Lilli Szafranski on 10/28/15.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import com.jaguarlandrover.rvi.RVINode;

public class RVIManager
{
    private final static String TAG = "UnlockDemo:RVIManager";

    private static RVIManager ourInstance = new RVIManager();

    public static RVIManager getInstance() {
        return ourInstance;
    }

    private static RVINode serverNode  = new RVINode(null);
    private static RVINode vehicleNode = new RVINode(null);

    private RVIManager() {
    }
}
