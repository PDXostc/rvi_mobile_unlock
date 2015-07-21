/**
 *  Copyright (C) 2015, Jaguar Land Rover
 *
 *  This program is licensed under the terms and conditions of the
 *  Mozilla Public License, version 2.0.  The full text of the
 *  Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 */

package com.jaguarlandrover.auto.remote.vehicleentry;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.google.common.collect.EvictingQueue;

import org.altbeacon.beacon.distance.CurveFittedDistanceCalculator;
import org.json.JSONObject;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscriber;

public class BeaconRanger {
    final BluetoothAdapter adapter;
    private boolean started = false;
    private Subscriber<? super RangeObject> sub;
    private EvictingQueue<Double> ranges = null;
    private Long lastdist = -1L;
    private SharedPreferences prefs;

    private ArrayList<String> vinMasks = new ArrayList<String>();

    //private static final int MEASURMENTS = 20;
    private static final int HITS_BEFORE_FIRE = 5;
    private static final int RANGE_IN_METERS = 3;
    private int numberMesurments = 10;

    private CurveFittedDistanceCalculator calc;

    public BeaconRanger(Context ctx) {
        final BluetoothManager bluetoothManager = (BluetoothManager) ctx.getSystemService(Context.BLUETOOTH_SERVICE);
        adapter = bluetoothManager.getAdapter();

        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        numberMesurments = Integer.parseInt(prefs.getString("pref_ranging_sample_buffer", "10"));
        ranges = EvictingQueue.create(numberMesurments);
        calc = new CurveFittedDistanceCalculator(0.42093, 6.9476, 0.54992);
    }

    BluetoothAdapter.LeScanCallback cb = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            //System.out.println("RVI Rec = " + bytesToHex(scanRecord));
                //Fiat VIN 3343334346464745384554323931343039

            //02 01 06 1a   ff 4c 00 02 1-8
            //15 00 00 00   00 77 77 55 9-16
            //55 33 33 00   00 00 00 00 17-24
            //00 ff ff ff   ff c2 00 00 25-32
            //00 00 00 00   00 00 00 00
            //00 00 00 00   00 00 00 00
            //00 00 00 00   00 00 00 00
            //00 00 00 00   00 00

            //setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

            if( started && sub != null ) {
                //check its correct iBeacon ...
                if( scanRecord[0] != 0x02 ||  scanRecord[1] != 0x01 || scanRecord[3] != 0x1A) {
                    //System.out.println("RVI not counting");
                    return;
                }
                //Check vin [9]-[24] & [25] use the top byte for the last digit.
                for(String s : vinMasks) {
                    byte[] b = s.getBytes();
                    for(int i = 0 ; i < b.length ; i++) {
                        System.out.println("RVI index "+i+" "+b[i]+" : "+scanRecord[i+9]);
                        if( b[i] != scanRecord[i+9] ) { //Not matching
                            System.out.println("RVI dropping out on index "+i+" "+b[i]+" != "+scanRecord[i+9]);
                            return;
                        }
                    }
                }

                double mean = 0;
                //double newReading2 = getDistance(rssi, scanRecord[29]);
                double newReading = calc.calculateDistance(scanRecord[29],rssi);
                //System.out.println("RVI New reading dist : "+newReading+" : "+newReading2);
                synchronized (ranges) {
                    ranges.add(newReading);
                    if(ranges.size() == numberMesurments) { //Full
                        for (double d : ranges) {
                            mean += d;
                            //System.out.println("RVI Loop mean : "+mean);
                        }
                        mean = mean / numberMesurments;
                    } else {
                        //System.out.println("RVI Not yet full : "+ranges.size());
                    }

                }

                if( mean > 0 ) {
                    Long latest = Math.round(mean);
                    if( latest != lastdist ) {
                        System.out.println("RVI New dist "+ latest);
                        lastdist = latest;
                    }

                    RangeObject ro = new RangeObject();
                    ro.id = new String(scanRecord, 9, 26 );
                    ro.addr = device.getAddress();
                    ro.rssi = rssi;
                    ro.txPower = scanRecord[29];
                    ro.distance = mean;
                    sub.onNext(ro);
                }
            }
        }
    };

