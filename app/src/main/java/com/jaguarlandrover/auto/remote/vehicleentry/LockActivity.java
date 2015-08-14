/**
 *  Copyright (C) 2015, Jaguar Land Rover
 *
 *  This program is licensed under the terms and conditions of the
 *  Mozilla Public License, version 2.0.  The full text of the
 *  Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 */

package com.jaguarlandrover.auto.remote.vehicleentry;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class LockActivity extends ActionBarActivity implements LockActivityFragment.LockFragmentButtonListener {
    private static final String TAG = "RVI";
    private boolean bound = false;
    private String username;
    private TextView userHeader;
    //private Messenger service = null;

    private RviService rviService = null;

    LockActivityFragment fragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate() Activity");

        handleExtra(getIntent());

        setContentView(R.layout.activity_lock);
        fragment = (LockActivityFragment) getFragmentManager().findFragmentById(R.id.fragment);
        doBindService();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleExtra(intent);
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
        doUnbindService();

        super.onDestroy();
        //For testing cleanup
        //Intent i = new Intent(this, RviService.class);
        //stopService(i);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lock, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setClass(LockActivity.this, AdvancedPreferenceActivity.class);
            startActivityForResult(intent, 0);
            return true;
        } else if( id == R.id.action_reset) {
            PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply(); //reset
            PreferenceManager.setDefaultValues(this, R.xml.advanced, true);
            return true;
        } else if(id == R.id.action_quit) {
            Intent i = new Intent(this, RviService.class);
            stopService(i);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            //mService = new Messenger(service);

            rviService = ((RviService.RviBinder)service).getService();

            rviService.servicesAvailable().
                    subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(String s) {
                            Log.i(TAG, "X: "+s);
                            fragment.onNewServiceDiscovered(s);
                            //Toast.makeText(LockActivity.this, "X: "+s, Toast.LENGTH_SHORT).show();
                        }
                    });

            // Tell the user about this for our demo.
            Toast.makeText(LockActivity.this, "RVI service connected",
                    Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            rviService = null;
            Toast.makeText(LockActivity.this, "RVI service disconnected",
                    Toast.LENGTH_SHORT).show();
        }
    };

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(new Intent(LockActivity.this,
                RviService.class), mConnection, Context.BIND_AUTO_CREATE);
        bound = true;
    }

    void doUnbindService() {
        if (bound) {
            // Detach our existing connection.
            unbindService(mConnection);
            bound = false;
        }
    }

    @Override
    public void onButtonCommand(String cmd) {
        //TODO send to RVI service
        rviService.service(cmd);
    }

    @Override
    public void keyShareCommand(String key){
        Intent intent = new Intent();
        switch(key) {
            case "keyshare":
            intent.setClass(LockActivity.this, keyShareActivity.class);
            startActivityForResult(intent, 0);
                break;
            case "keychange":
                intent.setClass(LockActivity.this, keyChangeActivity.class);
                startActivityForResult(intent, 0);
                break;
        }
    }
}
