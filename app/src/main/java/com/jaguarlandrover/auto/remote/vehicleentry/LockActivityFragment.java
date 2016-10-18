/**
 *  Copyright (C) 2015, Jaguar Land Rover
 *
 *  This program is licensed under the terms and conditions of the
 *  Mozilla Public License, version 2.0.  The full text of the
 *  Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 */

package com.jaguarlandrover.auto.remote.vehicleentry;

import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.internal.view.menu.MenuView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;

public class LockActivityFragment extends Fragment implements AdapterView.OnItemSelectedListener
{
    public static final String STOPPED_LBL = "StartStop";
    public static final String LOCKED_LBL  = "OpenClose";

    private static final String TAG = "UnlockDemo/LckActvtyFrg";

    private boolean revokeCheckStarted = false;

    private Spinner      mVehicleSpinner;
    private ImageButton  mLock;
    private ImageButton  mUnlock;
    private ImageButton  mTrunk;
    private ImageButton  mFind;
    private ImageButton  mStart;
    private ImageButton  mStop;
    private ImageButton  mPanic;
    private ImageButton  mChange;
    private ImageButton  mShare;
    private TextView     mKeyLabel;
    private TextView     mValidDate;
    private TextView     mValidTime;
    private TextView     mUserHeader;
    private TextView     mVehicleHeader;
    private ImageView    mServerConnected;
    private ImageView    mVehicleConnected;
    private LinearLayout mKeyManagementLayout;
    private LockFragmentButtonListener mButtonListener;
    //private Handler                    buttonSet;

    //Temp button press storage
    private SharedPreferences          sharedPref;
    private ArrayList<Vehicle> mPreviousVehicles;

    //    private Button panicOn;


    public LockActivityFragment() {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_lock, container, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        Typeface fontawesome = Typeface.createFromAsset(getActivity().getAssets(), "fonts/fontawesome-webfont.ttf");

        mVehicleSpinner = (Spinner) view.findViewById(R.id.vehicle_spinner);
        mLock = (ImageButton) view.findViewById(R.id.lock);
        mUnlock = (ImageButton) view.findViewById(R.id.unlock);
        mTrunk = (ImageButton) view.findViewById(R.id.trunk);
        mFind = (ImageButton) view.findViewById(R.id.find);
        mStart = (ImageButton) view.findViewById(R.id.start);
        mStop = (ImageButton) view.findViewById(R.id.stop);
        mPanic = (ImageButton) view.findViewById(R.id.panic);
        mShare = (ImageButton) view.findViewById(R.id.share);
        mChange = (ImageButton) view.findViewById(R.id.change);
        mKeyLabel = (TextView) view.findViewById(R.id.keysharelbl);
        mValidDate = (TextView) view.findViewById(R.id.guestvalidDate);
        mValidTime = (TextView) view.findViewById(R.id.guestvalidTime);
        mUserHeader = (TextView)view.findViewById(R.id.user_header);
        mVehicleHeader = (TextView) view.findViewById(R.id.vehicle_header);
        mKeyManagementLayout = (LinearLayout) view.findViewById(R.id.key_management_layout);
        mServerConnected = (ImageView) view.findViewById(R.id.server_connected);
        mVehicleConnected = (ImageView) view.findViewById(R.id.vehicle_connected);

        //buttonSet = new Handler();
        //startRepeatingTask();

        mVehicleSpinner.setOnItemSelectedListener(this);

        mLock.setOnClickListener(l);
        mUnlock.setOnClickListener(l);
        mTrunk.setOnClickListener(l);
        mFind.setOnClickListener(l);
        mStart.setOnClickListener(l);
        mStop.setOnClickListener(l);
        mPanic.setOnClickListener(l);
        mShare.setOnClickListener(l);
        mChange.setOnClickListener(l);

        mButtonListener = (LockFragmentButtonListener) getActivity();

        User user = ServerNode.getUserData();
        user.setSelectedVehicleIndex(-1);

        Integer selectedVehicleIndex = user.getSelectedVehicleIndex();

        if (selectedVehicleIndex == -1 && user.getVehicles().size() != 0) {
            user.setSelectedVehicleIndex(0);
        }

        updateUserInterface();

        if (ServerNode.isConnected())
            mServerConnected.setImageResource(R.drawable.connected);

        if (VehicleNode.isConnected())
            mVehicleConnected.setImageResource(R.drawable.connected);

        return view;
    }

