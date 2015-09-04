package com.jaguarlandrover.auto.remote.vehicleentry;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.locks.Lock;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class keyShareActivity extends ActionBarActivity implements keyShareActivityFragment.ShareFragmentButtonListener {
    String TAG = "JSON DATA:";
    static final int dateDialog = 0;
    static final int timeDialog=1;

    keyShareActivityFragment share_fragment;

    private RviService rviservice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handleExtra(getIntent());

        setContentView(R.layout.activity_key_share);

        share_fragment = (keyShareActivityFragment) getFragmentManager().findFragmentById(R.id.fragmentshare);

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
    protected Dialog onCreateDialog(int id){
        if(id == dateDialog)
            return new DatePickerDialog(this,share_fragment.getdplistener(), share_fragment.getyear(), share_fragment.getmonth()-1, share_fragment.getday());
        if(id == timeDialog)
            return  new TimePickerDialog(this, share_fragment.gettplistener(), share_fragment.gethour(), share_fragment.getmin(), false);
        return null;
    }

    public void alertMessage(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                switch(which){
                    case DialogInterface.BUTTON_POSITIVE:
                        try{
                            rviservice.sendaKey(share_fragment.getFormData());
                            Log.d("Form", share_fragment.getFormData().toString());
                        }catch (Exception e){

                        }
                        finish();
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
        if( extras != null && extras.size() > 0 ) {
            for(String k : extras.keySet()) {
                Log.i(TAG, "k = " + k+" : "+extras.getString(k));
            }
        }
        if( extras != null && "dialog".equals(extras.get("_extra1")) ) {
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

    public JSONArray dummyData(){
        JSONObject user = new JSONObject();
        JSONObject authServices = new JSONObject();

        try{
            authServices.put("lock","false");
            authServices.put("start","false");
            authServices.put("trunk","false");
            authServices.put("windows","false");
            authServices.put("lights","false");
            authServices.put("hazard","false");
            authServices.put("horn","false");

        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            user.put("username", "arodriguez");
            user.put("validTo", "2015-09-30T08:01:09.000Z");
            user.put("validFrom", "2015-08-21T23:31:59.000Z");
            user.put("vehicleVIN", "1234567890ABCDEFG");
            user.put("authorizedServices", authServices);


        }catch (JSONException e){
            e.printStackTrace();
        }

        JSONArray jsonArray = new JSONArray();

        jsonArray.put(user);
        return jsonArray;
    }
}
