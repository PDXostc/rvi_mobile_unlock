package com.jaguarlandrover.auto.remote.vehicleentry;

import android.app.ActionBar;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.locks.Lock;

/**
 * Created by rdz on 8/12/2015.
 */
public class User {
    public String username;
    public String vehicle;
    public String validfrom;
    public String validto;
    public String certid;
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
            String start = object.getString("validFrom").substring(0,23);
            String stop = object.getString("ValidTo").substring(0,23);
            try {
                SimpleDateFormat inputformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                SimpleDateFormat outputformat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
                inputformat.setTimeZone(TimeZone.getTimeZone("UTC"));

                Date newStart = inputformat.parse(start);
                Date newEnd = inputformat.parse(stop);
                this.validfrom = outputformat.format(newStart);
                this.validto = outputformat.format(newEnd);
            }catch (Exception e){Log.d("DATE","ERROR IN DATE FORMAT");
                e.printStackTrace();}

            JSONObject authservices = object.getJSONObject("authorizedServices");
            this.lock_unlock = authservices.getBoolean("lock");
            this.enginestart = authservices.getBoolean("engine");
            this.certid = object.getString("certid");
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    public static ArrayList<User> fromJson(JSONArray jsonobjects){//,LinearLayout layout, keyRevokeActivity activity){
        ArrayList<User> users = new ArrayList<User>();
        for(int i=0; i <jsonobjects.length();i++){
            try{
                users.add(new User(jsonobjects.getJSONObject(i)));
                //Log.i("DATA", jsonobjects.getJSONObject(i).toString());
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        return users;
    }

}
