package com.jaguarlandrover.auto.remote.vehicleentry;

import android.app.AlertDialog;
import android.content.*;
import android.graphics.Typeface;
import android.net.http.HttpResponseCache;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class LoginActivity extends ActionBarActivity implements LoginActivityFragment.LoginFragmentButtonListener{

    private static final String TAG = "RVI";
    private String status = "false";
    private Boolean auth = Boolean.FALSE;
    private RviService rviService = null;
    private LoginActivityFragment login_fragment = null;
    private boolean bound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        handleExtra(getIntent());
        login_fragment = (LoginActivityFragment) getFragmentManager().findFragmentById(R.id.fragmentlogin);

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
                            login_fragment.onNewServiceDiscovered(s);
                            //Toast.makeText(LockActivity.this, "X: "+s, Toast.LENGTH_SHORT).show();
                        }
                    });

            // Tell the user about this for our demo.
            Toast.makeText(LoginActivity.this, "RVI service connected",
                    Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            rviService = null;
            Toast.makeText(LoginActivity.this, "RVI service disconnected",
                    Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    public void onButtonCommand(View v) {
        submit(v);
    }

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(new Intent(LoginActivity.this,
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



    public void submit(View v){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);;



        BKTask task = new BKTask(this);
        task.setUser(login_fragment.userName.getEditableText().toString());
        task.setpWd(login_fragment.password.getEditableText().toString());
        task.execute(new String[]{("http://" + prefs.getString("pref_rvi_server", "38.129.64.40") + ":8000/token/new.json")});
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
        }

        return super.onOptionsItemSelected(item);
    }

    public void setStatus(String msg){
        try {
            JSONObject obj = new JSONObject(msg);
            status = obj.get("success").toString();
        }
        catch(Exception e){
            e.printStackTrace();

        }

        if(status.equals("true")){
            Intent intent = new Intent();
            intent.setClass(LoginActivity.this, LockActivity.class);
            startActivity(intent);
        }
        else{
            login_fragment.userName.setText("");
            login_fragment.password.setText("");
            Toast.makeText(LoginActivity.this,"username and password don't match",Toast.LENGTH_LONG).show();
        }
    }

    public void onNewServiceDiscovered(String... service) {
        for(String s:service)
            Log.e(TAG, "Service = " + s);
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
            alertDialogBuilder.setTitle("" + extras.get("_extra2"));
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
}
