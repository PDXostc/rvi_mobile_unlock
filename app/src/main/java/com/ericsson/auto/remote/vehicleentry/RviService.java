package com.ericsson.auto.remote.vehicleentry;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.subjects.PublishSubject;

public class RviService extends Service implements BeaconConsumer {
    private static final String TAG = "RviService";
    private BeaconManager beaconManager;
    private NotificationManager notificationManager;
    private ConnectivityManager cm;
    private ConcurrentHashMap<String,Long> visible = new ConcurrentHashMap<String, Long>();

    private BeaconDetector detector = null;
    private int timeoutSec = 10; //If beacon did not report in 10 sec then remove

    private BluetoothAdapter bluetoothAdapter;
    private Region region;

    private boolean connected = false;
    private BluetoothSocket sock;

    private PublishSubject<String> subject = null;
    ScheduledExecutorService executorService = null;

    public static final String[] SUPPORTED_SERVICES = {"jlr.com/bt/mobile/remote"};

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

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        subject = PublishSubject.create();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }

        region = new Region("Ranging Car", null, null, null);

        beaconManager = BeaconManager.getInstanceForApplication(this);
        detector = new BeaconDetector(notificationManager, this);
        initBeacon();
    }

    @Override
    public void onDestroy() {
        subject.onCompleted();
        beaconManager.unbind(this);
        beaconManager.getBeaconParsers().clear();
        if(executorService != null) executorService.shutdown();
        if(sock !=null) try {
            sock.close();
        } catch (IOException e) {
            Log.i(TAG, "closing BT");
        }
    }

    private PublishSubject<String> ps = PublishSubject.create();

    public Observable<String> servicesAvailable() {
//        String[] i = {"Login","Connect"};
//        return Observable.from(i);

        return ps;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        connectCloud(); //Make sure connection is there ...
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new RviBinder();

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        connectCloud();
        starting(intent);
        return START_STICKY;
    }

    protected void starting(Intent intent) {
        if( intent != null && intent.getExtras() != null ) {
            timeoutSec = intent.getExtras().getInt("timeoutSec", 10);
        } else {
            Log.w(TAG,"intent = "+intent);
            if(intent != null) Log.w(TAG,"extras = "+intent.getExtras());
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                for(Beacon b: beacons) {
                    String uuid = b.getId1().toString();
                    if("9277830A-B2EB-490F-A1DD-7FE38C492EDE".equalsIgnoreCase(uuid)) {
                        String bid = uuid+'-'+b.getId2().toString()+'-'+b.getId3().toString();
                        Log.d(TAG, "Found Car Index " + bid);
                        if( !visible.containsKey(bid) ) { //New
                            detector.reportNewBeacon(bid);
                        }
                        //Update timestamp
                        visible.put(bid,System.currentTimeMillis());
                    }
                    Log.i(TAG, "The beacon I see is about "+b.getDistance()+" meters away. : "+uuid);

                    if( !connected ) {
                        String remote = b.getBluetoothAddress();

                        try {
                            beaconManager.stopRangingBeaconsInRegion(region);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        beaconManager.unbind(RviService.this);
                        //bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("",UUID.randomUUID());
                        //bluetoothAdapter.getBluetoothLeAdvertiser().stopAdvertising(null);
                        //bluetoothAdapter.getBluetoothLeScanner().flushPendingScanResults(null);
                        //bluetoothAdapter.getBluetoothLeScanner().stopScan(null);


                        //bluetoothAdapter.disable();
                        //bluetoothAdapter.enable();

                        bluetoothAdapter.cancelDiscovery();
                        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(remote);
                        BluetoothSocket socket = null;
                        try {
                            socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB".toLowerCase())); //SerialPortServiceClass
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
                            //Log.i("STOFFE","3 "+socket+" : "/*device.createBond()*/);
                            socket.connect();
                        } catch (IOException e) {
                            Log.d("STOFFE", "Excepption:" + e.getLocalizedMessage());
                            try {
                                Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                                socket = (BluetoothSocket) m.invoke(device, 1);
                                socket.connect();
                            } catch (InvocationTargetException ite) {
                                e.printStackTrace();
                            } catch (NoSuchMethodException ne) {
                                e.printStackTrace();
                            } catch (IllegalAccessException ie) {
                                e.printStackTrace();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            try {
                                Log.i("STOFFE", "Sending to socket auth" + socket);
                                OutputStream out = socket.getOutputStream();
                                Notification n = BeaconDetector.creteNotification(RviService.this, "Connected to car");
                                notificationManager.notify(0,n);

                                JSONObject auth = RviConnection.createAuth(1, device.getAddress(), 1, "", "");

                                out.write(auth.toString().getBytes());
                                out.flush();


                                boolean running = true;
                                BufferedInputStream input = new BufferedInputStream(socket.getInputStream());
                                int cnt = 0;
                                while(running) {

                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    Log.i("STOFFE", "Sent to socket - ready : " + input.available());
                                    int open = input.read();
                                    if( open != '{') {
                                        running = false;
                                        Log.i("STOFFE", "First char did not match : " + open);
                                        break;
                                    }
                                    cnt++;
                                    baos.write(open); //Wait for the first then go
                                    Log.i("STOFFE", "Sent to socket - ready : " + input.available() + " open = " + open);

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
                                                    //Log.d("STOFFE", "Pre Obj: "+toparse);
                                                    JSONObject inObj = new JSONObject(toparse);
                                                    Log.d("STOFFE", "Done Obj: " + inObj.toString(2));
                                                    if (inObj.has("cmd")) {
                                                        String cmd = inObj.getString("cmd");
                                                        if(cmd!= null && cmd.equalsIgnoreCase("sa")){
                                                            Log.d("STOFFE", "sa");
                                                            if(inObj.has("svcs")) {
                                                                JSONArray services = inObj.getJSONArray("svcs");
                                                                for (int i = 0;i < services.length();i++) {
                                                                    ps.onNext(services.getString(i));
                                                                }
                                                            }

                                                            JSONObject saData = RviConnection.createServiceAnnouncement(
                                                                    1, SUPPORTED_SERVICES, "av", "", "");
                                                            out.write(saData.toString().getBytes());
                                                            out.flush();

                                                            JSONObject rcvData = RviConnection.createReceiveData(
                                                                    1, "jlr.com/bt/stoffe/unlock",
                                                                    new JSONArray("[{\"X\":\"O\"}]"), "", "");

                                                            out.write(rcvData.toString().getBytes());
                                                            out.flush();
                                                            Notification not = BeaconDetector.creteNotification(RviService.this, "Sent Unlock");
                                                            notificationManager.notify(0, not);

                                                            //ToDo send back
                                                        } else if(cmd!= null && cmd.equalsIgnoreCase("ping")) {
                                                            Log.d("STOFFE", "ping");
                                                            //todo send back
                                                        }
                                                    }
                                                }
                                            }
                                            Log.i("STOFFE", "Sent to socket - ready loop : " + input.available());
                                        } catch (IOException ioe) {
                                            ioe.printStackTrace();
                                        }
                                    }
                                }
                                socket.close();
                            } catch (IOException ioe) {

                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }

                            //device.
                            Log.i(TAG, "Baddr = " + remote);
                            connected = true;
                        }
                    }
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {   }
    }

    private void initBeacon() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                //Check for timeout in list ...
                ArrayList<String> toDelete = new ArrayList<String>();
                for(Map.Entry<String,Long> entry:visible.entrySet()) {
                    long delta = System.currentTimeMillis() - entry.getValue();
                    int deltaSec = (int) (delta/1000);
                    Log.d(TAG,"Delta = "+deltaSec+" for id = "+entry.getKey());
                    if( deltaSec > timeoutSec ) {
                        String id = entry.getKey();
                        detector.reportLostBeacon(id);
                        toDelete.add(id);
                    }
                }
                Log.d(TAG,"Scan # : "+visible.size()+" deleting : "+toDelete.size());
                for(String del:toDelete) {
                    visible.remove(del);
                }
            }
        }, 1, 1, TimeUnit.SECONDS);

        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
    }

    //Service Sub
    private void connectCloud() {
        Log.i("STOFFE", "Connecting to Cloud!");
        if(cm.getActiveNetworkInfo() == null || !cm.getActiveNetworkInfo().isConnected()) return;
        new Thread(new Runnable() {
            public void run() {
                RviServerConnection server = new RviServerConnection("rvi-test1.nginfotpdx.net",8807);
                try {
//                    OutputStream out = server.getOutputStream();
//                    JSONObject auth = RviConnection.createAuth(1, "127.0.0.1", 8807, "", "");
//
//                    out.write(auth.toString().getBytes());
//                    out.flush();

                    boolean running = true;
                    BufferedInputStream input = new BufferedInputStream(server.getInputStream());
                    int cnt = 0;
                    while(running) {

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        Log.i("STOFFE", "Sent to socket - ready : " + input.available());
                        int open = input.read();
                        if (open != '{') {
                            running = false;
                            Log.i("STOFFE", "First char did not match : " + open);
                            break;
                        }
                        cnt++;
                        baos.write(open); //Wait for the first then go
                        Log.i("STOFFE", "Sent to socket - ready : " + input.available() + " open = " + open);

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
                                        Log.i("STOFFE", "Received from Cloud : " + toparse);
                                        JSONObject obj = new JSONObject(toparse);
                                    }
                                }
                            } catch (IOException ioe) {
                                ioe.printStackTrace();
                            }
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }}).start();

    }

}
