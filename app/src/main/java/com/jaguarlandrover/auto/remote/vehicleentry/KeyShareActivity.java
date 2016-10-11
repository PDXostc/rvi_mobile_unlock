package com.jaguarlandrover.auto.remote.vehicleentry;

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

import java.util.ArrayList;

public class KeyShareActivity extends ActionBarActivity implements KeyShareActivityFragment.ShareFragmentButtonListener {
    private final static String TAG = "UnlockDemo/KeyShareActv";
    static final int dateDialog = 0;
    static final int timeDialog = 1;

    KeyShareActivityFragment share_fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handleExtra(getIntent());

        setContentView(R.layout.activity_key_share);

        share_fragment = (KeyShareActivityFragment) getFragmentManager().findFragmentById(R.id.fragmentshare);

        ArrayList<String> userList = new ArrayList<String>();
        ArrayList<String> vehicleList = new ArrayList<String>();
        //userList = rcvDataList(dummyData(), getResources().getString(R.string.USERNAME));
        //vehicleList = rcvDataList(dummyData(), getResources().getString(R.string.VEHICLE));
        //Log.i(TAG, dummyData().toString());

        //setUserImage(userList);
        //setVehicleImage(vehicleList);
        share_fragment.showUserSelect();
        share_fragment.showCarSelect();
        share_fragment.showDialog();
        //String selectedUser = getResources().getString(users[userPages.getCurrentItem()]);
        //Toast.makeText(keyShareActivity.this, selectedUser, Toast.LENGTH_LONG).show();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == dateDialog)
            return new DatePickerDialog(this, share_fragment.getdplistener(), share_fragment.getyear(), share_fragment.getmonth() - 1, share_fragment.getday());
        if (id == timeDialog)
            return new TimePickerDialog(this, share_fragment.gettplistener(), share_fragment.gethour(), share_fragment.getmin(), false);
        return null;
    }

    public void alertMessage() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        try{
                            //RviService.sendKey(share_fragment.getSharingUser());
                            ServerNode.authorizeServices(share_fragment.getSharingUser());
                            confirmationMessage();
                        } catch (Exception e) {

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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_key_share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void handleExtra(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null && extras.size() > 0) {
            for(String k : extras.keySet()) {
                Log.i(TAG, "k = " + k+" : "+extras.getString(k));
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
        //doUnbindService();

        super.onDestroy();
        //For testing cleanup
        //Intent i = new Intent(this, RviService.class);
        //stopService(i);
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