    public void sendPoptrunk(View view) {
    }

    public void sendPanic(View view) {
    }

    public void sendPanicOff(View view) {
    }

    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        //Assume auto mUnlock
        sharedPref.edit().putBoolean(LOCKED_LBL, false).commit();
        //assume stopped
        sharedPref.edit().putBoolean(STOPPED_LBL, true).commit();

        toggleButtonsFromPref();
    }

    private View.OnClickListener l = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            SharedPreferences.Editor ed = sharedPref.edit();
            switch (v.getId()) {
                case R.id.lock:
                    Log.i(TAG, "LockBtn");
                    ed.putBoolean(LOCKED_LBL, true);
                    mButtonListener.onButtonCommand(VehicleNode.FOB_SIGNAL_LOCK);
                    break;
                case R.id.unlock:
                    Log.i(TAG, "UnlockBtn");
                    ed.putBoolean(LOCKED_LBL, false);
                    mButtonListener.onButtonCommand(VehicleNode.FOB_SIGNAL_UNLOCK);
                    break;
                case R.id.trunk:
                    Log.i(TAG, "TrunkBtn");
                    ed.putBoolean("Gruka", false);
                    mButtonListener.onButtonCommand(VehicleNode.FOB_SIGNAL_TRUNK);
                    break;
                case R.id.find:
                    Log.i(TAG, "FindBtn");
                    ed.putBoolean("77", false);
                    mButtonListener.onButtonCommand(VehicleNode.FOB_SIGNAL_LIGHTS);
                    break;
                case R.id.start:
                    Log.i(TAG, "StartBtn");
                    ed.putBoolean(STOPPED_LBL, true);
                    mButtonListener.onButtonCommand(VehicleNode.FOB_SIGNAL_START);
                    break;
                case R.id.stop:
                    Log.i(TAG, "StopBtn");
                    ed.putBoolean(STOPPED_LBL, false);
                    mButtonListener.onButtonCommand(VehicleNode.FOB_SIGNAL_STOP);
                    break;
                case R.id.share:
                    Log.i(TAG, "ShareBtn");
                    mButtonListener.keyShareCommand("key_share");
                    break;
                case R.id.change:
                    Log.i(TAG, "ChangeBtn");
                    mButtonListener.keyShareCommand("key_revoke");
                    break;
                case R.id.panic:
                    Log.i(TAG, "PanicBtn");
                    break;
            }

            Log.i(TAG, "Before commit");
            ed.commit();
            //ed.apply();
            Log.i(TAG, "After commit");

            toggleButtonsFromPref();
            Log.i(TAG, "After toggle");
        }
    };

    private void toggleButtonsFromPref() {

        boolean locked = sharedPref.getBoolean(LOCKED_LBL, true);
        boolean stopped = sharedPref.getBoolean(STOPPED_LBL, true);

//        mUnlock.setSelected(!locked);
//        mLock.setSelected(locked);
//        start.setSelected(stopped);
//        stop.setSelected(!stopped);

        //mLock.setVisibility(locked?View.GONE:View.VISIBLE);
        //mUnlock.setVisibility(locked?View.VISIBLE:View.GONE);

        //start.setVisibility(stopped?View.GONE:View.VISIBLE);
        //stop.setVisibility(stopped?View.VISIBLE:View.GONE);

        //trunk.setEnabled(true);
    }

    public void onNewServiceDiscovered(String... service) {
        for (String s : service)
            Log.e(TAG, "Service = " + s);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemSelected: " + Integer.toString(position));

        User user = ServerNode.getUserData();
        user.setSelectedVehicleIndex(position);

        updateUserInterface();
        updateVehicleConnection();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.d(TAG, "onNothingSelected");

        User user = ServerNode.getUserData();
        user.setSelectedVehicleIndex(-1);

        updateUserInterface();
    }

    public interface LockFragmentButtonListener
    {
        public void onButtonCommand(String cmd);

        public void keyShareCommand(String key);
    }

    void updateVehicleSpinnerAdapter(ArrayList<Vehicle> vehicles) {
        Log.d(TAG, "updateVehicleSpinnerAdapter");

        if (vehicles.equals(mPreviousVehicles)) return;

        mPreviousVehicles = vehicles;

        List<String> vehicleNames = new ArrayList<String>();
        if (vehicles == null || vehicles.size() == 0) {
            vehicleNames.add("<no vehicle>");
        } else {
            for (Vehicle vehicle : vehicles) {
                vehicleNames.add(vehicle.getDisplayName());
            }
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, vehicleNames);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mVehicleSpinner.setAdapter(dataAdapter);
    }

    void updateVehicleConnection() {
        User user = ServerNode.getUserData();

        Integer selectedVehicleIndex = user.getSelectedVehicleIndex();
        VehicleNode.disconnect();

        if (selectedVehicleIndex != -1) {
            Vehicle vehicle = user.getVehicles().get(selectedVehicleIndex);

            VehicleNode.setServerUrl(vehicle.getVehicleUrl());
            VehicleNode.setServerPort(vehicle.getVehiclePort());

            VehicleNode.connect();
        }
    }

    public void updateUserInterface() {
        User user = ServerNode.getUserData();

        Integer selectedVehicleIndex = user.getSelectedVehicleIndex();
        Vehicle vehicle = (selectedVehicleIndex != -1) ? user.getVehicles().get(selectedVehicleIndex) : new Vehicle();

        String username = user.getFirstName() != null ? user.getFirstName() : "unknown";

        updateVehicleSpinnerAdapter(user.getVehicles());

        Log.d(TAG, "Saved userdata: " + user.toString());

        mUserHeader.setText(username);
        mVehicleHeader.setText("Vehicle: ");

        try {
            if (vehicle.getUserType().equals("guest")) {
                mKeyLabel.setText("Key Valid To:");

                mKeyManagementLayout.setVisibility(View.GONE);
                mValidDate.setVisibility(View.VISIBLE);

                if (!vehicle.hasAnyAuthorizedServices() || !vehicle.isKeyValid()) {
                    mLock.setEnabled(false);
                    mUnlock.setEnabled(false);
                    mTrunk.setEnabled(false);
                    mFind.setEnabled(false);
                    mStart.setEnabled(false);
                    mStop.setEnabled(false);
                    mPanic.setEnabled(false);

                    mValidDate.setText("Revoked");
                } else {
                    mLock.setEnabled(vehicle.getAuthorizedServices().isLock());
                    mUnlock.setEnabled(vehicle.getAuthorizedServices().isLock());
                    mTrunk.setEnabled(vehicle.getAuthorizedServices().isTrunk());
                    mFind.setEnabled(vehicle.getAuthorizedServices().isLights());
                    mStart.setEnabled(vehicle.getAuthorizedServices().isEngine());
                    mStop.setEnabled(vehicle.getAuthorizedServices().isEngine());
                    mPanic.setEnabled(vehicle.getAuthorizedServices().isHazard());

                    mValidDate.setText(vehicle.getValidTo());
                }

            } else if (vehicle.getUserType().equals("owner")) {
                mValidDate.setVisibility(View.GONE);
                mKeyManagementLayout.setVisibility(View.VISIBLE);
                mLock.setEnabled(true);
                mUnlock.setEnabled(true);
                mTrunk.setEnabled(true);
                mFind.setEnabled(true);
                mStart.setEnabled(true);
                mStop.setEnabled(true);
                mPanic.setEnabled(true);

            }
        } catch (Exception e) {
            mValidDate.setVisibility(View.VISIBLE);
            mKeyManagementLayout.setVisibility(View.GONE);
            mLock.setEnabled(false);
            mUnlock.setEnabled(false);
            mTrunk.setEnabled(false);
            mFind.setEnabled(false);
            mStart.setEnabled(false);
            mStop.setEnabled(false);
            mPanic.setEnabled(false);

            e.printStackTrace();
        }
    }

    void setServerConnected(boolean serverConnected) {
        if (serverConnected)
            mServerConnected.setImageResource(R.drawable.connected);
        else
            mServerConnected.setImageResource(R.drawable.disconnected);
    }

    void setVehicleConnected(boolean vehicleConnected) {
        if (vehicleConnected)
            mVehicleConnected.setImageResource(R.drawable.connected);
        else
            mVehicleConnected.setImageResource(R.drawable.disconnected);
    }
}
