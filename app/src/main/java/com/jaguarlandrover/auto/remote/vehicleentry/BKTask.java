package com.jaguarlandrover.auto.remote.vehicleentry;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Created by rdz on 8/14/2015.
 */
public class BKTask extends AsyncTask <String, Void, String> {
    private String name = "";
    private String password="";
    private static final String TAG = "BACKEND";
    public LoginActivity activity;

    public BKTask(LoginActivity a)
    {
        this.activity = a;
    }

    @Override
    protected String doInBackground(String ... urls){
        String output = null;
        for(String url : urls){
            output = getOutputFromUrl(url);
        }
        return output;
    }

    private String getOutputFromUrl(String url){
        StringBuffer output = new StringBuffer("");
        try{
            InputStream stream = getHttpConnection(url);
            BufferedReader buffer = new BufferedReader(
                    new InputStreamReader(stream));
            String s= "";
            while ((s=buffer.readLine())!= null ){
                output.append(s);
            }
        }catch(IOException e1){
            e1.printStackTrace();
            Log.i(TAG, "THIS FAILED IN STREAM");
        }
        return output.toString();
    }

    private InputStream getHttpConnection(String urlString) throws IOException{
        InputStream stream = null;
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();

        try{
            HttpURLConnection httpconnection = (HttpURLConnection) connection;
            httpconnection.setRequestMethod("POST");
            httpconnection.setDoOutput(true);
            httpconnection.connect();

            OutputStreamWriter writer = new OutputStreamWriter(httpconnection.getOutputStream());
            String urlParameters = "username="+name+"&password="+password;//"username=dthiriez&password=rvi";
            writer.write(urlParameters);
            writer.flush();

            if(httpconnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                stream = httpconnection.getInputStream();
            }
            writer.close();
        }catch(Exception ex){
            ex.printStackTrace();
            Log.i(TAG, "THIS FAILED IN CONNECTION");
        }

        return stream;
    }

    @Override
    protected void onPostExecute(String output){
        Log.d(TAG, output);
        activity.setStatus(output);

    }

    public void setUser(String user){
        this.name = user;
    }
    public void setpWd(String pwd){
        this.password = pwd;
    }
}
