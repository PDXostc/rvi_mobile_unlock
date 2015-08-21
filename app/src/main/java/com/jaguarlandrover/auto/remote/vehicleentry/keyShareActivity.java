package com.jaguarlandrover.auto.remote.vehicleentry;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
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

public class keyShareActivity extends ActionBarActivity {
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
    R.drawable.arodriguez,
    R.drawable.dthiriez};
    int[] vehicles = {R.drawable.sciontc};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_share);
        ArrayList<String> userList = new ArrayList<String>();
        ArrayList<String> vehicleList = new ArrayList<String>();
        //userList = rcvDataList(dummyData(), getResources().getString(R.string.USERNAME));
        //vehicleList = rcvDataList(dummyData(), getResources().getString(R.string.VEHICLE));
        Log.i(TAG, dummyData().toString());

        //setUserImage(userList);
        //setVehicleImage(vehicleList);
        //showSelect();
        showDialog();

        shareKeyBtn = (Button) findViewById(R.id.ShareBtn);
        lock_unlock = (Switch) findViewById(R.id.lock_unlock);
        enginestart = (Switch) findViewById(R.id.engine);

        shareKeyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //String selectedUser = getResources().getString(users[userPages.getCurrentItem()]);
                //Toast.makeText(keyShareActivity.this, selectedUser, Toast.LENGTH_LONG).show();
            }
        });
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
    public void showSelect(){

        ScrollPageAdapter userPageAdapter = new ScrollPageAdapter(this, users);
        userPages = (ViewPager) findViewById(R.id.userscroll);
        userPages.setOffscreenPageLimit(2);
        userPages.setPageMargin(-500);
        userPages.setHorizontalFadingEdgeEnabled(true);
        userPages.setFadingEdgeLength(50);
        userPages.setAdapter(userPageAdapter);

        ScrollPageAdapter carPageAdapter = new ScrollPageAdapter(this, vehicles);
        carPages = (ViewPager) findViewById(R.id.vehiclescroll);
        userPages.setOffscreenPageLimit(2);
        carPages.setPageMargin(-500);
        carPages.setHorizontalFadingEdgeEnabled(true);
        carPages.setFadingEdgeLength(50);
        carPages.setAdapter(carPageAdapter);

    }

    public void showDialog(){
        final Calendar cal = Calendar.getInstance();
        year_x = cal.get(Calendar.YEAR);
        month_x = cal.get(Calendar.MONTH);
        month_x = month_x+1;
        day_x = cal.get(Calendar.DAY_OF_MONTH);

        startdate = new TextView(this);
        enddate = new TextView(this);
        starttime = new TextView(this);
        endtime = new TextView(this);

        startdate =(TextView)findViewById(R.id.startlblDate);
        enddate = (TextView) findViewById(R.id.endlblDate);
        starttime = (TextView) findViewById(R.id.starttimeLbl);
        endtime = (TextView) findViewById(R.id.endtimeLbl);

        startdate.setText(month_x+"/"+day_x+"/"+year_x);
        enddate.setText(month_x + "/" + day_x + "/" + year_x);

        startdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeDialog = startdate;
                showDialog(dateDialog);
            }
        });

        enddate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeDialog = enddate;
                showDialog(dateDialog);
            }
        });

        starttime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeTime = starttime;
                showDialog(timeDialog);
            }
        });
        endtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeTime = endtime;
                showDialog(timeDialog);
            }
        });

    }
    private void updateDisplay(TextView dateDisplay) {
        dateDisplay.setText(month_x+"/"+day_x+"/"+year_x);
    }
    private void updateTime(TextView timeDisplay){
        String newMin;
        newMin = String.valueOf(min_x);
        if (min_x <10)
        {
            newMin = "0"+String.valueOf(min_x);
        }
        timeDisplay.setText(hour_x+":"+newMin+""+am_pm);
    }

    @Override
    protected Dialog onCreateDialog(int id){
        if(id == dateDialog)
            return new DatePickerDialog(this, dpickerListener , year_x, month_x-1, day_x);
        if(id == timeDialog)
            return  new TimePickerDialog(this, tpickerListener, hour_x, min_x, false);
        return null;
    }

    private DatePickerDialog.OnDateSetListener dpickerListener
            =new DatePickerDialog.OnDateSetListener(){
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayofMonth){
            year_x = year;
            month_x = monthOfYear+1;
            day_x = dayofMonth;
            updateDisplay(activeDialog);

        }
    };

    private TimePickerDialog.OnTimeSetListener tpickerListener = new TimePickerDialog.OnTimeSetListener(){
        @Override
        public void onTimeSet(TimePicker view,int hourOfDay, int minute ){
            am_pm = "AM";
            hour_x = hourOfDay;

            if(hourOfDay > 11)
            {
                am_pm = "PM";
                if(hourOfDay == 12)
                {}
                else {
                    hour_x = hourOfDay - 12;
                }
            }
            if(hourOfDay == 0)
            {
                hour_x=hourOfDay+12;
            }

            min_x = minute;

            updateTime(activeTime);
        }
    };


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
        List<String> oldData = new ArrayList<String>();
        int repeatcount =0;
        for(int i=0; i <data.length();i++){
            try{
                    item.add(object.getString(datatype));
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        return item;
    }

    public JSONArray dummyData(){
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
            vehicle.accumulate("vehicle", "sciontc");
            vehicle.accumulate("vehicle", "hasdjklhfsdjhf");
            vehicle.accumulate("services", "lock=true");
            vehicle.accumulate("services", "engine=true");
        }catch (JSONException e){
            e.printStackTrace();
        }

        JSONArray jsonArray = new JSONArray();

        jsonArray.put(users);
        jsonArray.put(vehicle);
        return jsonArray;
    }
}
