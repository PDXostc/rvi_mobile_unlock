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

    @SerializedName("vehicle_id")
    private String mVehicleId;

    @SerializedName("valid_from")
    private String mValidFrom = "1971-09-09T22:00:00.000Z";

    @SerializedName("valid_to")
    private String mValidTo = "1971-09-09T23:00:00.000Z";

    @SerializedName("user_type")
    private String mUserType = "guest";

    @SerializedName("vehicle_name")
    private String mVehicleName;

    @SerializedName("authorized_services")
    private VehicleServices mAuthorizedServices = new VehicleServices();

    Vehicle() {
    }

    public Vehicle(String vehicleId, String vehicleName) {

        this.setVehicleId(vehicleId);
        this.setVehicleName(vehicleName);
    }

    public Vehicle(JSONObject object) {
        try {

            JSONObject authservices = object.getJSONObject("authorizedServices");
            this.setLockUnlock(authservices.getBoolean("lock"));
            this.setEngineStart(authservices.getBoolean("engine"));

        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    public static ArrayList<UserCredentials> fromJson(JSONArray jsonobjects) {//,LinearLayout layout, keyRevokeActivity activity){
        ArrayList<UserCredentials> userCredentialses = new ArrayList<UserCredentials>();
        for(int i=0; i <jsonobjects.length();i++){
            try{
                userCredentialses.add(new UserCredentials(jsonobjects.getJSONObject(i)));
                //Log.i("DATA", jsonobjects.getJSONObject(i).toString());
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        return userCredentialses;
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

    public boolean isKeyValid() {
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

    public Boolean hasAnyAuthorizedServices() {
        return mAuthorizedServices.isLock()    ||
                mAuthorizedServices.isEngine() ||
                mAuthorizedServices.isHazard() ||
                mAuthorizedServices.isHorn()   ||
                mAuthorizedServices.isLights() ||
                mAuthorizedServices.isTrunk()  ||
                mAuthorizedServices.isWindows();
    }

    public String getVehicleId() {
        return mVehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.mVehicleId = vehicleId;
    }

    public String getValidFrom() {
        return prettyFormat(mValidFrom);
    }

    public void setValidFrom(String validFrom) {
        this.mValidFrom = validFrom;
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

    public String getValidTo() {
        return prettyFormat(mValidTo);
    }

    public void setValidTo(String validTo) {
        this.mValidTo = validTo;
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

    public boolean isLockUnlock() {
        return mAuthorizedServices.isLock();
    }

    public void setLockUnlock(boolean lockUnlock) {
        mAuthorizedServices.setLock(lockUnlock);
    }

    public boolean isEngineStart() {
        return mAuthorizedServices.isEngine();
    }

    public void setEngineStart(boolean engineStart) {
        mAuthorizedServices.setEngine(engineStart);
    }

    public String getUserType() {
        return mUserType;
    }

    public void setUserType(String userType) {
        mUserType = userType;
    }

    public String getVehicleName() {
        return mVehicleName;
    }

    public void setVehicleName(String vehicleName) {
        mVehicleName = vehicleName;
    }

    public VehicleServices getAuthorizedServices() {
        return mAuthorizedServices;
    }

    public void setAuthorizedServices(VehicleServices authorizedServices) {
        mAuthorizedServices = authorizedServices;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this, Vehicle.class);
    }
}
