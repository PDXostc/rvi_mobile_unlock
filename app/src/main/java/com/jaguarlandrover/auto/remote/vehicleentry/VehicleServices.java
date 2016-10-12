package com.jaguarlandrover.auto.remote.vehicleentry;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2016 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    VehicleServices.java
 * Project: UnlockDemo
 *
 * Created by Lilli Szafranski on 10/11/16.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import com.google.gson.annotations.SerializedName;

public class VehicleServices {
    private final static String TAG = "UnlockDemo/VehicleSrvcs";

    @SerializedName("engine")
    private boolean mEngine = false;

    @SerializedName("windows")
    private boolean mWindows = false;

    @SerializedName("lock")
    private boolean mLock = false;

    @SerializedName("hazard")
    private boolean mHazard = false;

    @SerializedName("horn")
    private boolean mHorn = false;

    @SerializedName("lights")
    private boolean mLights = false;

    @SerializedName("trunk")
    private boolean mTrunk = false;

    VehicleServices() {}

    public boolean isEngine() {
        return mEngine;
    }

    public void setEngine(boolean engine) {
        mEngine = engine;
    }

    public boolean isWindows() {
        return mWindows;
    }

    public void setWindows(boolean windows) {
        mWindows = windows;
    }

    public boolean isLock() {
        return mLock;
    }

    public void setLock(boolean lock) {
        mLock = lock;
    }

    public boolean isHazard() {
        return mHazard;
    }

    public void setHazard(boolean hazard) {
        mHazard = hazard;
    }

    public boolean isHorn() {
        return mHorn;
    }

    public void setHorn(boolean horn) {
        mHorn = horn;
    }

    public boolean isLights() {
        return mLights;
    }

    public void setLights(boolean lights) {
        mLights = lights;
    }

    public boolean isTrunk() {
        return mTrunk;
    }

    public void setTrunk(boolean trunk) {
        mTrunk = trunk;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VehicleServices)) return false;

        VehicleServices that = (VehicleServices) o;

        if (isEngine() != that.isEngine()) return false;
        if (isWindows() != that.isWindows()) return false;
        if (isLock() != that.isLock()) return false;
        if (isHazard() != that.isHazard()) return false;
        if (isHorn() != that.isHorn()) return false;
        if (isLights() != that.isLights()) return false;
        return isTrunk() == that.isTrunk();

    }

    @Override
    public int hashCode() {
        int result = (isEngine() ? 1 : 0);
        result = 31 * result + (isWindows() ? 1 : 0);
        result = 31 * result + (isLock() ? 1 : 0);
        result = 31 * result + (isHazard() ? 1 : 0);
        result = 31 * result + (isHorn() ? 1 : 0);
        result = 31 * result + (isLights() ? 1 : 0);
        result = 31 * result + (isTrunk() ? 1 : 0);
        return result;
    }
}
