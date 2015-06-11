package com.ericsson.auto.remote.vehicleentry;

import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by stoffe on 6/8/15.
 */
public abstract class RviConnection  {
    public abstract InputStream getInputStream() throws IOException;
    public abstract OutputStream getOutputStream() throws IOException;


    //                     String output = "{\"tid\":1,\"cmd\":\"authorize\",\"addr\":"+
    //                            "\"00:1B:DC:06:7D:66\",\"chan\":1,\"rvi_proto\":"+
    //                            "\"rvi_json\",\"certificate\":\"\",\"signature\":\"\"}";
//    public static JSONObject createBtAuth(int tid, String addr, int ch, String cert, String sig) throws JSONException {
//        JSONObject auth = new JSONObject();
//        auth.put("tid", tid);
//        auth.put("cmd", "authorize");
//        auth.put("addr", addr);
//        auth.put("chan", ch);
//        auth.put("rvi_proto", "rvi_json");
//        auth.put("certificate", cert);
//        auth.put("signature", sig);
//        return auth;
//    }

    //{"tid":1,"cmd":"au","addr":"127.0.0.1","port":8807,"ver":"1.0","cert":"","sign":"" }
    public static JSONObject createAuth(int tid, String addr, int port, String cert, String sig) throws JSONException {
        JSONObject auth = new JSONObject();
        auth.put("tid", tid);
        auth.put("cmd", "au");
        auth.put("addr", addr);
        auth.put("port", port);
        auth.put("ver", "1.0");
        auth.put("certificate", cert);
        auth.put("signature", sig);
        return auth;
    }

    public static JSONObject createReceiveData(int tid, String service, JSONArray params, String cert, String sig) throws JSONException {
        JSONObject payload = new JSONObject();
        payload.put("service", service);
        payload.put("timeout", 1433266704); //TODO
        payload.put("parameters", params);
        payload.put("certificate", cert);
        payload.put("signature", sig);

        JSONObject rcvData = new JSONObject();
        rcvData.put("tid", tid);
        rcvData.put("cmd", "receive_data");

        rcvData.put("proto_mod", "proto_json_rpc");
        String enc = Base64.encodeToString(payload.toString().getBytes(), 0);
        rcvData.put("payload", enc);

        Log.d("STOFFE", "sa : " + rcvData.toString());
        return rcvData;
    }
}
