package com.jaguarlandrover.auto.remote.vehicleentry;

import android.app.AlertDialog;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;


public class keyRevokeActivity extends ActionBarActivity {

    LinearLayout layout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_change);

        ArrayList<User> arrayofusers = new ArrayList<User>();
        //layout = (LinearLayout) findViewById(R.id.userlayout);

        UsersAdapter adapter = new UsersAdapter(this, arrayofusers);
        ListView listView = (ListView) findViewById(R.id.sharedKeys);
        listView.setAdapter(adapter);

        addUsers(adapter);
    }

    public void addUsers(UsersAdapter adapter){
        JSONObject user1 = new JSONObject();
        try{
            user1.put("username", "dthiriez");
            user1.put("vehicle", "Vehicle1");
            user1.put("validfrom", "09/01/2015");
            user1.put("validto", "09/15/2015");
            user1.put("lock_unlock", false);
            user1.put("enginestart", true);
        }catch (JSONException e){
            e.printStackTrace();
        }

        JSONObject user2 = new JSONObject();
        try{
            user2.put("username", "arodriguez");
            user2.put("vehicle", "Vehicle1");
            user2.put("validfrom", "08/13/2015 8:00 am");
            user2.put("validto", "09/15/2015  12:00 1pm");
            user2.put("lock_unlock", true);
            user2.put("enginestart", true);
        }catch (JSONException e){
            e.printStackTrace();
        }

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(user1);
        jsonArray.put(user2);
        ArrayList<User> newUsers = User.fromJson(jsonArray);//, layout, this);
        adapter.addAll(newUsers);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_key_change, menu);
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
