package com.jaguarlandrover.auto.remote.vehicleentry;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2015 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    FobParams.java
 * Project: UnlockDemo
 *
 * Created by Lilli Szafranski on 11/16/15.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import com.google.gson.annotations.SerializedName;

public class FobParams
{
    private final static String TAG = "UnlockDemo:FobParams";

    @SerializedName("latitude")
    private double mLat = 0;

    @SerializedName("longitude")
    private double mLon = 0;

    @SerializedName("username")
    private String mUsername;

    @SerializedName("vehicleVIN")
    private String mVehicleVin;

    LocationManager  mLocationManager  = (LocationManager) UnlockApplication.getContext().getSystemService(Context.LOCATION_SERVICE);
    LocationListener mLocationListener = new LocationListener()
    {
        @Override
        public void onLocationChanged(Location location) {
            ourInstance.mLat = location.getLatitude();
            ourInstance.mLon = location.getLongitude();

            Log.d(TAG, "MY CURRENT LOCATION - Latitude = " + ourInstance.mLat + " Longitude = " + ourInstance.mLon);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private static FobParams ourInstance = new FobParams();

    private FobParams() {
    //    mLocationManager = (LocationManager) UnlockApplication.getContext().getSystemService(Context.LOCATION_SERVICE);
    }

    public static void startUpdatingLocation() {
        ourInstance.mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60, 1, ourInstance.mLocationListener);
    }

    public static void stopUpdatingLocation() {
        ourInstance.mLocationManager.removeUpdates(ourInstance.mLocationListener);
    }

    public static FobParams getSnapshot() {
        UserCredentials userCredentials = ServerNode.getUserCredentials();

        ourInstance.mUsername = userCredentials.getUserName();
        ourInstance.mVehicleVin = userCredentials.getVehicleVin();

        return ourInstance;
    }
}
