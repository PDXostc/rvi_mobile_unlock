package com.jaguarlandrover.auto.remote.vehicleentry;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.http.HttpResponseCache;
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


public class LoginActivity extends ActionBarActivity{

    private Button login;
    public EditText userName;
    public EditText password;
    private static final String TAG = "RVI";
    private String status = "false";
    private Boolean auth = Boolean.FALSE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        Typeface fontawesome = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont.ttf");
        login = (Button)findViewById(R.id.loginBtn);
        login.setTypeface(fontawesome);
        userName = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit(v);
            }
        });
    }

    public void submit(View v){
        BKTask task = new BKTask(this);
        task.setUser(userName.getEditableText().toString());
        task.setpWd(password.getEditableText().toString());
        task.execute(new String[]{"http://ec2-54-172-25-254.compute-1.amazonaws.com:8000/token/new.json"});
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
            userName.setText("");
            password.setText("");
            Toast.makeText(LoginActivity.this,"username and password don't match",Toast.LENGTH_LONG).show();
        }
    }
}
