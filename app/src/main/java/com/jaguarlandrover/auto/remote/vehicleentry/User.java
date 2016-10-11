package com.jaguarlandrover.auto.remote.vehicleentry;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2016 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    User.java
 * Project: UnlockDemo
 *
 * Created by Lilli Szafranski on 10/11/16.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.openssl.MiscPEMGenerator;

import java.util.ArrayList;

public class User {
    private final static String TAG = "UnlockDemo/User        ";

    @SerializedName("username")
    private String mUserName;

    @SerializedName("first_name")
    private String mFirstName;

    @SerializedName("last_name")
    private String mLastName;

    @SerializedName("guests")
    private ArrayList<User> mGuests = new ArrayList<>();

    @SerializedName("vehicles")
    private ArrayList<Vehicle> mVehicles = new ArrayList<>();

    User() {
    }

    public User(String userName, String vehicle, String validFrom, String validTo, boolean lockUnlock, boolean engineStart) {
        this.setUserName(userName);
    }

    public User(JSONObject object) {
        try {
            this.setUserName(object.getString("username"));
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        this.mUserName = userName;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String firstName) {
        mFirstName = firstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String lastName) {
        mLastName = lastName;
    }

    public ArrayList<User> getGuests() {
        if (mGuests == null) return new ArrayList<>();
        return mGuests;
    }

    public void setGuests(ArrayList<User> guests) {
        mGuests = guests;
    }

    public ArrayList<Vehicle> getVehicles() {
        if (mVehicles == null) return new ArrayList<>();
        return mVehicles;
    }

    public void setVehicles(ArrayList<Vehicle> vehicles) {
        mVehicles = vehicles;
    }

    private final static String SELECTED_VEHICLE_INDEX_KEY = "SELECTED_VEHICLE_INDEX_KEY";

    Integer getSelectedVehicleIndex() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(UnlockApplication.getContext());
        Integer selectedIndex = preferences.getInt(SELECTED_VEHICLE_INDEX_KEY, -1);

        if (mVehicles == null || selectedIndex < -1 || selectedIndex >= mVehicles.size()) {
            setSelectedVehicleIndex(-1);
            return -1;
        }

        return selectedIndex;
    }

    void setSelectedVehicleIndex(Integer selectedVehicleIndex) {
        if (mVehicles == null || selectedVehicleIndex < -1 || selectedVehicleIndex >= mVehicles.size())
            selectedVehicleIndex = -1;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(UnlockApplication.getContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(SELECTED_VEHICLE_INDEX_KEY, selectedVehicleIndex);
        editor.apply();
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this, User.class);
    }
}
