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
import android.os.Handler;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LockActivityFragment extends Fragment implements AdapterView.OnItemSelectedListener
{
    public static final String STOPPED_LBL = "StartStop";
    public static final String LOCKED_LBL  = "OpenClose";

    private static final String TAG = "UnlockDemo/LckActvtyFrg";

    private boolean revokeCheckStarted = false;

    private Spinner                    vehicleSpinner;
    private ImageButton                lock;
    private ImageButton                unlock;
    private ImageButton                trunk;
    private ImageButton                find;
    private ImageButton                start;
    private ImageButton                stop;
    private ImageButton                panic;
    private ImageButton                change;
    private ImageButton                share;
    private TextView                   keylbl;
    private TextView                   validDate;
    private TextView                   validTime;
    private TextView                   userHeader;
    private TextView                   vehicleHeader;
    private LinearLayout               keyManagementLayout;
    private LockFragmentButtonListener buttonListener;
    private Handler                    buttonSet;

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

        vehicleSpinner = (Spinner) view.findViewById(R.id.vehicle_spinner);
        lock = (ImageButton) view.findViewById(R.id.lock);
        unlock = (ImageButton) view.findViewById(R.id.unlock);
        trunk = (ImageButton) view.findViewById(R.id.trunk);
        find = (ImageButton) view.findViewById(R.id.find);
        start = (ImageButton) view.findViewById(R.id.start);
        stop = (ImageButton) view.findViewById(R.id.stop);
        panic = (ImageButton) view.findViewById(R.id.panic);
        share = (ImageButton) view.findViewById(R.id.share);
        change = (ImageButton) view.findViewById(R.id.change);
        keylbl = (TextView) view.findViewById(R.id.keysharelbl);
        validDate = (TextView) view.findViewById(R.id.guestvalidDate);
        validTime = (TextView) view.findViewById(R.id.guestvalidTime);
        userHeader = (TextView)view.findViewById(R.id.user_header);
        vehicleHeader = (TextView) view.findViewById(R.id.vehicle_header);
        keyManagementLayout = (LinearLayout) view.findViewById(R.id.key_management_layout);

//        User user = ServerNode.getUserData();
//        if (user == null) {
//            updateUserInterface(new User());
//        } else {
//            updateUserInterface(user);
//        }

        buttonSet = new Handler();
        startRepeatingTask();

        vehicleSpinner.setOnItemSelectedListener(this);

        lock.setOnClickListener(l);
        unlock.setOnClickListener(l);
        trunk.setOnClickListener(l);
        find.setOnClickListener(l);
        start.setOnClickListener(l);
        stop.setOnClickListener(l);
        panic.setOnClickListener(l);
        share.setOnClickListener(l);
        change.setOnClickListener(l);

        buttonListener = (LockFragmentButtonListener) getActivity();

        User user = ServerNode.getUserData();
        user.setSelectedVehicleIndex(-1);

        updateUserInterface();

        return view;
    }

    Runnable StatusCheck = new Runnable()
    {
        @Override
        public void run() { // TODO: Still probably need to fix the logic here

            User user = ServerNode.getUserData();

            Integer selectedVehicleIndex = user.getSelectedVehicleIndex();
            Vehicle vehicle = (selectedVehicleIndex != -1) ? user.getVehicles().get(selectedVehicleIndex) : new Vehicle();

            if (vehicle.getUserType().equals("guest") && !vehicle.isKeyValid()) { // TODO: Check logic now that changed
                updateUserInterface();//user);
            }

            // Revoke check at the beginning of every minute
            if (!revokeCheckStarted) {
                revokeCheckStarted = true;
                Calendar calendar = Calendar.getInstance();
                int seconds = calendar.get(Calendar.SECOND);
                int sleepSecs = 60 - seconds;
                buttonSet.postDelayed(StatusCheck, sleepSecs * 1000);
            } else {
                buttonSet.postDelayed(StatusCheck, 60000);
            }

//            updateUserInterface();
//            buttonSet.postDelayed(StatusCheck, 15000);
        }
    };

    void startRepeatingTask() {
        StatusCheck.run();
    }

    void stopRepeatingTask() {
        buttonSet.removeCallbacks(StatusCheck);
    }

    public void sendPoptrunk(View view) {
    }

    public void sendPanic(View view) {
    }

    public void sendPanicOff(View view) {
    }

    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        //Assume auto unlock
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
                    buttonListener.onButtonCommand(VehicleNode.FOB_SIGNAL_LOCK);
                    break;
                case R.id.unlock:
                    Log.i(TAG, "UnlockBtn");
                    ed.putBoolean(LOCKED_LBL, false);
                    buttonListener.onButtonCommand(VehicleNode.FOB_SIGNAL_UNLOCK);
                    break;
                case R.id.trunk:
                    Log.i(TAG, "TrunkBtn");
                    ed.putBoolean("Gruka", false);
                    buttonListener.onButtonCommand(VehicleNode.FOB_SIGNAL_TRUNK);
                    break;
                case R.id.find:
                    Log.i(TAG, "FindBtn");
                    ed.putBoolean("77", false);
                    buttonListener.onButtonCommand(VehicleNode.FOB_SIGNAL_LIGHTS);
                    break;
                case R.id.start:
                    Log.i(TAG, "StartBtn");
                    ed.putBoolean(STOPPED_LBL, true);
                    buttonListener.onButtonCommand(VehicleNode.FOB_SIGNAL_START);
                    break;
                case R.id.stop:
                    Log.i(TAG, "StopBtn");
                    ed.putBoolean(STOPPED_LBL, false);
                    buttonListener.onButtonCommand(VehicleNode.FOB_SIGNAL_STOP);
                    break;
                case R.id.share:
                    Log.i(TAG, "ShareBtn");
                    buttonListener.keyShareCommand("keyshare");
                    break;
                case R.id.change:
                    Log.i(TAG, "ChangeBtn");
                    buttonListener.keyShareCommand("keychange");
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

