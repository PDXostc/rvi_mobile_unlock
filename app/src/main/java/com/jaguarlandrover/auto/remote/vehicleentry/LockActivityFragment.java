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
import android.os.Looper;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class LockActivityFragment extends Fragment {

    public static final String STOPPED_LBL="StartStop";
    public static final String LOCKED_LBL="OpenClose";

    private static final String TAG = "RVI";

    private Button lock;
    private Button unlock;
    private Button start;
    private Button stop;
    private Button trunk;
    private Button panic;
    private Button panicOn;

    private LockFragmentButtonListener buttonListener;

    //Temp button press storage
    private SharedPreferences sharedPref;

    public LockActivityFragment() {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lock, container, false);

        Typeface fontawesome = Typeface.createFromAsset(getActivity().getAssets(), "fonts/fontawesome-webfont.ttf");

        lock = (Button) view.findViewById(R.id.lock);
        unlock = (Button) view.findViewById(R.id.unlock);
        start = (Button) view.findViewById(R.id.start);
        stop = (Button) view.findViewById(R.id.stop);
        trunk = (Button) view.findViewById(R.id.trunk);
        panic = (Button) view.findViewById(R.id.panic);
        panicOn = (Button) view.findViewById(R.id.panicOn);

        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

        unlock.setTypeface(fontawesome);
        lock.setTypeface(fontawesome);
        start.setTypeface(fontawesome);
        stop.setTypeface(fontawesome);
        trunk.setTypeface(fontawesome);
        panic.setTypeface(fontawesome);
        panicOn.setTypeface(fontawesome);

        lock.setOnClickListener(l);
        unlock.setOnClickListener(l);
        start.setOnClickListener(l);
        stop.setOnClickListener(l);
        trunk.setOnClickListener(l);
        panic.setOnClickListener(l);
        panicOn.setOnClickListener(l);

        buttonListener = (LockFragmentButtonListener) getActivity();

        return view;
    }

    public void sendPoptrunk(View view) {
    }

    public void sendPanic(View view) {
    }

    public void sendPanicOff(View view) {
    }

    public void onViewStateRestored (Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        //Assume auto unlock
        sharedPref.edit().putBoolean(LOCKED_LBL, false).commit();
        //assume stopped
        sharedPref.edit().putBoolean(STOPPED_LBL, true).commit();

        toggleButtonsFromPref();
    }


    private View.OnClickListener l = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SharedPreferences.Editor ed = sharedPref.edit();
            switch(v.getId()) {
                case R.id.lock:
                    Log.i(TAG,"LockBtn");
                    ed.putBoolean(LOCKED_LBL,true);
                    buttonListener.onButtonCommand("lock");
                    break;
                case R.id.unlock:
                    Log.i(TAG,"UnlockBtn");
                    ed.putBoolean(LOCKED_LBL, false);
                    buttonListener.onButtonCommand("unlock");
                    break;
                case R.id.start:
                    Log.i(TAG,"StartBtn");
                    ed.putBoolean(STOPPED_LBL, false);
                    break;
                case R.id.stop:
                    Log.i(TAG,"StopBtn");
                    ed.putBoolean(STOPPED_LBL, true);
                    break;
                case R.id.trunk:Log.i(TAG, "TrunkBtn");
                    ed.putBoolean("Gruka", false);
                    buttonListener.onButtonCommand("trunk");
                    break;
                case R.id.panic:
                    Log.i(TAG, "PanicBtn");
                    panicOn.setVisibility(View.VISIBLE);
                    panic.setVisibility(View.GONE);
                    buttonListener.onButtonCommand("panic");
                    Log.i(TAG, "PanicBtn swap 1 ");
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            panic.setVisibility(View.VISIBLE);
                            panicOn.setVisibility(View.GONE);
                            Log.i(TAG, "PanicBtn swap 2 ");
                        }
                    }, 5000);
                    break;
                case R.id.panicOn:Log.i(TAG,"PanicOnBtn");break;
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
        boolean stopped = sharedPref.getBoolean(STOPPED_LBL,true);

        lock.setVisibility(locked?View.GONE:View.VISIBLE);
        unlock.setVisibility(locked?View.VISIBLE:View.GONE);

        trunk.setEnabled(true);
    }

    public void onNewServiceDiscovered(String... service) {
        for(String s:service)
            Log.e(TAG, "Service = " + s);
    }

    public interface LockFragmentButtonListener {
        public void onButtonCommand(String cmd);
    }
}
