package com.jaguarlandrover.auto.remote.vehicleentry;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by rdz on 8/12/2015.
 */
public class UserCredentials
{
    @SerializedName("username")
    private String mUserName;

    @SerializedName("vehicleVIN")
    private String mVehicleVin;

    @SerializedName("validFrom")
    private String mValidFrom          = "1971-09-09T22:00:00.000Z";
    private String mValidFromFormatted = null;

    @SerializedName("validTo")
    private String mValidTo          = "1971-09-09T23:00:00.000Z";
    private String mValidToFormatted = null;

    @SerializedName("userType")
    private String mUserType = "guest";

    @SerializedName("guests")
    private ArrayList<String> mGuests;

    @SerializedName("vehicleName")
    private String mVehicleName;

    @SerializedName("authorizedServices")
    private AuthorizedServices mAuthorizedServices = new AuthorizedServices();

    @SerializedName("certid")
    private String mCertId;

//    private boolean mLockUnlock;
//    private boolean mEngineStart;

    UserCredentials() {
    }

    public UserCredentials(String userName, String vehicle, String validFrom, String validTo, boolean lockUnlock, boolean engineStart) {
        this.setUserName(userName);
        this.setVehicleVin(vehicle);
        this.setValidFrom(validFrom);
        this.setValidTo(validTo);
        this.setLockUnlock(lockUnlock);
        this.setEngineStart(engineStart);
    }

    public UserCredentials(JSONObject object) {
        try {
            this.setUserName(object.getString("username"));
            String start = object.getString("validFrom").substring(0, 23);
            String stop = object.getString("validTo").substring(0, 23);
            try {
                SimpleDateFormat inputformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                SimpleDateFormat outputformat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
                inputformat.setTimeZone(TimeZone.getTimeZone("UTC"));

                Date newStart = inputformat.parse(start);
                Date newEnd = inputformat.parse(stop);
                mValidFromFormatted = outputformat.format(newStart);
                mValidToFormatted = outputformat.format(newEnd);
            } catch (Exception e) {
                Log.d("DATE", "ERROR IN DATE FORMAT");
                e.printStackTrace();
            }

            JSONObject authservices = object.getJSONObject("authorizedServices");
            this.setLockUnlock(authservices.getBoolean("lock"));
            this.setEngineStart(authservices.getBoolean("engine"));
            this.setCertId(object.getString("certid"));
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

    private String newFormat(String oldFormat) {
        String newFormat = oldFormat.substring(0, 23);

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date newDate = inputFormat.parse(newFormat);
            return outputFormat.format(newDate);

        } catch (Exception e) {
            Log.d("DATE", "ERROR IN DATE FORMAT");
            e.printStackTrace();
        }

        return null;
    }

    public boolean isKeyValid() throws ParseException {
        String[] dateTime;

        try {
            dateTime = mValidTo.split("T");
        }
        catch (Exception e) {
            return false;
        }

        String userDate = dateTime[0];
        String userTime = dateTime[1];

        String userDateTime = userDate + " " + userTime;

        SimpleDateFormat formatter1;

        try {
            formatter1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        } catch (Exception e) {
            return false;
        }

        formatter1.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date savedDate = formatter1.parse(userDateTime);
        Date dateNow = new Date();

        return savedDate.compareTo(dateNow) > 0;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        this.mUserName = userName;
    }

    public String getVehicleVin() {
        return mVehicleVin;
    }

    public void setVehicleVin(String vehicleVin) {
        this.mVehicleVin = vehicleVin;
    }

    public String getValidFrom() {
        if (mValidFromFormatted == null) mValidFromFormatted = newFormat(mValidFrom);

        return mValidFromFormatted;
    }

    public void setValidFrom(String validFrom) {
        this.mValidFrom = validFrom;
    }

    public String getValidTo() {
        if (mValidToFormatted == null) mValidToFormatted = newFormat(mValidTo);

        return mValidToFormatted;
    }

    public void setValidTo(String validTo) {
        this.mValidTo = validTo;
    }

    public String getCertId() {
        return mCertId;
    }

    public void setCertId(String certId) {
        this.mCertId = certId;
    }

    public boolean isLockUnlock() {
        return mAuthorizedServices.isLock();
//        if (mAuthorizedServices != null) return mAuthorizedServices.isLock();
//        else return mLockUnlock; // TODO: Remove when done refactoring
    }

    public void setLockUnlock(boolean lockUnlock) {
        mAuthorizedServices.setLock(lockUnlock);
    }

    public boolean isEngineStart() {
        return mAuthorizedServices.isEngine();
//        if (mAuthorizedServices != null) return mAuthorizedServices.isEngine();
//        else return mEngineStart; // TODO: Remove when done refactoring
    }

    public void setEngineStart(boolean engineStart) {
        mAuthorizedServices.setEngine(engineStart);
//        this.mEngineStart = engineStart;
    }

    public String getUserType() {
        return mUserType;
    }

    public void setUserType(String userType) {
        mUserType = userType;
    }

    public ArrayList<String> getGuests() {
        return mGuests;
    }

    public void setGuests(ArrayList<String> guests) {
        mGuests = guests;
    }

    public String getVehicleName() {
        return mVehicleName;
    }

    public void setVehicleName(String vehicleName) {
        mVehicleName = vehicleName;
    }

    public AuthorizedServices getAuthorizedServices() {
        return mAuthorizedServices;
    }

    public void setAuthorizedServices(AuthorizedServices authorizedServices) {
        mAuthorizedServices = authorizedServices;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this, UserCredentials.class);
    }
}

class AuthorizedServices
{
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

    AuthorizedServices() {}

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
}
