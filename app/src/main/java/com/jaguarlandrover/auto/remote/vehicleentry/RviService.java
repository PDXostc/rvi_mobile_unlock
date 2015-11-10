/**
 *  Copyright (C) 2015, Jaguar Land Rover
 *
 *  This program is licensed under the terms and conditions of the
 *  Mozilla Public License, version 2.0.  The full text of the
 *  Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 */

package com.jaguarlandrover.auto.remote.vehicleentry;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class RviService extends Service /* implements BeaconConsumer */{
    private static final String TAG = "RVI:RVIService";
    private        ConnectivityManager cm;
    private static TelephonyManager    tm;
    private        ConcurrentHashMap<String, Long> visible = new ConcurrentHashMap<String, Long>();
    private static double                          latit   = 0;
    private static double                          longi   = 0;
    private static Location location;
    LocationManager locationManager;
    //private int timeoutSec = 10; //If beacon did not report in 10 sec then remove

    private BluetoothAdapter bluetoothAdapter;

    private Gson gson = new Gson();

    private static boolean connected  = false;
    private static boolean connecting = false;
    private static boolean unlocked   = false;

    //ScheduledExecutorService executorService = null;

    private SharedPreferences prefs;

    //public static final String[] SUPPORTED_SERVICES = new String[1];

    private static final ConcurrentHashMap<String, String> certs = new ConcurrentHashMap<String, String>(1);

    public RviService() {
    }

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class RviBinder extends Binder
    {
        RviService getService() {
            return RviService.this;
        }
    }

    private BeaconRanger br = null;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate Service");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location) {
                latit = location.getLatitude();
                longi = location.getLongitude();
                SharedPreferences.Editor e = prefs.edit();
                e.putString("moving", "true");
                e.commit();
                String myLocation = "Latitude = " + latit + " Longitude = " + longi;

                //I make a log to see the results
                Log.e("MY CURRENT LOCATION", myLocation);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60, 1, locationListener);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //TODO base on VIN instead

        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //SUPPORTED_SERVICES[0] = "jlr.com/mobile/"+ tm.getDeviceId()+"/control/status";


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }


        br = new BeaconRanger(this);
        //br.addVinMask("3C3CFFGE8ET291409"); //My Fiat
        //br.addVinMask("3C3CFFGE8ET"); //Lots of fiats
        //br.addVinMask("3"); //Lots of cars
        br.start();
        Observable<RangeObject> obs =  br.getRangeStream();
        obs.observeOn(Schedulers.newThread()).subscribeOn(Schedulers.newThread()).subscribe(beaconSubscriber);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy() Service");

        br.stop();
