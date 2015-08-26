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
    int year_x, month_x, day_x, hour_x, min_x;
    String am_pm;
    TextView startdate, enddate, starttime, endtime;
    static final int dateDialog = 0;
    static final int timeDialog=1;
    private TextView activeDialog;
    private TextView activeTime;
    private Button shareKeyBtn;
    private Switch lock_unlock;
    private Switch enginestart;
    ViewPager userPages;
    ViewPager carPages;
    int[] users={R.drawable.bjamal,
    R.drawable.llesavre,
    R.drawable.dthiriez,
    R.drawable.arodriguez};

    int[] vehicles = {R.drawable.sciontc};
    keyShareActivityFragment share_fragment = null;


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
        share_fragment.showSelect();
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
    public void onButtonCommand(View v) {
        finish();
        Log.i(TAG,"BUTTON CLICKED"+v.toString());
    }

    public void setUserImage(ArrayList<String> userList){

        for(int i=0; i<userList.size();i++){
            users[i]= getResources().getIdentifier(userList.get(i),"drawable", getPackageName());
        }
    }

    public void setVehicleImage(ArrayList<String> carList){

        for(int i=0; i<carList.size();i++){
            vehicles[i]= getResources().getIdentifier(carList.get(i),"drawable", getPackageName());
        }
    }
    public ArrayList<String> rcvDataList(JSONArray data, String datatype){
        JSONObject object = new JSONObject();
        ArrayList<String> item = new ArrayList<String>();
        int repeatcount =0;
        try {
            object = data.getJSONObject(1);
            for(int i=0; i<object.length();i++){
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        /*for(int i=0; i <data.length();i++){
            try{
                object = data.getJSONObject(i);
                item.add(object.getString());
            }catch(JSONException e){
                e.printStackTrace();
            }
        }*/
        return item;
    }

  /*  public JSONArray dummyData(){
        JSONObject users = new JSONObject();
        JSONObject vehicle = new JSONObject();
        try{
            users.accumulate("user", "arodriguez");
            users.accumulate("user", "dthiriez");
            users.accumulate("user", "llesavre");
            users.accumulate("user", "bjamal");
        }catch (JSONException e){
            e.printStackTrace();
        }

        try{
            vehicle.accumulate("vehiclename", "sciontc");
            vehicle.accumulate("vehiclename", "hasdjklhfsdjhf");
            vehicle.accumulate("services", "lock=true");
            vehicle.accumulate("services", "engine=true");
        }catch (JSONException e){
            e.printStackTrace();
        }

        JSONArray jsonArray = new JSONArray();

        jsonArray.put(users);
        jsonArray.put(vehicle);
        return jsonArray;
    }*/
}
