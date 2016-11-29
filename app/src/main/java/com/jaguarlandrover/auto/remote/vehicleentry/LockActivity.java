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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.genivi.pki.PKIManager;
import com.jaguarlandrover.rvi.RVILocalNode;


public class LockActivity extends ActionBarActivity implements LockActivityFragment.LockFragmentButtonListener, ServerNode.Listener, VehicleNode.Listener {
    private static final String TAG = "UnlockDemo/LockActivity";

    private Handler userDataCheckerHandler;
    private Handler guestActivityCheckerHandler;
    LockActivityFragment mLockActivityFragment = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate() Activity");

        handleExtra(getIntent());

        userDataCheckerHandler = new Handler();
        guestActivityCheckerHandler = new Handler();

        setContentView(R.layout.activity_lock);
        mLockActivityFragment = (LockActivityFragment) getFragmentManager().findFragmentById(R.id.fragmentlock);

        startRepeatingTasks();

        ServerNode.addListener(this);
        VehicleNode.addListener(this);
    }

    Runnable userDataCheckerRunnable = new Runnable()
    {
        @Override
        public void run() {
            try {
                checkForUserData();
            } catch (Exception e) {
                e.printStackTrace();
            }

            userDataCheckerHandler.postDelayed(userDataCheckerRunnable, 3 * 1000);
        }
    };

    Runnable guestActivityCheckerRunnable = new Runnable()
    {
        @Override
        public void run() {
            try {
                checkForGuestActivity();
            } catch (Exception e) {
                e.printStackTrace();
            }

            guestActivityCheckerHandler.postDelayed(guestActivityCheckerRunnable, 10 * 1000);
        }
    };

    void startRepeatingTasks() {
        userDataCheckerRunnable.run();
        guestActivityCheckerRunnable.run();
    }

    private void stopRepeatingTasks() {
        userDataCheckerHandler.removeCallbacks(userDataCheckerRunnable);
        guestActivityCheckerHandler.removeCallbacks(guestActivityCheckerRunnable);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleExtra(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void handleExtra(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null && extras.size() > 0) {
            for (String k : extras.keySet()) {
                Log.i(TAG, "k = " + k + " : " + extras.getString(k));
            }
        }
        if (extras != null && "dialog".equals(extras.get("_extra1"))) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("" + extras.get("_extra2"));
            alertDialogBuilder
                    .setMessage("" + extras.get("_extra3"))
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener()
                    {
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

        stopRepeatingTasks();

        ServerNode.removeListener(this);
        VehicleNode.removeListener(this);

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lock, menu);

        return true;
    }

    void confirmationAlert(final int id, String actionMessage) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        if (id == R.id.action_delete_all_keys_certs) {

                            ServerNode.disconnect();

                            PKIManager.deleteAllKeysAndCerts(LockActivity.this);
                            RVILocalNode.removeAllCredentials(LockActivity.this);
                            ServerNode.deleteUserData();

                            Intent intent = new Intent();
                            intent.setClass(LockActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();

                        } else if (id == R.id.action_delete_server_certs) {

                            ServerNode.disconnect();

                            PKIManager.deleteServerCerts(LockActivity.this);
                            RVILocalNode.removeAllCredentials(LockActivity.this);

                            Intent intent = new Intent();
                            intent.setClass(LockActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();

                        } else if (id == R.id.action_restore_defaults) {

                            ServerNode.disconnect();

                            PreferenceManager.getDefaultSharedPreferences(LockActivity.this).edit().clear().apply();
                            PreferenceManager.setDefaultValues(LockActivity.this, R.xml.advanced, true);

                            ServerNode.connect();

                        } else if (id == R.id.action_quit) {
                            Intent i = new Intent(LockActivity.this, BluetoothRangingService.class);
                            stopService(i);
                            finish();

                        }

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:

                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to " + actionMessage + "?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("Cancel", dialogClickListener).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setClass(LockActivity.this, AdvancedPreferenceActivity.class);
            startActivityForResult(intent, 0);

            return true;

        } else if (id == R.id.action_delete_all_keys_certs) {
            confirmationAlert(id, "delete all your keys and certificates");

        } else if (id == R.id.action_delete_server_certs) {
            confirmationAlert(id, "delete the server certificates");

        } else if (id == R.id.action_restore_defaults) {
            confirmationAlert(id, "reset all the settings");

        } else if (id == R.id.action_quit) {
            confirmationAlert(id, "quit");

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onButtonCommand(String cmd) {
        VehicleNode.sendFobSignal(cmd);
    }

    public void keyUpdate(final User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LockActivity.this);
        builder.setInverseBackgroundForced(true);
        builder.setMessage("Key updates have been made").setCancelable(false).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mLockActivityFragment.updateUserInterface();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void notifyGuestUsedKey(final String guestUser, final String guestService) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LockActivity.this);
        builder.setInverseBackgroundForced(true);
        builder.setMessage("Remote key used by "+ guestUser + "!")
                .setCancelable(true)
                .setPositiveButton("OK", new
                        DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void keyShareCommand(String key) {
        Intent intent = new Intent();

        User user = ServerNode.getUserData();
        Integer selectedVehicleIndex = user.getSelectedVehicleIndex();

        if (selectedVehicleIndex == -1) {
            return; // TODO: error #amm
        }

        String vehicleString = user.getVehicles().get(selectedVehicleIndex).toString();
        String userString = user.toString();

        intent.putExtra("selectedVehicle", vehicleString);
        intent.putExtra("snapshotUser", userString);

        switch (key) {
            case "key_share":
                intent.setClass(LockActivity.this, KeyShareActivity.class);

                break;

            case "key_revoke":
                intent.setClass(LockActivity.this, KeyRevokeActivity.class);

                break;
        }

        startActivityForResult(intent, 0);
    }

    public void checkForUserData() {
        User userData = ServerNode.getUserData();

        if (userData != null && ServerNode.thereIsNewUserData()) {
            keyUpdate(userData);
            ServerNode.setThereIsNewUserData(false);
        }
    }

    public void checkForGuestActivity() {
        InvokedServiceReport report = ServerNode.getInvokedServiceReport();

        if (report != null && ServerNode.thereIsNewInvokedServiceReport()) {
            notifyGuestUsedKey(report.getUserName(), report.getServiceIdentifier());
            ServerNode.setThereIsNewInvokedServiceReport(false);
        }
    }

    @Override
    public void serverNodeDidConnect() {
        mLockActivityFragment.setServerConnected(true);
    }

    @Override
    public void serverNodeDidDisconnect() {
        mLockActivityFragment.setServerConnected(false);
    }

    @Override
    public void vehicleNodeDidConnect() {
        mLockActivityFragment.setVehicleConnected(true);

    }

    @Override
    public void vehicleNodeDidDisconnect() {
        mLockActivityFragment.setVehicleConnected(false);

    }
}
