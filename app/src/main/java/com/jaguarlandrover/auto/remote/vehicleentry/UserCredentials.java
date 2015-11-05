package com.jaguarlandrover.auto.remote.vehicleentry;

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

/**
 * Created by rdz on 8/12/2015.
 */
public class UserCredentials
{
    private final static String TAG = "UnlockDemo:UserCrede...";

    private final static String SERVER_DATE_TIME_FORMATTER = "yyyy-MM-dd'T'HH:mm:ss";
    private final static String PRETTY_DATE_TIME_FORMATTER = "MM/dd/yyyy h:mm a z";

    @SerializedName("username")
    private String mUserName;

    @SerializedName("vehicleVIN")
    private String mVehicleVin;

    @SerializedName("validFrom")
    private String mValidFrom = "1971-09-09T22:00:00.000Z";

    @SerializedName("validTo")
    private String mValidTo   = "1971-09-09T23:00:00.000Z";

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

    public String getCertId() {
        return mCertId;
    }

    public void setCertId(String certId) {
        this.mCertId = certId;
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
