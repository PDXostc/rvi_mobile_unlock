package com.jaguarlandrover.auto.remote.vehicleentry;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2016 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    Vehicle.java
 * Project: UnlockDemo
 *
 * Created by Lilli Szafranski on 10/11/16.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class Vehicle {
    private final static String TAG = "UnlockDemo/Vehicle_____";

    private final static String SERVER_DATE_TIME_FORMATTER = "yyyy-MM-dd'T'HH:mm:ss";
    private final static String PRETTY_DATE_TIME_FORMATTER = "MM/dd/yyyy h:mm a z";

    private final static String DEFAULT_VALID_FROM = "1971-09-09T22:00:00.000Z";
    private final static String DEFAULT_VALID_TO   = "1971-09-09T23:00:00.000Z";

    @SerializedName("vehicle_id")
    private String mVehicleId = "";

    @SerializedName("valid_from")
    private String mValidFrom = DEFAULT_VALID_FROM;

    @SerializedName("valid_to")
    private String mValidTo = DEFAULT_VALID_TO;

    @SerializedName("user_type")
    private String mUserType = "guest";

    @SerializedName("display_name")
    private String mDisplayName = "";

    @SerializedName("vehicle_url")
    private String mVehicleUrl = "";

    @SerializedName("vehicle_port")
    private Integer mVehiclePort = 0;

    @SerializedName("authorized_services")
    private VehicleServices mAuthorizedServices = new VehicleServices();

    Vehicle() {
    }

    Vehicle(String vehicleId) {
        setVehicleId(vehicleId);
    }

    Vehicle(String vehicleId, String validFrom, String validTo) {
        setVehicleId(vehicleId);

        setValidFrom(validFrom);
        setValidTo(validTo);
    }

    private String prettyFormat(String serverString) {
        try {
            SimpleDateFormat serverFormat = new SimpleDateFormat(SERVER_DATE_TIME_FORMATTER);
            SimpleDateFormat prettyFormat = new SimpleDateFormat(PRETTY_DATE_TIME_FORMATTER);
            serverFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date newDate = serverFormat.parse(serverString.substring(0, 23));
            return prettyFormat.format(newDate);

        } catch (Exception e) {
            Log.d(TAG, "Error parsing date/time.");
            e.printStackTrace();

            return serverString;
        }
    }

    boolean isKeyValid() {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(SERVER_DATE_TIME_FORMATTER);
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date validTo   = formatter.parse(mValidTo.substring(0, 23));
            Date validFrom = formatter.parse(mValidFrom.substring(0, 23));
            Date dateNow   = new Date();

            return validTo.compareTo(dateNow) > 0 && validFrom.compareTo(dateNow) < 0;

        } catch (Exception e) {
            Log.d(TAG, "Error parsing date/time.");
            e.printStackTrace();

            return false;
        }
    }

    Boolean hasAnyAuthorizedServices() {
        return mAuthorizedServices.isLock()    ||
                mAuthorizedServices.isEngine() ||
                mAuthorizedServices.isHazard() ||
                mAuthorizedServices.isHorn()   ||
                mAuthorizedServices.isLights() ||
                mAuthorizedServices.isTrunk()  ||
                mAuthorizedServices.isWindows();
    }

    private void setVehicleId(String vehicleId) {
        mVehicleId = vehicleId == null ? "" : vehicleId;
    }

    String getVehicleId() {
        return mVehicleId;
    }

    String getValidFrom() {
        return prettyFormat(mValidFrom);
    }

    void setValidFrom(String validFrom) {
        this.mValidFrom = validFrom == null ? DEFAULT_VALID_FROM : validFrom;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(SERVER_DATE_TIME_FORMATTER);
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

            formatter.parse(mValidFrom.substring(0, 23));
        } catch (Exception e) {
            Log.d(TAG, "Error: Incorrect format for 'validFrom'. May cause issues when syncing with server.");
            e.printStackTrace();
        }
    }

    // TODO: Doesn't seem to be working correctly
    public void setValidFromAsDate(Date validFrom) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(SERVER_DATE_TIME_FORMATTER);
            formatter.setTimeZone(TimeZone.getTimeZone("GMT"));

            mValidFrom = formatter.format(validFrom) + ".000Z";

            Log.d(TAG, mValidFrom);
        } catch (Exception e) {
            Log.d(TAG, "Error parsing date/time");
            e.printStackTrace();
        }
    }

    String getValidTo() {
        return prettyFormat(mValidTo);
    }

    void setValidTo(String validTo) {
        this.mValidTo = validTo == null ? DEFAULT_VALID_TO : validTo;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(SERVER_DATE_TIME_FORMATTER);
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

            formatter.parse(mValidTo.substring(0, 23));

        } catch (Exception e) {
            Log.d(TAG, "Error: Incorrect format for 'validTo'. May cause issues when syncing with server.");
            e.printStackTrace();
        }
    }

    // TODO: Doesn't seem to be working correctly
    public void setValidToAsDate(Date validFrom) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(SERVER_DATE_TIME_FORMATTER);
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

            mValidFrom = formatter.format(validFrom) + ".000Z";
        } catch (Exception e) {
            Log.d(TAG, "Error parsing date/time");
            e.printStackTrace();
        }
    }

    boolean isLockUnlock() {
        return mAuthorizedServices.isLock();
    }

    boolean isEngineStart() {
        return mAuthorizedServices.isEngine();
    }

    String getUserType() {
        return mUserType;
    }

    String getDisplayName() {
        if (mDisplayName == null) return mVehicleId;

        return mDisplayName;
    }

    VehicleServices getAuthorizedServices() {
        return mAuthorizedServices;
    }

    public String getVehicleUrl() {
        return mVehicleUrl;
    }

    public Integer getVehiclePort() {
        return mVehiclePort;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this, Vehicle.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicle)) return false;

        Vehicle vehicle = (Vehicle) o;

        if (getVehicleId() != null ? !getVehicleId().equals(vehicle.getVehicleId()) : vehicle.getVehicleId() != null) return false;
        if (getValidFrom() != null ? !getValidFrom().equals(vehicle.getValidFrom()) : vehicle.getValidFrom() != null) return false;
        if (getValidTo() != null ? !getValidTo().equals(vehicle.getValidTo()) : vehicle.getValidTo() != null) return false;
        if (getUserType() != null ? !getUserType().equals(vehicle.getUserType()) : vehicle.getUserType() != null) return false;
        if (getDisplayName() != null ? !getDisplayName().equals(vehicle.getDisplayName()) : vehicle.getDisplayName() != null) return false;
        if (getVehicleUrl() != null ? !getVehicleUrl().equals(vehicle.getVehicleUrl()) : vehicle.getVehicleUrl() != null) return false;
        if (getVehiclePort() != null ? !getVehiclePort().equals(vehicle.getVehiclePort()) : vehicle.getVehiclePort() != null) return false;
        return getAuthorizedServices() != null ? getAuthorizedServices().equals(vehicle.getAuthorizedServices()) : vehicle.getAuthorizedServices() == null;

    }

    @Override
    public int hashCode() {
        int result = getVehicleId() != null ? getVehicleId().hashCode() : 0;
        result = 31 * result + (getValidFrom() != null ? getValidFrom().hashCode() : 0);
        result = 31 * result + (getValidTo() != null ? getValidTo().hashCode() : 0);
        result = 31 * result + (getUserType() != null ? getUserType().hashCode() : 0);
        result = 31 * result + (getDisplayName() != null ? getDisplayName().hashCode() : 0);
        result = 31 * result + (getVehicleUrl() != null ? getVehicleUrl().hashCode() : 0);
        result = 31 * result + (getVehiclePort() != null ? getVehiclePort().hashCode() : 0);
        result = 31 * result + (getAuthorizedServices() != null ? getAuthorizedServices().hashCode() : 0);
        return result;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof Vehicle)) return false;
