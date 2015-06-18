/**
 *  Copyright (C) 2015, Jaguar Land Rover
 *
 *  This program is licensed under the terms and conditions of the
 *  Mozilla Public License, version 2.0.  The full text of the
 *  Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 */

package com.jaguarlandrover.auto.remote.vehicleentry;

import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class RviConnection  {
    private static final String TAG = "RVI";

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

    //{"tid":1,"cmd":"rcv","mod":"proto_json_rpc","data":"eyJzZXJ2aWNlIjoiamxyLmNvbS9idC9zdG9mZmUvbG9jayIsInRpbWVvdXQiOjE
    //0MzQwNTMyNTkwMDAsInBhcmFtZXRlcnMiOlt7ImEiOiJiIn1dLCJzaWduYXR1cmUiOiJzaWduYXR1cmUiLCJjZXJ0aWZpY2F
    //0ZSI6ImNlcnRpZmljYXRlIn0="}
    //
    // Decoded
    // {"service":"jlr.com/bt/stoffe/lock","timeout":1434053259000,"parameters":[{"a":"b"}],"signature":"signature","certificate":"certificate"}

    public static JSONObject createReceiveData(int tid, String service, JSONArray params, String cert, String sig) throws JSONException {
        JSONObject payload = new JSONObject();
        payload.put("service", service);
        payload.put("timeout", 1433266704); //TODO
        payload.put("parameters", params);
        payload.put("certificate", cert);
        payload.put("signature", sig);

        JSONObject rcvData = new JSONObject();
        rcvData.put("tid", tid);
        rcvData.put("cmd", "rcv");

        rcvData.put("mod", "proto_json_rpc");
        String enc = Base64.encodeToString(payload.toString().getBytes(), 0);
        rcvData.put("data", enc);

        Log.d(TAG, "rcv : " + rcvData.toString());
        return rcvData;
    }


    //{"sign": "","cmd": "sa","tid": 1,"svcs": ["jlr.com\/bt\/stoffe\/unlock","jlr.com\/bt\/stoffe\/unlock:lock"],"stat": "av"}
    public static JSONObject createServiceAnnouncement(int tid, String[] services, String stat, String cert, String sig) throws JSONException {
        JSONObject sa = new JSONObject();

        sa.put("tid", tid);
        sa.put("cmd", "sa");
        sa.put("stat", stat);
        sa.put("svcs", services);
        sa.put("certificate", cert);
        sa.put("signature", sig);

        Log.d(TAG, "sa : " + sa.toString());
        return sa;
    }

    //{"cmd": "ping"}
}
