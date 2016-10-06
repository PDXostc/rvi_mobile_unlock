package com.jaguarlandrover.auto.remote.vehicleentry;

import android.app.AlertDialog;
import android.content.*;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.jaguarlandrover.pki.PKIManager;
import com.jaguarlandrover.rvi.RVILocalNode;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Calendar;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class LoginActivity extends ActionBarActivity implements LoginActivityFragment.LoginFragmentButtonListener{

    private static final String TAG = "RVI";
    private String status = "false";
    private Boolean auth = Boolean.FALSE;
    private RviService rviService = null;
    private LoginActivityFragment mLoginActivityFragment = null;
    private boolean bound = false;

    private final static String X509_PRINCIPAL_PATTERN = "CN=%s, O=Genivi, OU=%s, EMAILADDRESS=%s";
    private final static String X509_ORG_UNIT          = "Android Unlock App";

    private final static String DEFAULT_PROVISIONING_SERVER_BASE_URL         = "http://192.168.16.245:8000";
    private final static String DEFAULT_PROVISIONING_SERVER_CSR_URL          = "/csr";
    private final static String DEFAULT_PROVISIONING_SERVER_VERIFICATION_URL = "/verification"; // TODO: 'Verification' or 'validation'?

    private boolean mRviServerConnected    = false;
    private boolean mAllValidCertsAcquired = false;
    private boolean mValidatingToken       = false;

    private KeyStore          mServerCertificateKeyStoreHolder = null;
    private KeyStore          mDeviceCertificateKeyStoreHolder = null;
    private ArrayList<String> mDefaultPrivilegesHolder         = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login2);
        handleExtra(getIntent());

        mLoginActivityFragment = (LoginActivityFragment) getFragmentManager().findFragmentById(R.id.fragmentlogin);

        mLoginActivityFragment.setVerifyButtonEnabled(true);

        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            String token  = uri.getQueryParameter("tokencode");
            String certId = uri.getQueryParameter("certid");

            if (token != null && certId != null) {
                RVILocalNode.start(this);
                RVILocalNode.removeAllCredentials(this);

                mLoginActivityFragment.setStatusTextText("Validating email...");
                mLoginActivityFragment.setVerifyButtonEnabled(false);

                mValidatingToken = true;

                Log.d(TAG, "valueOne: " + token + ", valueTwo: " + certId);

                String tokenString = "{ \"token\":\"" + token + "\", \"certId\":\"" + certId + "\"}";

                PKIManager.sendTokenVerificationRequest(this, new PKIManager.ProvisioningServerListener() {
                    @Override
                    public void certificateSigningRequestSuccessfullySent() {

                    }

                    @Override
                    public void certificateSigningRequestSuccessfullyReceived() {

                    }

                    @Override
                    public void managerDidReceiveServerSignedStuff(KeyStore serverCertificateKeyStore, KeyStore deviceCertificateKeyStore, String deviceKeyStorePassword, ArrayList<String> defaultPrivileges) {
                        Log.d(TAG, "Got server stuff, trying to connect");

                        mLoginActivityFragment.hideControls(true);
                        mLoginActivityFragment.setStatusTextText("Validating email... Processing certificates.");

                        mServerCertificateKeyStoreHolder = serverCertificateKeyStore;
                        mDeviceCertificateKeyStoreHolder = deviceCertificateKeyStore;
                        mDefaultPrivilegesHolder         = defaultPrivileges;

                        mValidatingToken       = false;
                        mAllValidCertsAcquired = true;

                        doTheRviThingIfEverythingElseIsComplete();
                    }
                }, DEFAULT_PROVISIONING_SERVER_BASE_URL, DEFAULT_PROVISIONING_SERVER_VERIFICATION_URL, tokenString);
            }
        }

        if (!mValidatingToken) {
            if (PKIManager.hasValidSignedDeviceCert(this) && PKIManager.hasValidSignedServerCert(this)) {
                mLoginActivityFragment.hideControls(true);
                mLoginActivityFragment.setStatusTextText("Binding service...");

                mAllValidCertsAcquired = true;

                mServerCertificateKeyStoreHolder = PKIManager.getServerKeyStore(this);
                mDeviceCertificateKeyStoreHolder = PKIManager.getDeviceKeyStore(this);

                doTheRviThingIfEverythingElseIsComplete();

            } else if (PKIManager.hasValidSignedDeviceCert(this)) {
                mLoginActivityFragment.setStatusTextText("Resend email");
                mLoginActivityFragment.setStatusTextText("Please check your email account and click the link.");

            } else {
                mLoginActivityFragment.setStatusTextText("The RVI Unlock Demo needs to verify your email address.");

            }
        }

        doBindService();
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

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            //mService = new Messenger(service);

            mRviServerConnected = true;

            rviService = ((RviService.RviBinder)service).getService();

            rviService
                    .servicesAvailable()
                    .subscribeOn(Schedulers.newThread())
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
                            Log.i(TAG, "X: " + s);
                            mLoginActivityFragment.onNewServiceDiscovered(s);
                        }
                    });

            // Tell the user about this for our demo.
            Toast.makeText(LoginActivity.this, "RVI service connected", Toast.LENGTH_SHORT).show();

            doTheRviThingIfEverythingElseIsComplete();
        }

        public void onServiceDisconnected(ComponentName className) {
            mRviServerConnected = false;

            rviService = null;
            Toast.makeText(LoginActivity.this, "RVI service disconnected", Toast.LENGTH_SHORT).show();
        }
    };


    private void doTheRviThingIfEverythingElseIsComplete() {
        if (mRviServerConnected && mAllValidCertsAcquired) {
            rviService.setServerKeyStore(mServerCertificateKeyStoreHolder);
            rviService.setDeviceKeyStore(mDeviceCertificateKeyStoreHolder);
            rviService.setDeviceKeyStorePassword(null);

            if (mDefaultPrivilegesHolder != null)
                rviService.setPrivileges(mDefaultPrivilegesHolder);

            rviService.tryConnectingServerNode();

            Intent intent = new Intent();

            intent.setClass(LoginActivity.this, LockActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onButtonCommand(View v) {
        submit(v);
    }

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(new Intent(LoginActivity.this, RviService.class), mConnection, Context.BIND_AUTO_CREATE);
        bound = true;
    }

    void doUnbindService() {
        if (bound) {
            // Detach our existing connection.
            unbindService(mConnection);
            bound = false;
        }
    }

    public void submit(View v) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String email = prefs.getString("savedEmail", "");

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.YEAR, 1);

        mLoginActivityFragment.setVerifyButtonEnabled(false);
        mLoginActivityFragment.setStatusTextText("Connecting to server. Please check your email in a few minutes.");

        PKIManager.generateKeyPairAndCertificateSigningRequest(this, new PKIManager.CertificateSigningRequestGeneratorListener() {
            @Override
            public void generateCertificateSigningRequestSucceeded(String certificateSigningRequest) {

                mLoginActivityFragment.setVerifyButtonEnabled(true);
                mLoginActivityFragment.setStatusTextText("Resend email");
                mLoginActivityFragment.setStatusTextText("Please check your email account and click the link.");

                PKIManager.sendCertificateSigningRequest(LoginActivity.this, new PKIManager.ProvisioningServerListener() {
                    @Override
                    public void certificateSigningRequestSuccessfullySent() {

                    }

                    @Override
                    public void certificateSigningRequestSuccessfullyReceived() {

                    }

                    @Override
                    public void managerDidReceiveServerSignedStuff(KeyStore serverCertificateKeyStore, KeyStore deviceCertificateKeyStore, String deviceKeyStorePassword, ArrayList<String> defaultPrivileges) {
                        Log.d(TAG, "Got server stuff, trying to connect");

                        mServerCertificateKeyStoreHolder = serverCertificateKeyStore;
                        mDeviceCertificateKeyStoreHolder = deviceCertificateKeyStore;
                        mDefaultPrivilegesHolder         = defaultPrivileges;

                        mValidatingToken       = false;
                        mAllValidCertsAcquired = true;

                        doTheRviThingIfEverythingElseIsComplete();
                    }
                }, DEFAULT_PROVISIONING_SERVER_BASE_URL, DEFAULT_PROVISIONING_SERVER_CSR_URL, certificateSigningRequest, true);
            }

            @Override
            public void generateCertificateSigningRequestFailed(Throwable reason) {
                // TODO: Update ui with failure message
            }

        }, start.getTime(), end.getTime(), X509_PRINCIPAL_PATTERN, RVILocalNode.getLocalNodeIdentifier(this), X509_ORG_UNIT, email);

//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//
//        BKTask task = new BKTask(this);
//        task.setUser(mLoginActivityFragment.mEmail.getEditableText().toString());
//        task.setpWd(mLoginActivityFragment.password.getEditableText().toString());
//        task.execute(new String[]{prefs.getString("pref_login_url", "http://rvi-test2.nginfotpdx.net:8000/token/new.json")});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
            Intent intent = new Intent();

            intent.setClass(LoginActivity.this, AdvancedPreferenceActivity.class);
            startActivityForResult(intent, 0);

            return true;

        } else if (id == R.id.action_reset) {
            // TODO: Dialog

            PKIManager.deleteKeysAndCerts(this);
            RVILocalNode.start(this);
            RVILocalNode.removeAllCredentials(this);

            mValidatingToken       = false;
            mAllValidCertsAcquired = false;

            mLoginActivityFragment.setStatusTextText("The RVI Unlock Demo needs to verify your email address.");
            mLoginActivityFragment.hideControls(false);
            mLoginActivityFragment.setVerifyButtonEnabled(true);

        } else if (id == R.id.action_reset_2) {
            // TODO: Dialog

            //PKIManager.deleteKeysAndCerts(this);
            RVILocalNode.start(this);
            RVILocalNode.removeAllCredentials(this);

            mValidatingToken       = false;
            mAllValidCertsAcquired = false;

            mLoginActivityFragment.setStatusTextText("The RVI Unlock Demo needs to verify your email address.");
            mLoginActivityFragment.hideControls(false);
            mLoginActivityFragment.setVerifyButtonEnabled(true);

        }

        return super.onOptionsItemSelected(item);
    }

