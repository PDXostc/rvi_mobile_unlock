/**
 * Copyright (C) 2015, Jaguar Land Rover
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0.  The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 */

package com.jaguarlandrover.auto.remote.vehicleentry;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;

public class BluetoothReceiver extends BroadcastReceiver
{
    public static boolean BluetoothConnected;
    private static final String TAG = "UnlockDemo/BltoothRecvr";

    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "Bluetooth Intent Recieved");
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String action = intent.getAction();
        Log.d(TAG, "Bluetooth Called: Action: " + action);

        if (action.equalsIgnoreCase("android.bluetooth.device.action.ACL_CONNECTED")) {
            Log.d(TAG, "BLUETOOTH CONNECTED RECIEVED");
            BluetoothConnected = true;

            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.d(TAG, "Device +" + device.getName() + " uu " + device.getUuids());

            Parcelable[] uuidExtra = intent.getParcelableArrayExtra("android.bluetooth.device.extra.UUID");
            if (BluetoothDevice.ACTION_UUID.equals(action)) {
                uuidExtra = intent.getParcelableArrayExtra("android.bluetooth.device.extra.UUID");
            }
            Log.d(TAG, "SDP has no errors: = " + uuidExtra);
            if (uuidExtra == null) return;
            // if(found==false){
            for (int i = 0; i < uuidExtra.length; i++) {
                Log.d(TAG, "UUID:   " + uuidExtra[i]);

                if ((uuidExtra[i].toString()).equals("00001101-0000-1000-8000-00805F9B34FB".toLowerCase())) {
                    Log.d(TAG, "Match found in loop");

                }

            }
            //device.setPairingConfirmation(true);
        }

        if (action.equalsIgnoreCase("android.bluetooth.device.action.ACL_DISCONNECTED")) {
            Log.d(TAG, "BLUETOOTH DISCONNECTED RECIEVED");
            BluetoothConnected = false;
        }

        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR);
            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    Log.d(TAG, "Bluetooth off");
                    Intent i = new Intent(context, BluetoothRangingService.class);
                    i.putExtra("bluetooth", state);
                    context.startService(i);
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    Log.d(TAG, "Turning Bluetooth off");
                    break;
                case BluetoothAdapter.STATE_ON:
                    Log.d(TAG, "Bluetooth on");
                    Intent j = new Intent(context, BluetoothRangingService.class);
                    j.putExtra("bluetooth", state);
                    context.startService(j);
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    Log.d(TAG, "Turning Bluetooth on");
                    break;
            }
        }
    }
}