//        if(executorService != null) executorService.shutdown();
    }

    private PublishSubject<String> commandSink = PublishSubject.create();

    public Observable<String> servicesAvailable() {
//        String[] i = {"Login","Connect"};
//        return Observable.from(i);

        return commandSink;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");

        connectServerNode();

        return mBinder;
    }

    private void connectServerNode() {
        ServerNode.connect();
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new RviBinder();

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        connectServerNode();
        starting(intent);
        return START_STICKY;
    }

    protected void starting(Intent intent) {
//        if( intent != null && intent.getExtras() != null ) {
//            timeoutSec = intent.getExtras().getInt("timeoutSec", 10);
//        } else {
//            timeoutSec = Integer.parseInt(prefs.getString("pref_beacon_time",""+timeoutSec));
//            Log.w(TAG,"intent = "+intent);
//            if(intent != null) Log.w(TAG,"extras = "+intent.getExtras());
//        }
        if( intent != null && intent.getExtras() != null ) {
            int btState = intent.getExtras().getInt("bluetooth", BluetoothAdapter.STATE_OFF);
            if( btState == BluetoothAdapter.STATE_ON ) {
                Log.w(TAG, "BT on, start ranging");
                br.start();
            } else if( btState == BluetoothAdapter.STATE_OFF ) {
                Log.w(TAG, "BT off, stop ranging");
                br.stop();
            }

        }
    }

    private static final PublishSubject<JSONObject> btSender = PublishSubject.create();

    private Subscriber<RangeObject> beaconSubscriber = new Subscriber<RangeObject>()
    {
        @Override
        public void onCompleted() {
            Log.i(TAG, "Beacon Ranger DONE");
        }

        @Override
        public void onError(Throwable e) {
            Log.e(TAG, "", e);
        }

        @Override
        public void onNext(final RangeObject ro) {
            //Log.w(TAG, "Beacon Ranger Object : " + ro);

            final double unlockDistance = Double.parseDouble(prefs.getString("pref_auto_unlock_dist", "1"));
            final double connectDistance = Double.parseDouble(prefs.getString("pref_auto_conn_dist", "3"));
            double grayArea = Double.parseDouble(prefs.getString("pref_auto_lock_unlock_cutoff_gray_area", "0.4"));

            if (grayArea > 1.0 || grayArea < 0.0) {
                Log.d(TAG, "Invalid grayArea: " + grayArea + "! Resetting to default value of 0.4");
                grayArea = 0.4;
            }

            final double weightedCutoff = ((1.0 - grayArea) / 2.0);

            Log.d(TAG, "distance:" + ro.distance + ", weightedDistance:" + ro.weightedDistance + ", unlockDistance:" + unlockDistance + ", connectDistance:" + connectDistance);
            Log.d(TAG, "connected:" + connected + ", connecting:" + connecting + ", unlocked:" + unlocked);

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            UserCredentials userCredentials = ServerNode.getUserCredentials();
            if (userCredentials != null) {
                try {
                    if (userCredentials.getUserType().equals("guest") && (userCredentials.getAuthorizedServices().isLock() || !userCredentials.isKeyValid())) { // TODO: Test
                        Log.d(TAG, "User is not authorized to lock/unlock car.");

                        return;
                    } else {
                        // No longer using auto lock/unlock for demo, commenting out
                        //if(prefs.getString("autounlock", "nothing").equals("true")){
                        if (!connected && (ro.distance > connectDistance)) {
                            Log.d(TAG, "Too far out to connect : " + ro.distance);
                            return;
                        }

                        if (connected && (!unlocked) && (ro.weightedDistance > weightedCutoff)) {
                            Log.d(TAG, "Too far out unlock : " + ro.distance);
                            return;
                        }

                        if (connected && (!unlocked) && ro.weightedDistance <= weightedCutoff) {
                            unlocked = true;
                            //changing back to normal unlock instead of auto_*
                            RviService.service("unlock", RviService.this);
                            sendNotification(RviService.this, getResources().getString(R.string.not_auto_unlock));
                            return;
                        }

                        if (connected && unlocked && ro.weightedDistance >= (1.0 - weightedCutoff)) {
                            unlocked = false;
                            //changing back to normal lock instead of auto_*
                            RviService.service("lock", RviService.this);
                            sendNotification(RviService.this, getResources().getString(R.string.not_auto_lock));
                            return;
                        }

                        if (connecting) {
                            Log.i(TAG, "Already connecting to BT endpoint.");
                            return;
                        }

                        if (connected) {
                            //br.stop(); //stop reporting
                            return;
                        }
                        SharedPreferences.Editor e = prefs.edit();
                        e.putString("moving", "false");
                        e.commit();
                        //}
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //br.stop();
            //connected = true;
            connecting = true;
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(ro.addr);
            //INIT BT connection
            String[] myServices = {"jlr.com/mobile/" + tm.getDeviceId() + "/control/status"};
            Observable<JSONObject> obs = connectBluetooth(device, RviService.this, btSender);
            obs.subscribeOn(Schedulers.newThread()).observeOn(Schedulers.io()).subscribe(new Subscriber<JSONObject>()
            {
                //obs.subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread()).subscribe(new Subscriber<JSONObject>() {
                @Override
                public void onCompleted() {
                    Log.i(TAG, "Done with BT connection.");
                    br.start(); // Start looking again
                }

                @Override
                public void onError(Throwable e) {
                    Log.e(TAG, "", e);
                    br.start(); // Start looking again
                }

                @Override
                public void onNext(JSONObject s) {
                    if (s.has("cmd")) {
                        try {
                            String cmd = s.getString("cmd");
                            if (cmd != null && cmd.equalsIgnoreCase("sa")) {
                                Log.d(TAG, "sa");
                                if (s.has("svcs")) {
                                    JSONArray services = s.getJSONArray("svcs");
                                    for (int i = 0; i < services.length(); i++) {
                                        commandSink.onNext(services.getString(i));
                                    }
                                }

                                //btSender.onNext(RviProtocol.createReceiveData(1, "jlr.com/bt/stoffe/unlock",new JSONArray("[{\"X\":\"O\"}]"), "", ""));

                                //Start a PING thread
                                if (false) {
                                    //if( prefs.getBoolean("pref_fire_bt_ping",true) ) {
                                    //Allocate ping thread
                                    long freq = Long.parseLong(prefs.getString("pref_bt_ping_time", "1000"));
                                    Subscription subscription = Observable.timer(freq, freq, TimeUnit.MILLISECONDS).subscribe(new Action1<Long>()
                                    {
                                        @Override
                                        public void call(Long l) {
                                            Log.d(TAG, "Ping :" + l);
                                            service("ping", RviService.this);
                                        }
                                    });
                                }


                            } else if (cmd != null && cmd.equalsIgnoreCase("ping")) {
                                Log.d(TAG, "ping");
                            }
                        } catch (JSONException e) {
                            Log.d(TAG, "JSON EXception on BT parsing.");
                        }
                    }

                }
            });
        }

    };

    public static Observable<JSONObject> connectBluetooth(final BluetoothDevice device, final Context ctx, final Observable<JSONObject> sender) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        Log.i(TAG, "Connecting to Bluetooth!" + Thread.currentThread().getName());
        Observable<JSONObject> myObservable = Observable.create(
                new Observable.OnSubscribe<JSONObject>()
                {
                    BluetoothSocket sock = null;

                    @Override
                    public void call(Subscriber<? super JSONObject> sub) {
                        if (connected == true) return;

                        connected = false;
                        unlocked = false;

                        //Config
                        int btChannel = Integer.parseInt(prefs.getString("pref_bt_channel", "1"));

                        // Commented out as part of BT reset connection issue. See,
                        // https://github.com/PDXostc/rvi_mobile_unlock/issues/4
                        /*
                        try {
                            sock = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB".toLowerCase())); //SerialPortServiceClass
                            //00001105-0000-1000-8000-00805f9b34fb //OBEXObjectPushServiceClass
                            //00001106-0000-1000-8000-00805f9b34fb //OBEXFileTransferServiceClass
                            //0000112d-0000-1000-8000-00805f9b34fb //SIMAccessServiceClass
                            //0000110e-0000-1000-8000-00805f9b34fb //AVRemoteControlServiceClass
                            //00001112-0000-1000-8000-00805f9b34fb //HeadsetAudioGatewayServiceClass
                            //0000111f-0000-1000-8000-00805f9b34fb //HandsfreeAudioGatewayServiceClass
                            //00000000-0000-1000-8000-00805f9b34fb
                            //BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                            //device.createBond();
                            //Log.i(TAG,"3 "+socket+" : "device.createBond()");
                            sock.connect();
                        } catch (IOException e) {

                            Log.d(TAG, "Excepption:" + e.getLocalizedMessage());
*/
                        try {
                            Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                            sock = (BluetoothSocket) m.invoke(device, btChannel);

                            // Added as part of BT reset connection issue. See,
                            // https://github.com/PDXostc/rvi_mobile_unlock/issues/4
                            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

                            sock.connect();
                        } catch (InvocationTargetException ite) {
                            ite.printStackTrace();
                        } catch (NoSuchMethodException ne) {
                            ne.printStackTrace();
                        } catch (IllegalAccessException ie) {
                            ie.printStackTrace();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        Subscription sendersub = null;
                        try {
                            Log.i(TAG, "Sending to socket auth" + sock);

                            //notifyWatch("AutoUnlock");
                            //Cert if in
                            final String cert = (certs.size() > 0) ? certs.values().iterator().next() : "";
                            JSONObject auth = RviProtocol.createAuth(1, device.getAddress(), 1, cert, "");
                            final OutputStream out = sock.getOutputStream();

                            // Added as part of BT reset connection issue. See,
                            // https://github.com/PDXostc/rvi_mobile_unlock/issues/4
                            SystemClock.sleep(500);
                            Log.i(TAG, "BT reset - getState()" + BluetoothAdapter.getDefaultAdapter().getState());
                            Log.i(TAG, "BT reset - isConnected()" + sock.isConnected());
                            //out.write(auth.toString().getBytes());

                            out.flush();

                            connected = true;
                            connecting = false;

                            sendNotification(ctx, ctx.getResources().getString(R.string.not_connected_car));

                            //If first passed meens socket is up, subscribe and let the flow start
                            sendersub = sender.subscribeOn(Schedulers.io()).subscribe(new Action1<JSONObject>()
                            {
                                @Override
                                public void call(JSONObject s) {
                                    try {
                                        out.write(s.toString().getBytes());
                                        out.flush();
                                    } catch (IOException ioe) {
                                        //sub.onError(ioe);
                                        Log.e(TAG, "", ioe);
                                    }
                                }
                            });

                            boolean running = true;
                            BufferedInputStream input = new BufferedInputStream(sock.getInputStream());

                            int cnt = 0;
                            while (running) {

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                Log.i(TAG, "Sent to socket - ready : " + input.available());
                                int open = input.read();
                                if (open != '{') {
                                    running = false;
                                    Log.i(TAG, "First char did not match : " + open);
                                    break;
                                }
                                cnt++;
                                baos.write(open); //Wait for the first then go
                                Log.i(TAG, "Sent to socket - ready : " + input.available() + " open = " + open);

                                while (input.available() > 0) {
                                    try {
                                        int tmp = input.read();
                                        baos.write(tmp);
                                        if (tmp == '{') cnt++;
                                        else if (tmp == '}') {
                                            cnt--;
                                            if (cnt == 0) { //done
                                                baos.flush();
                                                String toparse = baos.toString();
                                                baos.reset();
                                                //Log.d(TAG, "Pre Obj: "+toparse);
                                                JSONObject inObj = new JSONObject(toparse);
                                                Log.d(TAG, "Done Obj: " + inObj.toString(2));
                                                sub.onNext(inObj);
                                            }
                                        }
                                        Log.i(TAG, "Sent to socket - ready loop : " + input.available());
                                    } catch (IOException ioe) {
                                        ioe.printStackTrace();
                                    }
                                }
                            }
                            sock.close();

                        } catch (IOException ioe) {
                            Log.e(TAG, "Resetting .....");
                            connected = false;
                            connecting = false;
                            unlocked = false;
                            sock = null;
                            sub.onError(ioe);
                        } catch (JSONException e1) {
                            sub.onError(e1);
                        } finally {
                            connected = false;
                            connecting = false;
                            unlocked = false;
                            sock = null;
                            sub.onCompleted();
                            if (sendersub != null) sendersub.unsubscribe();
                        }

                        //device.
                        Log.i(TAG, "Baddr = " + device.getAddress());
                    }
                    //return;
                }
                //}
        );

        return myObservable;
    }

    static void sendNotification(Context ctx, String action, String ... extras) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        boolean fire = prefs.getBoolean("pref_fire_notifications",true);

        if(!fire) return;

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(ctx)
                        .setSmallIcon(R.drawable.rvi_not)
                        .setAutoCancel(true)
                        .setContentTitle(ctx.getResources().getString(R.string.app_name))
                        .setContentText(action);

        Intent targetIntent = new Intent(ctx, LockActivity.class);
        int j = 0;
        for(String ex : extras) {
            targetIntent.putExtra("_extra"+(++j), ex);
            targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        nm.notify(0, builder.build());
    }

    public static void service(String service, Context ctx) {
        Log.i(TAG, "Invoking service : "+service+" the car, conn = " + btSender);

        UserCredentials userCredentials = ServerNode.getUserCredentials();

        //final String cert = (certs.size() > 0)?certs.values().iterator().next():"";
        final String cert = "";
        if (userCredentials != null && btSender != null && btSender.hasObservers()) {
            Log.i(TAG, "Invoking service : " + service + " on car, we have a BT socket");
            try {
                JSONArray locationData = new JSONArray();
                JSONObject location = new JSONObject();
                location.put("username", userCredentials.getUserName());
                location.put("vehicleVIN", userCredentials.getVehicleVin());
                location.put("latitude", latit);
                location.put("longitude", longi);
                locationData.put(location);
                JSONObject rcv = RviProtocol.createReceiveData(2, "jlr.com/vin/stoffe/" + service,
                locationData, cert, "");
                /*
                try{
                    Thread.sleep(100);
                }catch(InterruptedException e){
                    System.out.println("got interrupted!");
                }
                */
                btSender.onNext(rcv);
            } catch (JSONException e) {
                btSender.onError(e);
            }

        }
    }
}
