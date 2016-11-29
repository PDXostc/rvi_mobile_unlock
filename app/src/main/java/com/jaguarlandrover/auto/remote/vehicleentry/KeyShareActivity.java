package com.jaguarlandrover.auto.remote.vehicleentry;
/**
 * Copyright (C) 2015, Jaguar Land Rover
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0.  The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 */

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;


public class KeyShareActivity extends ActionBarActivity implements KeyShareActivityFragment.ShareFragmentButtonListener
{
    private final static String TAG = "UnlockDemo/KeyShareActv";
    static final int dateDialog = 0;
    static final int timeDialog = 1;

    KeyShareActivityFragment mShareFragment;
    Vehicle mSelectedVehicle = new Vehicle();
    User mSnapshotUser = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handleExtra(getIntent());

        setContentView(R.layout.activity_key_share);

        mShareFragment = (KeyShareActivityFragment) getFragmentManager().findFragmentById(R.id.fragmentshare);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Gson gson = new Gson();

            String vehicleString = (String) extras.get("selectedVehicle");
            mSelectedVehicle = gson.fromJson(vehicleString, Vehicle.class);

            String userString = (String) extras.get("snapshotUser");
            mSnapshotUser = gson.fromJson(userString, User.class);

            mShareFragment.setSelectedVehicle(mSelectedVehicle);
            mShareFragment.setSnapshotUser(mSnapshotUser);
        }

        mShareFragment.showUserSelect();
        mShareFragment.showDialog();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == dateDialog)
            return new DatePickerDialog(this, mShareFragment.getDatePickerListener(), mShareFragment.getYear(), mShareFragment.getMonth() - 1, mShareFragment.getDay());
        if (id == timeDialog)
            return new TimePickerDialog(this, mShareFragment.getTimePickerListener(), mShareFragment.getHour(), mShareFragment.getMin(), false);
        return null;
    }

    public void alertMessage() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        try{
                            ServerNode.authorizeServices(mShareFragment.getSharingUser());
                            confirmationMessage();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:

                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure?")
                .setPositiveButton("Share Key", dialogClickListener)
                .setNegativeButton("Cancel", dialogClickListener).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_key_share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void handleExtra(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null && extras.size() > 0) {
            for(String k : extras.keySet()) {
                Log.i(TAG, "k = " + k + " : " + extras.getString(k));
            }
        }

        if (extras != null && "dialog".equals(extras.get("_extra1"))) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(""+extras.get("_extra2"));
            alertDialogBuilder
                    .setMessage(""+extras.get("_extra3"))
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

        super.onDestroy();
    }

    @Override
    public void onButtonCommand(String cmd) {
        Intent intent = new Intent();
        switch (cmd) {
            case "share":
                alertMessage();
                break;
        }
    }

    public void confirmationMessage() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                switch(which){
                    case DialogInterface.BUTTON_POSITIVE:
                        finish();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Key successfully shared")
                .setPositiveButton("Ok", dialogClickListener).show();
    }
}
