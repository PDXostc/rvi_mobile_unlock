/**
 *  Copyright (C) 2015, Jaguar Land Rover
 *
 *  This program is licensed under the terms and conditions of the
 *  Mozilla Public License, version 2.0.  The full text of the
 *  Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 */

package com.jaguarlandrover.auto.remote.vehicleentry;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.nfc.Tag;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.widget.TabHost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class RviService extends Service /* implements BeaconConsumer */{
    private static final String TAG = "RVI";
    private ConnectivityManager cm;
    private static TelephonyManager tm;
    private ConcurrentHashMap<String,Long> visible = new ConcurrentHashMap<String, Long>();
    private static double latit =0;
    private static double longi=0;
    private static Location location;
    LocationManager locationManager;
    //private int timeoutSec = 10; //If beacon did not report in 10 sec then remove

    private BluetoothAdapter bluetoothAdapter;

    private static boolean connected = false;
    private static boolean connecting = false;
    private static boolean unlocked = false;

    //ScheduledExecutorService executorService = null;

    private SharedPreferences prefs;

    //public static final String[] SUPPORTED_SERVICES = new String[1];

    private static final ConcurrentHashMap<String,String> certs = new ConcurrentHashMap<String,String>(1);

    public RviService() {
    }

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class RviBinder extends Binder {
        RviService getService() {
            return RviService.this;
        }
    }

    private BeaconRanger br = null;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate Service");
        locationManager=    (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
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
        /*SharedPreferences.Editor editor = prefs.edit();

        Set<String> userData = prefs.getStringSet("userData", new HashSet<String>());
        Set<String> services = prefs.getStringSet("services", new HashSet<String>());

        userData.add(getResources().getString(R.string.USERNAME));
        userData.add(getResources().getString(R.string.VEHICLE));
        userData.add(getResources().getString(R.string.USERTYPE));

        services.add(getResources().getString(R.string.LOCK));
        services.add(getResources().getString(R.string.ENGINE));

        editor.putStringSet("userData", userData);
        editor.putStringSet("services", services);
        editor.commit();
*/



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

    //private PublishSubject<String> notificationSubject = PublishSubject.create();

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        //connectCloud(this); //Make sure connection is there ...
        _connectConnect();

        return mBinder;
    }
    private static final PublishSubject<JSONObject> cloudSender = PublishSubject.create();
    private void _connectConnect() {
        String rviServer = prefs.getString("pref_rvi_server", "38.129.64.40");//"54.172.25.254");//
        int rviPort = Integer.parseInt(prefs.getString("pref_rvi_server_port","8807"));

        //Create service vector
        final String certProv = "jlr.com/mobile/" + tm.getDeviceId() + "/dm/cert_provision";
        final String certRsp = "jlr.com/mobile/"+tm.getDeviceId()+"/dm/cert_response";
        final String certAccountDetails = "jlr.com/mobile/"+tm.getDeviceId()+"/dm/cert_accountdetails";
        final String serviceInvokedByGuest = "jlr.com/mobile/"+tm.getDeviceId()+"/report/serviceinvokedbyguest";
        final String[] ss = {certProv, certRsp, certAccountDetails, serviceInvokedByGuest};

        //final PublishSubject<JSONObject> cloudSender = PublishSubject.create();

        Observable<JSONObject> obs = connectCloud(ss, rviServer, rviPort , cm, cloudSender);

        obs.subscribeOn(Schedulers.io()).subscribe(new Subscriber<JSONObject>() {

            @Override
            public void onCompleted() {
                Log.e(TAG, "Cloud connection DONE");
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "", e);
            }

            @Override
            public void onNext(JSONObject s) {
                //Log.i(TAG, "Received from Cloud JSON: " + s.toString());
                if (!s.has("cmd")) {
                    Log.w(TAG, "CMD is missing!");
                    return; //Very strange
                }

                try {
                    String cmd = s.getString("cmd");
                    cmd.toLowerCase().trim();
                    //TODO here add Cert validation!

                    if ("rcv".equals(cmd)) {
                        if (!s.has("data")) {
                            Log.w(TAG, "DATA is missing!");
                            return; //Very strange
                        }
                        JSONObject data = RviProtocol.parseData(s.getString("data"));
                        String servicePtr = data.getString("service");
                        //ADD SERVICE HERE----*************

                        Log.i(TAG, "Received Service : " + servicePtr);
                        //Log.i(TAG, "Received Service : " + data);
                        //CERT SERVICE
                        if (servicePtr.equals(ss[0])) {
                            JSONArray params = data.getJSONArray("parameters");
                            Log.i(TAG, "Received Cert Params : " + params);
                            JSONObject p1 = params.getJSONObject(0);
                            JSONObject p2 = params.getJSONObject(1);
                            String certId = p1.getString("certid");
                            String jwt = p2.getString("certificate");
                            Log.i(TAG, "Received from Cloud Cert ID: " + certId);
                            Log.i(TAG, "JWT = " + jwt);

                            certs.put(certId, jwt);
                            //Debug
                            // Errors seen here on parseAndValidateJWT. Should be getting Base64
                            // from backend, but sometimes getting errors that it's not.
                            // Should be fixed now, backend is sending URL safe Base64,
                            // parseAndValidateJWT now using Base64.URL_SAFE
                            String[] token = RviProtocol.parseAndValidateJWT(jwt);
                            JSONObject key = new JSONObject(token[1]);
                            Log.d(TAG, "Token = " + key.toString(2));
                            sendNotification(RviService.this, getResources().getString(R.string.not_new_key) + " : " + key.getString("id"),
                                    "dialog", "New Key", key.getString("id"));

                        } else if (servicePtr.equals(ss[1])) {
                            String params = data.getString("parameters");
                            Log.i(TAG, "Received from Cloud Cert: " + params);

                            SharedPreferences.Editor e = prefs.edit();
                            e.putString("Certificates", params);
                            e.putString("newKeyList", "true");
                            e.apply();

                        } else if (servicePtr.equals(ss[2])) {
                            JSONArray params = data.getJSONArray("parameters");
                            JSONObject p1 = params.getJSONObject(0);
                            Log.i(TAG, "User Data:" + p1);

                            SharedPreferences.Editor e = prefs.edit();
                            e.putString("Userdata", p1.toString());
                            e.putString("newdata", "true");
                            e.commit();

                        } else if (servicePtr.equals(ss[3])) {
                            JSONArray params = data.getJSONArray("parameters");
                            JSONObject p1 = params.getJSONObject(0);
                            Log.i(TAG, "Service Invoked by Guest:" + p1);

                            SharedPreferences.Editor e = prefs.edit();
                            e.putString("guestInvokedService", p1.toString());
                            e.putString("newguestactivity", "true");
                            e.commit();

                        }

                    } else if ("sa".equals(cmd)) {
                        if (!s.has("svcs")) {
                            Log.w(TAG, "SERVICES is missing!");
                            return; //Very strange
                        }
                        JSONArray svcs = s.getJSONArray("svcs");
                        String[] services = new String[svcs.length()];
                        for (int i = 0; i < services.length; i++) {
                            services[i] = svcs.getString(i);
                        }
                        //Just print
                        for (String s1 : services) {
                            Log.d(TAG, "Found service : " + s1);
                        }


                    } else if ("au".equals(cmd)) {
                        String addr = s.getString("addr");
                        int port = s.getInt("port");
                        Log.d(TAG, "Authentication from server " + addr + " : " + port);

                        JSONObject saData = RviProtocol.createServiceAnnouncement(
                                1, ss, "av", "", "");
                        cloudSender.onNext(saData);


                    } else if ("ping".equals(cmd)) { //NOOP
                    } else {
                        Log.w(TAG, "Unknown command received - " + cmd);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "", e);
                }
            }
        });
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new RviBinder();

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        //connectCloud(this);
        _connectConnect();
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

    private boolean isKeyValid() throws ParseException {
        String[] dateTime;

        try {
            dateTime = JSONParser(prefs.getString("Userdata", "There's nothing"), "validTo").split("T");
        }
        catch (Exception e) {
            return false;
        }

        String userDate = dateTime[0];
        String userTime = dateTime[1];

        String userDateTime = userDate + " " + userTime;

        SimpleDateFormat formatter1;

        try {
            formatter1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        } catch (Exception e) {
            return false;
        }

        formatter1.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date savedDate = formatter1.parse(userDateTime);
        Date dateNow = new Date();

        return savedDate.compareTo(dateNow) > 0;
    }


    private Subscriber<RangeObject> beaconSubscriber = new Subscriber<RangeObject>(){
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
            final double lockDistance = Double.parseDouble(prefs.getString("pref_auto_lock_dist", "10"));

            Log.d(TAG,"distance:"+ro.distance+", lockDistance:"+lockDistance+", unlockDistance:"+unlockDistance+", connectDistance:"+connectDistance);
            Log.d(TAG,"connected:"+connected+", connecting:"+connecting+", unlocked:"+unlocked);

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String userType = JSONParser(prefs.getString("Userdata", "Nothing there!!"), "userType");
            String showme = JSONParser(prefs.getString("Userdata", "Nothing There!!"), "authorizedServices");
            JSONObject services = null;
            try{
                services = new JSONObject(showme);
                if (userType.equals("guest") && (services.getString("lock").equals("false") || !isKeyValid())) {
                    Log.d(TAG, "User is not authorized to lock/unlock car.");
                } else {
                    // No longer using auto lock/unlock for demo, commenting out
                    //if(prefs.getString("autounlock", "nothing").equals("true")){
                        if (!connected && (ro.distance > connectDistance)) {
                            Log.d(TAG, "Too far out to connect : " + ro.distance);
                            return;
                        }

                        if (connected && (!unlocked) && (ro.weightedDistance > 0.5/*unlockDistance*/)) {
                            Log.d(TAG, "Too far out unlock : " + ro.distance);
                            return;
                        }

                        if (connected && (!unlocked) && ro.weightedDistance <= 0.5/*unlockDistance*/) {
                            unlocked = true;
                            //changing back to normal unlock instead of auto_*
                            RviService.service("unlock", RviService.this);
                            sendNotification(RviService.this, getResources().getString(R.string.not_auto_unlock));
                            return;
                        }

                        if (connected && unlocked && ro.weightedDistance >= 0.5/*lockDistance*/) {
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
                        e.putString("moving","false");
                        e.commit();
                    //}
                }
            }catch(Exception e){e.printStackTrace();}

            //br.stop();
            //connected = true;
            connecting = true;
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(ro.addr);
            //INIT BT connection
            String[] myServices = {"jlr.com/mobile/"+ tm.getDeviceId()+"/control/status"};
            Observable<JSONObject> obs = connectBluetooth(device, RviService.this, btSender );
            obs.subscribeOn(Schedulers.newThread()).observeOn(Schedulers.io()).subscribe(new Subscriber<JSONObject>() {
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
                                    Subscription subscription = Observable.timer(freq, freq, TimeUnit.MILLISECONDS).subscribe(new Action1<Long>() {
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


    private void initBeacon() {
//        executorService = Executors.newSingleThreadScheduledExecutor();
//        executorService.scheduleAtFixedRate(new Runnable() {
//            @Override
//            public void run() {
//                //Check for timeout in list ...
//                ArrayList<String> toDelete = new ArrayList<String>();
//                for(Map.Entry<String,Long> entry:visible.entrySet()) {
//                    long delta = System.currentTimeMillis() - entry.getValue();
//                    int deltaSec = (int) (delta/1000);
//                    Log.d(TAG,"Delta = "+deltaSec+" for id = "+entry.getKey());
//                    if( deltaSec > timeoutSec ) {
//                        String id = entry.getKey();
//                        detector.reportLostBeacon(id);
//                        toDelete.add(id);
//                    }
//                }
//                //Log.d(TAG,"Scan # : "+visible.size()+" deleting : "+toDelete.size());
//                for(String del:toDelete) {
//                    visible.remove(del);
//                }
//            }
//        }, 1, 1, TimeUnit.SECONDS);

        //beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        //beaconManager.bind(this);
    }

    /*
                                    //Pause discovery
                                if(beaconManager != null && region != null) {
                                    try {
                                        beaconManager.stopRangingBeaconsInRegion(region);
                                    } catch (RemoteException re) {
                                        Log.w(TAG,"",e);
                                    }
                                }

                                    //Clean up and in flight
                                if(executorService != null) executorService.shutdown();
                                executorService = Executors.newSingleThreadScheduledExecutor();


                                Notification n = BeaconDetector.creteNotification(RviService.this,
                                        getResources().getString(R.string.not_connected_car));
                                if( n != null ) notificationManager.notify(0,n);

                                FINAL
                                                                //Pause discovery
                                if(beaconManager != null && region != null) {
                                    try {
                                        beaconManager.startRangingBeaconsInRegion(region);
                                    } catch (RemoteException re) {
                                        Log.w(TAG,"",e);
                                    }
                                }

     */


    public static Observable<JSONObject> connectBluetooth(final BluetoothDevice device, final Context ctx, final Observable<JSONObject> sender) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        Log.i(TAG, "Connecting to Bluetooth!"+Thread.currentThread().getName());
        Observable<JSONObject> myObservable = Observable.create(
                new Observable.OnSubscribe<JSONObject>() {
                    BluetoothSocket sock = null;
                    @Override
                    public void call(Subscriber<? super JSONObject> sub) {
                        if (connected == true) return;

                        connected = false;
                        unlocked = false;

                        //Config
                        int btChannel = Integer.parseInt(prefs.getString("pref_bt_channel","1"));

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
                                final String cert = (certs.size() > 0)?certs.values().iterator().next():"";
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
                                sendersub = sender.subscribeOn(Schedulers.io()).subscribe(new Action1<JSONObject>() {
                                    @Override
                                    public void call(JSONObject s) {
                                        try {
                                            out.write(s.toString().getBytes());
                                            out.flush();
                                        } catch (IOException ioe) {
                                            //sub.onError(ioe);
                                            Log.e(TAG,"",ioe);
                                        }
                                    }
                                });

                                boolean running = true;
                                BufferedInputStream input = new BufferedInputStream(sock.getInputStream());

                                int cnt = 0;
                                while(running) {

                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    Log.i(TAG, "Sent to socket - ready : " + input.available());
                                    int open = input.read();
                                    if( open != '{') {
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
                                            if( tmp == '{' ) cnt++;
                                            else if( tmp == '}') {
                                                cnt--;
                                                if(cnt == 0) { //done
                                                    baos.flush();
                                                    String toparse = baos.toString();
                                                    baos.reset();
                                                    //Log.d(TAG, "Pre Obj: "+toparse);
                                                    JSONObject inObj = new JSONObject(toparse);
                                                    Log.d(TAG, "Done Obj: " + inObj.toString(2));
                                                    sub.onNext( inObj );
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
                                Log.e(TAG,"Resetting .....");
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
                                if(sendersub != null) sendersub.unsubscribe();
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

    //Service Sub
    public static Observable<JSONObject> connectCloud( final String[] services,  final String host, final int port, final ConnectivityManager cm , final Observable<JSONObject> sender) {


        Log.i(TAG, "Connecting to Cloud!");
        Observable<JSONObject> myObservable = Observable.create(
                new Observable.OnSubscribe<JSONObject>() {

                    @Override
                    public void call(Subscriber<? super JSONObject> sub) {
                        if (cm.getActiveNetworkInfo() == null || !cm.getActiveNetworkInfo().isConnected()) {
                            sub.onError(new Exception("No RVI network"));
                            return;
                        }


                        RviServerConnection server = new RviServerConnection(host, port);
                        Subscription sendersub = null;
                        try {

                            boolean running = true;
                            BufferedInputStream input = new BufferedInputStream(server.getInputStream());
                            int cnt = 0;

                            final OutputStream out = server.getOutputStream();

                            JSONObject auth = RviProtocol.createAuth(1, server.socket.getLocalAddress().getHostAddress(), 1, "", "");
                            out.write(auth.toString().getBytes());
                            out.flush();
                            Log.i(TAG, "Sent AU to server. ");

                            //If first passed meens socket is up, subscribe and let the flow start
                            sendersub = sender.subscribeOn(Schedulers.io()).subscribe(new Action1<JSONObject>() {
                                @Override
                                public void call(JSONObject s) {
                                    try {
                                        out.write(s.toString().getBytes());
                                        out.flush();
                                    } catch (IOException ioe) {
                                        //sub.onError(ioe);
                                        Log.e(TAG,"",ioe);
                                    }
                                }
                            });

                            //String[] ss = {"jlr.com/mobile/865800020280340/dm/cert_provision"};

//                            JSONObject saData = RviProtocol.createServiceAnnouncement(
//                                    1, services, "av", "", "");
//                            out.write(saData.toString().getBytes());
//                            out.flush();
//                            Log.i(TAG, "Sent SA to server. ");

                            while (running) {

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                Log.i(TAG, "Sent to socket - ready : " + input.available());
                                int open = input.read();

                                if (open != '{') {
                                    //running = false;
                                    Log.i(TAG, "First char did not match : " + open);
                                    while (input.available() > 0) {
                                        int tmp = input.read();
                                        baos.write(tmp);
                                    }
                                    baos.flush();
                                    String toparse = baos.toString();
                                    Log.i(TAG, "BAD DATA Received from Cloud : " + toparse);
                                    baos.reset();
                                    // break;
                                }
                                cnt++;
                                baos.write(open); //Wait for the first then go
                                Log.i(TAG, "Sent to socket - ready : " + input.available() + " open = " + open);

                                // Added "|| cnt > 0" for truncated data issue. See,
                                // https://github.com/PDXostc/rvi_mobile_unlock/issues/3
                                while (input.available() > 0 || cnt > 0) {
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
                                                Log.i(TAG, "Received from Cloud : " + toparse);
                                                JSONObject obj = new JSONObject(toparse);
                                                sub.onNext(obj);
                                            }
                                        }
                                    } catch (IOException ioe) {
                                        ioe.printStackTrace();
                                    }
                                }
                            }
                        } catch (IOException e) {
                            sub.onError(e);
                        } catch (JSONException e) {
                            sub.onError(e);
                        } catch (Throwable t) {
                            sub.onError(t);
                        } finally {
                            sub.onCompleted();
                            if( sendersub != null ) sendersub.unsubscribe();
                            try {
                                if (server.socket != null) server.socket.close();
                            } catch (IOException e) {
                                sub.onError(e);
                            }
                        }

                    }
                }
        );

        return myObservable;
    }

    private void notifyWatch(String not) {
//        Intent i = new Intent();
//        i.setAction("com.ericsson.auto.remote.wearme.SHOW_NOTIFICATION");
//        i.putExtra(WatchNotificationReceiver.CONTENT_KEY, not);
//        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
//        sendBroadcast(i);
        //WakefulBroadcastReceiver.completeWakefulIntent(i);
        Log.i(TAG, "Sent #1 broadcast");
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String user = JSONParser(prefs.getString("Userdata", "Nothing There!!"), "username");
        String vehicle = JSONParser(prefs.getString("Userdata", "Nothing There!!"), "vehicleVIN");
        //final String cert = (certs.size() > 0)?certs.values().iterator().next():"";
        final String cert = "";
        if( btSender != null && btSender.hasObservers() ) {
            Log.i(TAG, "Invoking service : " + service + " on car, we have a BT socket");
            try {
                JSONArray locationData = new JSONArray();
                JSONObject location = new JSONObject();
                location.put("username", user);
                location.put("vehicleVIN", vehicle);
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

    public static void sendaKey(JSONArray json){
        JSONObject send;
        try{
            send = RviProtocol.createReceiveData(3,"jlr.com/backend/dm/cert_create",json,"","");
            // Testing for dupe service invokes
            //send = RviProtocol.createRequestData(3, "jlr.com/backend/dm/cert_create", json.getJSONObject(0), "", "");
            Log.d(TAG,"Successfully sent"+send.toString());
            // Testing for dupe service invokes
            Log.d("stack cloud send: ", Thread.currentThread().getStackTrace().toString());
            Thread.currentThread().getStackTrace();
            cloudSender.onNext(send);
        }catch(Exception e) {
            e.printStackTrace();
            cloudSender.onError(e);
        }

    }

    public static void requestAll(JSONArray json){
        JSONObject request;
        JSONObject uuid = new JSONObject();
        try{
            uuid.put("mobileUUID", tm.getDeviceId());
            json.put(uuid);
            request = RviProtocol.createReceiveData(4, "jlr.com/backend/dm/cert_requestall", json, "", "");
            // Testing for dupe service invokes
            Log.d(TAG, "Successfully sent" + request.toString());
            Log.d("stack cloud send: ", Thread.currentThread().getStackTrace().toString());
            Thread.currentThread().getStackTrace();
            // Testing for dupe service invokes
            //request = RviProtocol.createRequestData(4, "jlr.com/backend/dm/cert_requestall", json.getJSONObject(0), "", "");
            cloudSender.onNext(request);

        }catch (Exception e){
            e.printStackTrace();
            cloudSender.onError(e);}
    }

    public  static void revokeKey(JSONArray json){
        JSONObject send;
        try{
            send = RviProtocol.createReceiveData(3,"jlr.com/backend/dm/cert_modify",json,"","");
            // Testing for dupe service invokes
            //send = RviProtocol.createRequestData(3, "jlr.com/backend/dm/cert_modify", json.getJSONObject(0), "", "");
            Log.d(TAG, "Successfully sent" + send.toString());
            Log.d("stack cloud send: ", Thread.currentThread().getStackTrace().toString());
            Thread.currentThread().getStackTrace();
            cloudSender.onNext(send);
        }catch(Exception e) {
            e.printStackTrace();
            cloudSender.onError(e);
        }
    }
    public static String JSONParser(String jsonString, String RqstData)
    {
        try {
            JSONObject json = new JSONObject(jsonString);
            String parameterVal = json.getString(RqstData);
            return parameterVal;
        }catch (Exception e){
            //e.printStackTrace();
        }
        return "0";
    }
}
