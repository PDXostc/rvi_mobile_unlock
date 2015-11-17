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

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import com.google.gson.annotations.SerializedName;

public class FobParamsManager
{
    private final static String TAG = "UnlockDemo:FobParams";

    private static double lat = 0;
    private static double lon = 0;

    LocationManager  mLocationManager  = (LocationManager) UnlockApplication.getContext().getSystemService(Context.LOCATION_SERVICE);
    private LocationListener mLocationListener = new LocationListener()
    {
        @Override
        public void onLocationChanged(Location location) {
            lat = location.getLatitude();
            lon = location.getLongitude();

            Log.d(TAG, "MY CURRENT LOCATION - Latitude = " + lat + " Longitude = " + lon);
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

    private static FobParamsManager ourInstance = new FobParamsManager();

    private FobParamsManager() {
        //    mLocationManager = (LocationManager) UnlockApplication.getContext().getSystemService(Context.LOCATION_SERVICE);
    }

    public static void startUpdatingLocation() {
        ourInstance.mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60, 1, ourInstance.mLocationListener);
    }

    public static void stopUpdatingLocation() {
        ourInstance.mLocationManager.removeUpdates(ourInstance.mLocationListener);
    }

    static class FobParams
    {
        @SerializedName("latitude")
        private double mLat = 0;

        @SerializedName("longitude")
        private double mLon = 0;

        @SerializedName("username")
        private String mUsername;

        @SerializedName("vehicleVIN")
        private String mVehicleVin;

        public FobParams() {
            UserCredentials userCredentials = ServerNode.getUserCredentials();
            mLat = lat;
            mLon = lon;
            mUsername = userCredentials.getUserName();
            mVehicleVin = userCredentials.getVehicleVin();
        }
    }

//    public static FobParamsManager getSnapshot() {
//        Log.d(TAG, "getSnapshot()");
//        UserCredentials userCredentials = ServerNode.getUserCredentials();
//        FobParamsManager copy = new FobParamsManager();
//
//        copy.mLat = ourInstance.mLat;
//        copy.mLon = ourInstance.mLon;
//        copy.mUsername = userCredentials.getUserName();
//        copy.mVehicleVin = userCredentials.getVehicleVin();
//
//        return copy;
//    }
}
