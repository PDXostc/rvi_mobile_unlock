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

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class User {
    private final static String TAG = "UnlockDemo:User";

    private final static String SERVER_DATE_TIME_FORMATTER = "yyyy-MM-dd'T'HH:mm:ss";
    private final static String PRETTY_DATE_TIME_FORMATTER = "MM/dd/yyyy h:mm a z";

    @SerializedName("username")
    private String mUserName;

    @SerializedName("first_name")
    private String mFirstName;

    @SerializedName("last_name")
    private String mLastName;

    @SerializedName("guests")
    private ArrayList<User> mGuests;

    @SerializedName("vehicles")
    private ArrayList<Vehicle> mVehicles;

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
        return mGuests;
    }

    public void setGuests(ArrayList<User> guests) {
        mGuests = guests;
    }

    public ArrayList<Vehicle> getVehicles() {
        return mVehicles;
    }

    public void setVehicles(ArrayList<Vehicle> vehicles) {
        mVehicles = vehicles;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this, UserCredentials.class);
    }
}
