package com.jaguarlandrover.auto.remote.vehicleentry;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.gson.Gson;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;


public class KeyRevokeActivity extends ActionBarActivity {
    LinearLayout layout;
    int mPosition;

    Vehicle mSelectedVehicle = new Vehicle();
    User mSnapshotUser = new User();

    ArrayList<User> mFilteredRemotes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_change);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Gson gson = new Gson();

            String vehicleString = (String) extras.get("selectedVehicle");
            mSelectedVehicle = gson.fromJson(vehicleString, Vehicle.class);

            String userString = (String) extras.get("snapShotUser");
            mSnapshotUser = gson.fromJson(userString, User.class);

            for (User guestUser : mSnapshotUser.getGuests()) {
                for (Vehicle vehicle : guestUser.getVehicles()) {
                    if (vehicle.getVehicleId().equals(mSelectedVehicle.getVehicleId()) && vehicle.getUserType().equals("guest")) {
                        User remote = new User(guestUser.getUserName());
                        remote.addVehicle(vehicle);

                        mFilteredRemotes.add(remote);
                    }
                }
            }
        }

        RemoteCredentialsAdapter adapter = new RemoteCredentialsAdapter(this, mFilteredRemotes);
        ListView listView = (ListView) findViewById(R.id.sharedKeys);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPosition = position;
                alertMessage();
            }
        });

        listView.setAdapter(adapter);
        //addUsers(adapter);
    }

    public void alertMessage(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                switch(which){
                    case DialogInterface.BUTTON_POSITIVE:
                        try{
                            ServerNode.revokeAuthorization(selectKey());
                        } catch (Exception e){
                            e.printStackTrace();
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

    public User selectKey() {

        ArrayList<User> guestUsers = ServerNode.getUserData().getGuests();

        return guestUsers.get(mPosition);
    }

//    public void addUsers(RemoteCredentialsAdapter adapter){
//        try {
//
//            ArrayList<User> guestUsers = ServerNode.getUserData().getGuests();
//
//            adapter.addAll(guestUsers);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

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
