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

import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.openssl.MiscPEMGenerator;

import java.util.ArrayList;

public class User {
    private final static String TAG = "UnlockDemo/User________";

    @SerializedName("username")
    private String mUserName = "";

    @SerializedName("first_name")
    private String mFirstName = "";

    @SerializedName("last_name")
    private String mLastName = "";

    @SerializedName("guests")
    private ArrayList<User> mGuests = new ArrayList<>();

    @SerializedName("vehicles")
    private ArrayList<Vehicle> mVehicles = new ArrayList<>();

    User() {
    }

    public User(String userName) {
        mUserName = userName;
    }

    String getUserName() {
        return mUserName;
    }

    String getFirstName() {
        return mFirstName;
    }

    String getLastName() {
        return mLastName;
    }

    ArrayList<User> getGuests() {
        if (mGuests == null) return new ArrayList<>();
        return mGuests;
    }

    ArrayList<Vehicle> getVehicles() {
        if (mVehicles == null) return new ArrayList<>();
        return mVehicles;
    }

    void addVehicle(Vehicle vehicle) {
        mVehicles.add(vehicle);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        if (getUserName()  != null ? !getUserName().equals(user.getUserName())   : user.getUserName()  != null) return false;
        if (getFirstName() != null ? !getFirstName().equals(user.getFirstName()) : user.getFirstName() != null) return false;
        if (getLastName()  != null ? !getLastName().equals(user.getLastName())   : user.getLastName()  != null) return false;

        if (!getGuests().equals(user.getGuests())) return false;
        if (!getVehicles().equals(user.getVehicles())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getUserName() != null ? getUserName().hashCode() : 0;
        result = 31 * result + (getFirstName() != null ? getFirstName().hashCode() : 0);
        result = 31 * result + (getLastName() != null ? getLastName().hashCode() : 0);
        result = 31 * result + getGuests().hashCode();
        result = 31 * result + getVehicles().hashCode();
        return result;
    }
}
