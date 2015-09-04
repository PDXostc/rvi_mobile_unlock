package com.jaguarlandrover.auto.remote.vehicleentry;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
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
    SharedPreferences sharedpref;
    int Item ;
    RviService rviService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_change);
        sharedpref = PreferenceManager.getDefaultSharedPreferences(this);
        ArrayList<User> arrayofusers = new ArrayList<User>();
        //layout = (LinearLayout) findViewById(R.id.userlayout);

        UsersAdapter adapter = new UsersAdapter(this, arrayofusers);
        ListView listView = (ListView) findViewById(R.id.sharedKeys);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Item = position;
                alertMessage();
            }
        });
        listView.setAdapter(adapter);
        addUsers(adapter);

    }
    public void alertMessage(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                switch(which){
                    case DialogInterface.BUTTON_POSITIVE:
                        try{
                            rviService.revokeKey(selectKey());//share_fragment.getFormData());
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
                .setPositiveButton("Revoke Key", dialogClickListener)
                .setNegativeButton("Cancel", dialogClickListener).show();
    }

    public JSONArray selectKey(){
        JSONArray revokeKey = new JSONArray();
        try {
            JSONObject jsonObject = new JSONObject(sharedpref.getString("Certificates", "NOTHING here"));
            JSONArray jsonArray = jsonObject.getJSONArray("certificates");
            JSONObject key = jsonArray.getJSONObject(Item);
            JSONObject revoke = new JSONObject();

            JSONObject authServices = new JSONObject();
            authServices.put("lock", "false");
            authServices.put("start", "false");
            authServices.put("trunk", "false");
            authServices.put("windows", "false");
            authServices.put("lights", "false");
            authServices.put("hazard", "false");
            authServices.put("horn", "false");

            revoke.put("username", key.getString("username"));
            revoke.put("ValidTo", "");
            revoke.put("validFrom", "");
            revoke.put("certid", key.getString("certid"));
            revoke.put("authorizedServices", authServices);

            revokeKey.put(revoke);
            Log.d("REVOKE", revokeKey.toString());
        } catch (Exception e){e.printStackTrace();}
        return revokeKey;
    }

    public void addUsers(UsersAdapter adapter){
                try{
                    JSONObject jsonObject=new JSONObject(sharedpref.getString("Certificates","NOTHING here"));
                    JSONArray jsonArray = jsonObject.getJSONArray("certificates");
                    ArrayList<User> newUsers = User.fromJson(jsonArray);//, layout, this);
                    adapter.addAll(newUsers);
                }catch(Exception e){e.printStackTrace();}

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