//        unlock.setSelected(!locked);
//        lock.setSelected(locked);
//        start.setSelected(stopped);
//        stop.setSelected(!stopped);

        //lock.setVisibility(locked?View.GONE:View.VISIBLE);
        //unlock.setVisibility(locked?View.VISIBLE:View.GONE);

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
        User user = ServerNode.getUserData();
        user.setSelectedVehicleIndex(position);

        updateUserInterface();//user);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        User user = ServerNode.getUserData();
        user.setSelectedVehicleIndex(-1);

        updateUserInterface();//user);
    }

    public interface LockFragmentButtonListener
    {
        public void onButtonCommand(String cmd);

        public void keyShareCommand(String key);
    }

    void updateVehicleSpinnerAdapter(ArrayList<Vehicle> vehicles) {
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

        vehicleSpinner.setAdapter(dataAdapter);
    }

    public void updateUserInterface() {//User user) {
        //if (user == null) user = new User();

        User user = ServerNode.getUserData();

        Integer selectedVehicleIndex = user.getSelectedVehicleIndex();
        Vehicle vehicle = (selectedVehicleIndex != -1) ? user.getVehicles().get(selectedVehicleIndex) : new Vehicle();

        String username = user.getFirstName() != null ? user.getFirstName() : "unknown";

        updateVehicleSpinnerAdapter(user.getVehicles());

//        if (selectedVehicleIndex != -1 && selectedVehicleIndex != vehicleSpinner.getSelectedItemPosition()) vehicleSpinner.setSelection(selectedVehicleIndex);

        Log.d(TAG, "Saved userdata: " + user.toString());

        userHeader.setText(String.format(getString(R.string.User), username));
        vehicleHeader.setText("Vehicle: ");

        try {
            if (vehicle.getUserType().equals("guest")) {
                keylbl.setText("Key Valid To:");

                keyManagementLayout.setVisibility(View.GONE);
                validDate.setVisibility(View.VISIBLE);

                if (!vehicle.hasAnyAuthorizedServices() || !vehicle.isKeyValid()) {
                    lock.setEnabled(false);
                    unlock.setEnabled(false);
                    trunk.setEnabled(false);
                    find.setEnabled(false);
                    start.setEnabled(false);
                    stop.setEnabled(false);
                    panic.setEnabled(false);

                    validDate.setText("Revoked");
                } else {
                    lock.setEnabled(vehicle.getAuthorizedServices().isLock());
                    unlock.setEnabled(vehicle.getAuthorizedServices().isLock());
                    trunk.setEnabled(vehicle.getAuthorizedServices().isTrunk());
                    find.setEnabled(vehicle.getAuthorizedServices().isLights());
                    start.setEnabled(vehicle.getAuthorizedServices().isEngine());
                    stop.setEnabled(vehicle.getAuthorizedServices().isEngine());
                    panic.setEnabled(vehicle.getAuthorizedServices().isHazard());

                    validDate.setText(vehicle.getValidTo());
                }

            } else if (vehicle.getUserType().equals("owner")) {
                validDate.setVisibility(View.GONE);
                keyManagementLayout.setVisibility(View.VISIBLE);
                lock.setEnabled(true);
                unlock.setEnabled(true);
                trunk.setEnabled(true);
                find.setEnabled(true);
                start.setEnabled(true);
                stop.setEnabled(true);
                panic.setEnabled(true);

            }
        } catch (Exception e) {
            validDate.setVisibility(View.VISIBLE);
            keyManagementLayout.setVisibility(View.GONE);
            lock.setEnabled(false);
            unlock.setEnabled(false);
            trunk.setEnabled(false);
            find.setEnabled(false);
            start.setEnabled(false);
            stop.setEnabled(false);
            panic.setEnabled(false);

            e.printStackTrace();
        }
    }
}
