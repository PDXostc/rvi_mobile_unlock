package com.ericsson.auto.remote.vehicleentry;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class BluetoothReceiver extends BroadcastReceiver
{
    public static boolean BluetoothConnected;
    private static final String TAG = "STOFFE";

    public void onReceive(Context context, Intent intent)
    {

        Log.d(TAG, "Bluetooth Intent Recieved");

        String action = intent.getAction();
        Log.d(TAG, "Bluetooth Called: Action: " + action);

        if (action.equalsIgnoreCase("android.bluetooth.device.action.ACL_CONNECTED"))
        {
            Log.d(TAG, "BLUETOOTH CONNECTED RECIEVED");
            BluetoothConnected = true;

            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.d(TAG, "Device +"+device.getName()+" uu "+device.getUuids());

            Parcelable[] uuidExtra =intent.getParcelableArrayExtra("android.bluetooth.device.extra.UUID");
            if (BluetoothDevice.ACTION_UUID.equals(action)){
                uuidExtra =intent.getParcelableArrayExtra("android.bluetooth.device.extra.UUID");
            }
            Log.d(TAG,"SDP has no errors: = "+uuidExtra);
            if( uuidExtra == null ) return;
            // if(found==false){
            for(int i=0;i<uuidExtra.length;i++){
                Log.d(TAG,"UUID:   "+ uuidExtra[i]);
                if((uuidExtra[i].toString()).equals("00001101-0000-1000-8000-00805F9B34FB".toLowerCase())){
                    Log.d(TAG,"Match found in loop");

                }

            }
            //device.setPairingConfirmation(true);
        }

        if (action.equalsIgnoreCase("android.bluetooth.device.action.ACL_DISCONNECTED"))
        {
            Log.d(TAG, "BLUETOOTH DISCONNECTED RECIEVED");
            BluetoothConnected = false;
        }
    }
}
