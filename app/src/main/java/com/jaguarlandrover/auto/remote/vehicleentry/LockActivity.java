/**
 *  Copyright (C) 2015, Jaguar Land Rover
 *
 *  This program is licensed under the terms and conditions of the
 *  Mozilla Public License, version 2.0.  The full text of the
 *  Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 */

package com.jaguarlandrover.auto.remote.vehicleentry;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class LockActivity extends ActionBarActivity implements LockActivityFragment.LockFragmentButtonListener {
    private static final String TAG = "RVI";
    private boolean bound = false;
    private String username;
    private TextView userHeader;
    //private Messenger service = null;
    private Handler keyCheck;
    private RviService rviService = null;
    private Handler request;
    LockActivityFragment lock_fragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate() Activity");

        handleExtra(getIntent());

        keyCheck = new Handler();
        request = new Handler();

        setContentView(R.layout.activity_lock);
        lock_fragment = (LockActivityFragment) getFragmentManager().findFragmentById(R.id.fragmentlock);
        startRepeatingTask();
        //doBindService();
    }

    Runnable StatusCheck = new Runnable() {
        @Override
        public void run() {
            checkforKeys();
            keyCheck.postDelayed(StatusCheck, 5000);
        }
    };

    Runnable requestCheck = new Runnable() {
        @Override
        public void run() {
            requestComplete();
            request.postDelayed(requestCheck, 500);
        }
    };

    void startRequest(){
        requestCheck.run();
    }
    void done(){
        request.removeCallbacks(requestCheck);
    }
    void startRepeatingTask(){
        StatusCheck.run();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleExtra(intent);
    }

    @Override
    public void onResume() {super.onResume();}

    private void handleExtra(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null && extras.size() > 0) {
            for (String k : extras.keySet()) {
                Log.i(TAG, "k = " + k + " : " + extras.getString(k));
            }
        }
        if (extras != null && "dialog".equals(extras.get("_extra1"))) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("" + extras.get("_extra2"));
            alertDialogBuilder
                    .setMessage("" + extras.get("_extra3"))
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            alertDialogBuilder.create().show();
        }
    }


    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy() Activity");
        //doUnbindService();

        super.onDestroy();
        //For testing cleanup
        //Intent i = new Intent(this, RviService.class);
        //stopService(i);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lock, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setClass(LockActivity.this, AdvancedPreferenceActivity.class);
            startActivityForResult(intent, 0);
            return true;
        } else if (id == R.id.action_reset) {
            PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply(); //reset
            PreferenceManager.setDefaultValues(this, R.xml.advanced, true);
            return true;
        } else if (id == R.id.action_quit) {
            Intent i = new Intent(this, RviService.class);
            stopService(i);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onButtonCommand(String cmd) {
        //TODO send to RVI service
        rviService.service(cmd, LockActivity.this);
    }

    public void keyUpdate(final String authServ, final String userType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LockActivity.this);
        builder.setInverseBackgroundForced(true);
        builder.setMessage("Updates have been made").setCancelable(false).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        lock_fragment.setButtons(authServ, userType);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void keyShareCommand(String key) {
        Intent intent = new Intent();
        switch (key) {
            case "keyshare":
                intent.setClass(LockActivity.this, keyShareActivity.class);
                startActivityForResult(intent, 0);
                break;
            case "keychange":
                try {
                    rviService.requestAll(Request());
                    startRequest();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }
    public JSONArray Request() throws JSONException {
        JSONObject request = new JSONObject();
        request.put("vehicleVIN", "1234567890ABCDEFG");

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(request);
        return jsonArray;
    }

    public void checkforKeys(){
        SharedPreferences sharedpref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor e = sharedpref.edit();
        String showme = lock_fragment.JSONParser(sharedpref.getString("Userdata", "Nothing There!!"), "authorizedServices");
        String userType = lock_fragment.JSONParser(sharedpref.getString("Userdata", "Nothing there!!"), "userType");
        if (sharedpref.getString("newdata", "nothing").equals("true")) {
            keyUpdate(showme, userType);
            e.putString("newdata", "false");
            e.commit();
        }
    }

    public void requestComplete(){
        SharedPreferences sharedpref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor e = sharedpref.edit();
        String newRequest = sharedpref.getString("newKeyList", "nothing");
        if(newRequest.equals("true")){
            done();
            e.putString("newKeyList", "false");
            e.commit();
            Intent intent = new Intent();
            intent.setClass(LockActivity.this, keyRevokeActivity.class);
            startActivityForResult(intent, 0);
        }
    }
}
