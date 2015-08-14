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

/**
 * Created by rdz on 8/14/2015.
 */
public class BKTask extends AsyncTask <String, Void, String> {
    private String name = "";
    private String password="";
    private static final String TAG = "BACKEND";

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
            String urlParameters = "username="+name+"&password="+password;
            writer.write(urlParameters);
            writer.flush();

            if(httpconnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                stream = httpconnection.getInputStream();
            }
            writer.close();
        }catch(Exception ex){
            ex.printStackTrace();
        }

        return stream;
    }

    @Override
    protected void onPostExecute(String output){
        Log.d(TAG, output);
    }

    public void setUser(String user){
        this.name = user;
    }
    public void setpWd(String pwd){
        this.password = pwd;
    }
}
