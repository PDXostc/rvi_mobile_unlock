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
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
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
    private TelephonyManager tm;
    private ConcurrentHashMap<String,Long> visible = new ConcurrentHashMap<String, Long>();

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

    //private PublishSubject<String> notificationSubject = PublishSubject.create();

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        //connectCloud(this); //Make sure connection is there ...
        _connectConnect();

        return mBinder;
    }

    private void _connectConnect() {
        String rviServer = prefs.getString("pref_rvi_server","rvi-test1.nginfotpdx.net");
        int rviPort = Integer.parseInt(prefs.getString("pref_rvi_server_port","8807"));

        //Create service vector
        final String certProv = "jlr.com/mobile/" + tm.getDeviceId() + "/dm/cert_provision";
        final String[] ss = {certProv};

        final PublishSubject<JSONObject> cloudSender = PublishSubject.create();

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
                if( !s.has("cmd") ) {
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
                        Log.i(TAG, "Received Service : " + servicePtr);
                        //Log.i(TAG, "Received Service : " + data);
                        //CERT SERVICE
                        JSONArray params = data.getJSONArray("parameters");
                        //Log.i(TAG, "Received Cert Params : " + params);
                        JSONObject p1 = params.getJSONObject(0);
                        JSONObject p2 = params.getJSONObject(1);
                        String certId = p1.getString("certid");
                        String jwt = p2.getString("certificate");
                        Log.i(TAG, "Received from Cloud Cert ID: " + certId);
                        Log.i(TAG, "JWT = " + jwt);
                        certs.put(certId, jwt);
                        //Debug
                        String[] token = RviProtocol.parseAndValidateJWT(jwt);
                        JSONObject key = new JSONObject(token[1]);
                        Log.d(TAG, "Token = "+ key.toString(2));
                        sendNotification(RviService.this, getResources().getString(R.string.not_new_key)+" : "+key.getString("id"),
                                "dialog", "New Key", key.getString("id"));

                    } else if ("sa".equals(cmd)) {
                        if (!s.has("svcs")) {
                            Log.w(TAG, "SERVICES is missing!");
                            return; //Very strange
                        }
                        JSONArray svcs = s.getJSONArray("svcs");
                        String[] services = new String[svcs.length()];
                        for ( int i = 0 ; i < services.length  ; i++ ) {
                            services[i] = svcs.getString(i);
                        }
                        //Just print
                        for(String s1 : services) {
                            Log.d(TAG,"Found service : "+s1);
                        }


                    } else if ("au".equals(cmd)) {
                        String addr = s.getString("addr");
                        int port = s.getInt("port");
                        Log.d(TAG,"Authentication from server "+addr+" : "+port);

                        JSONObject saData = RviProtocol.createServiceAnnouncement(
                                1, ss, "av", "", "");
                        cloudSender.onNext( saData );


                    } else if ("ping".equals(cmd)) { //NOOP
                    } else {
                        Log.w(TAG, "Unknown command received - " + cmd);
                    }
                } catch (JSONException e) {
                    Log.e(TAG,"",e);
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

            final int unlockDistance = Integer.parseInt(prefs.getString("pref_auto_unlock_dist", "1"));
            final int connectDistance = Integer.parseInt(prefs.getString("pref_auto_conn_dist", "3"));
            final int lockDistance = Integer.parseInt(prefs.getString("pref_auto_lock_dist", "10"));

            Log.d(TAG,"distance:"+ro.distance+", lockDistance:"+lockDistance+", unlockDistance:"+unlockDistance+", connectDistance:"+connectDistance);
            Log.d(TAG,"connected:"+connected+", connecting:"+connecting+", unlocked:"+unlocked);


            if( !connected && (ro.distance > connectDistance ) ) {
                Log.d(TAG, "Too far out to connect : " + ro.distance);
                return;
            }

            if( connected && (!unlocked) && (ro.distance > unlockDistance ) ) {
                Log.d(TAG, "Too far out unlock : " + ro.distance);
                return;
            }

            if (connected && (!unlocked) && ro.distance <= unlockDistance) {
                unlocked = true;
                RviService.service("unlock");
                sendNotification(RviService.this, getResources().getString(R.string.not_auto_unlock));
                return;
            }

            if (connected && unlocked && ro.distance >= lockDistance) {
                unlocked = false;
                RviService.service("lock");
                sendNotification(RviService.this, getResources().getString(R.string.not_auto_lock));
                return;
            }

            if(connecting) {
                Log.i(TAG, "Already connecting to BT endpoint.");
                return;
            }

            if( connected ) {
                //br.stop(); //stop reporting
                return;
            }

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
                                            service("ping");
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

                        connected = false;
                        unlocked = false;

                        //Config
                        int btChannel = Integer.parseInt(prefs.getString("pref_bt_channel","1"));

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
//
                            device.createBond();
                            //Log.i(TAG,"3 "+socket+" : "/*device.createBond()*/);
                            sock.connect();
                        } catch (IOException e) {
                            Log.d(TAG, "Excepption:" + e.getLocalizedMessage());
                            try {
                                Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                                sock = (BluetoothSocket) m.invoke(device, btChannel);
                                sock.connect();
                            } catch (InvocationTargetException ite) {
                                e.printStackTrace();
                            } catch (NoSuchMethodException ne) {
                                e.printStackTrace();
                            } catch (IllegalAccessException ie) {
                                e.printStackTrace();
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
                                out.write(auth.toString().getBytes());
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
                }
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

    public static void service(String service) {
        Log.i(TAG, "Invoking service : "+service+" the car, conn = " + btSender);

        //final String cert = (certs.size() > 0)?certs.values().iterator().next():"";
        final String cert = "";
        if( btSender != null && btSender.hasObservers() ) {
            Log.i(TAG, "Invoking service : "+service+" on car, we have a BT socket");
            try {
                JSONObject rcv = RviProtocol.createReceiveData(2, "jlr.com/bt/stoffe/" + service,
                        new JSONArray("[{\"O\":\"K\"}]"), cert, "");

                btSender.onNext(rcv);
            } catch (JSONException e) {
                btSender.onError(e);
            }

        }

    }

}