//    public void setStatus(String msg) {
//        try {
//            JSONObject obj = new JSONObject(msg);
//            status = obj.get("success").toString();
//
//        } catch(Exception e) {
//            e.printStackTrace();
//
//        }
//
//        if (status.equals("true")) {
//            Intent intent = new Intent();
//
//            intent.setClass(LoginActivity.this, LockActivity.class);
//            startActivity(intent);
//
//        } else {
//
//            //mLoginActivityFragment.mEmail.setText("");
//            //mLoginActivityFragment.password.setText("");
//
//            ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
//            if (networkInfo == null || networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
//               Toast.makeText(LoginActivity.this, "Network unavailable", Toast.LENGTH_LONG).show();
//            } else {
//                Toast.makeText(LoginActivity.this, "username and password don't match", Toast.LENGTH_LONG).show();
//            }
//        }
//    }

    public void onNewServiceDiscovered(String... service) {
        for (String s : service)
            Log.e(TAG, "Service = " + s);
    }

    private void handleExtra(Intent intent) {
        Bundle extras = intent.getExtras();

        if (extras != null && extras.size() > 0 ) {
            for(String k : extras.keySet()) {
                Log.i(TAG, "k = " + k + " : " + extras.getString(k));
            }
        }

        if (extras != null && "dialog".equals(extras.get("_extra1"))) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            alertDialogBuilder.setTitle("" + extras.get("_extra2"));
            alertDialogBuilder
                    .setMessage("" + extras.get("_extra3"))
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            alertDialogBuilder.create().show();
        }
    }
}