//    double getDistance(int rssi, int txPower) {
//    /*
//     * RSSI = TxPower - 10 * n * lg(d)
//     * n = 2 (in free space)
//     *
//     * d = 10 ^ ((TxPower - RSSI) / (10 * n))
//     */
//
//        return Math.pow(10d, ((double) txPower - rssi) / (10 * 2));
//    }

    //0.42093, 6.9476, 0.54992
    double getDistance(int rssi, int txPower) {
    /*
     * RSSI = TxPower - 10 * n * lg(d)
     * n = 2 (in free space)
     *
     * d = 10 ^ ((TxPower - RSSI) / (10 * n))
     */

        return Math.pow(0.42093, ((double) txPower - rssi) / (6.9476) + 0.54992);
    }

    /*
    d6 be 89 8e # Access address for advertising data (this is always the same fixed value)
    40 # Advertising Channel PDU Header byte 0.  Contains: (type = 0), (tx add = 1), (rx add = 0)
    24 # Advertising Channel PDU Header byte 1.  Contains:  (length = total bytes of the advertising payload + 6 bytes for the BLE mac address.)
    05 a2 17 6e 3d 71 # Bluetooth Mac address (note this is a spoofed address)
    02 01 1a 1a ff 4c 00 02 15 e2 c5 6d b5 df fb 48 d2 b0 60 d0 f5 a7 10 96 e0 00 00 00 00 c5 # Bluetooth advertisement
    52 ab 8d 38 a5 # checksum

    02 # Number of bytes that follow in first AD structure
    01 # Flags AD type
    1A # Flags value 0x1A = 000011010
        bit 0 (OFF) LE Limited Discoverable Mode
        bit 1 (ON) LE General Discoverable Mode
        bit 2 (OFF) BR/EDR Not Supported
        bit 3 (ON) Simultaneous LE and BR/EDR to Same Device Capable (controller)
        bit 4 (ON) Simultaneous LE and BR/EDR to Same Device Capable (Host)
    1A # Number of bytes that follow in second (and last) AD structure
    FF # Manufacturer specific data AD type
    4C 00 # Company identifier code (0x004C == Apple)
    02 # Byte 0 of iBeacon advertisement indicator
    15 # Byte 1 of iBeacon advertisement indicator
    e2 c5 6d b5 df fb 48 d2 b0 60 d0 f5 a7 10 96 e0 # iBeacon proximity uuid
    00 00 # major
    00 00 # minor
    c5 # The 2's complement of the calibrated Tx Power

     */

    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    public Observable<RangeObject> getRangeStream() {
        Observable<RangeObject> myObservable = Observable.create(
                new Observable.OnSubscribe<RangeObject>() {

                    @Override
                    public void call(Subscriber<? super RangeObject> sub) {
                        BeaconRanger.this.sub = sub;
                        while( true ) {
                            SystemClock.sleep(1000);
                        }
                    }
                });
        return myObservable;
    }

    public void start() {
        //Deprecated but have to have in API 18
        adapter.startLeScan(cb);
        started = true;
    }

    public void stop() {
        started = false;
        adapter.stopLeScan(cb);
    }

    public String[] getVinMasks() {
        return vinMasks.toArray(new String[0]);
    }

    /**
     * Could be 1-17 Characters VIN white spaces and dashes will be removed.
     * It is totally fine to have first X bytes only to match and pass a short string
     * the string will be matched
     */
    public void addVinMask(String mask) {
        vinMasks.add(mask);
    }

    public void clearVinMasks() {
        vinMasks.clear();
    }
}

class RangeObject {
    String id;
    String addr;
    double distance;
    int rssi;
    int txPower;

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "id:"+id+" addr:"+addr+" dist:"+distance+" rssi:"+rssi+" txPow:"+txPower;
    }
}
