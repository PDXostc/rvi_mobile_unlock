package com.jaguarlandrover.auto.remote.vehicleentry;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.Spinner;
import android.widget.Toast;

public class keyShareActivity extends ActionBarActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_share);

        Spinner userdropdown = (Spinner)findViewById(R.id.spinner1);
        String[] users = new String[]{"SelectUser", "dthiriez", "arodriguez"};
        ArrayAdapter<String> useradapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, users);
        userdropdown.setAdapter(useradapter);

        Spinner cardropdown = (Spinner)findViewById(R.id.spinner2);
        String[] vehicles = new String[]{"Select Vehicle", "Vehicle1", "Vehicle2"};
        ArrayAdapter<String> caradapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, vehicles);
        cardropdown.setAdapter(caradapter);
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
}
