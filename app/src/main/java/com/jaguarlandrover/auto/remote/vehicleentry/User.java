package com.jaguarlandrover.auto.remote.vehicleentry;

import android.app.ActionBar;
import android.widget.Button;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;

/**
 * Created by rdz on 8/12/2015.
 */
public class User {
    public String username;
    public String vehicle;
    public String validfrom;
    public String validto;
    public boolean lock_unlock;
    public boolean enginestart;

    public User(String username, String vehicle, String validfrom, String validto,
                boolean lock_unlock, boolean enginestart){
        this.username = username;
        this.vehicle = vehicle;
        this.validfrom = validfrom;
        this.validto = validto;
        this.lock_unlock = lock_unlock;
        this.enginestart = enginestart;
    }

    public User(JSONObject object){
        try{
            this.username = object.getString("username");
            this.vehicle = object.getString("vehicle");
            this.validfrom = object.getString("validfrom");
            this.validto = object.getString("validto");
            this.lock_unlock = object.getBoolean("lock_unlock");
            this.enginestart = object.getBoolean("enginestart");
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    public static ArrayList<User> fromJson(JSONArray jsonobjects){//,LinearLayout layout, keyRevokeActivity activity){
        ArrayList<User> users = new ArrayList<User>();
        for(int i=0; i <jsonobjects.length();i++){
            try{
                users.add(new User(jsonobjects.getJSONObject(i)));

            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        return users;
    }

}