//
//        Vehicle vehicle = (Vehicle) o;
//
//        if (getVehicleId() != null ? !getVehicleId().equals(vehicle.getVehicleId()) : vehicle.getVehicleId() != null) return false;
//        if (getValidFrom() != null ? !getValidFrom().equals(vehicle.getValidFrom()) : vehicle.getValidFrom() != null) return false;
//        if (getValidTo() != null ? !getValidTo().equals(vehicle.getValidTo()) : vehicle.getValidTo() != null) return false;
//        if (getUserType() != null ? !getUserType().equals(vehicle.getUserType()) : vehicle.getUserType() != null) return false;
//        if (getDisplayName() != null ? !getDisplayName().equals(vehicle.getDisplayName()) : vehicle.getDisplayName() != null) return false;
//        return getAuthorizedServices().equals(vehicle.getAuthorizedServices());
//
//    }
//
//    @Override
//    public int hashCode() {
//        int result = getVehicleId() != null ? getVehicleId().hashCode() : 0;
//        result = 31 * result + (getValidFrom() != null ? getValidFrom().hashCode() : 0);
//        result = 31 * result + (getValidTo() != null ? getValidTo().hashCode() : 0);
//        result = 31 * result + (getUserType() != null ? getUserType().hashCode() : 0);
//        result = 31 * result + (getDisplayName() != null ? getDisplayName().hashCode() : 0);
//        result = 31 * result + getAuthorizedServices().hashCode();
//        return result;
//    }
}
