package com.jaguarlandrover.auto.remote.vehicleentry;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class keyShareActivity extends ActionBarActivity {

    int year_x, month_x, day_x, hour_x, min_x;
    String am_pm;
    TextView startdate, enddate, starttime, endtime;
    static final int dateDialog = 0;
    static final int timeDialog=1;
    private TextView activeDialog;
    private TextView activeTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_share);
        showDialog();
        Spinner userdropdown = (Spinner)findViewById(R.id.spinner1);
        String[] users = new String[]{"SelectUser", "dthiriez", "arodriguez"};
        ArrayAdapter<String> useradapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, users);
        userdropdown.setAdapter(useradapter);

        Spinner cardropdown = (Spinner)findViewById(R.id.spinner2);
        String[] vehicles = new String[]{"Select Vehicle", "Vehicle1", "Vehicle2"};
        ArrayAdapter<String> caradapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, vehicles);
        cardropdown.setAdapter(caradapter);
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
            return new DatePickerDialog(this, dpickerListener , year_x, month_x, day_x);
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
}
