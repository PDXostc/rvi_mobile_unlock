package com.jaguarlandrover.rvi;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2015 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    DlinkReceivePacket.java
 * Project: RVI SDK
 *
 * Created by Lilli Szafranski on 6/15/15.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import android.util.Base64;
import com.google.gson.annotations.SerializedName;

/**
 * The type Dlink "receive" request packet. This request is used to invoke RVI services.
 */
class DlinkReceivePacket extends DlinkPacket
{
    private final static String TAG = "RVI:DlinkReceivePacket";

    /**
     * The mod parameter.
     * This client is only using 'proto_json_rpc' at the moment.
     */
    @SerializedName("mod")
    private String mMod;

    /**
     * The Service used to create the request params.
     */
    @SerializedName("data")
    private Service mService;
//    private transient Service mService;

    /**
     * The service is converted to a json string, then base64 encoded to be embedded in the packet's json.
     */
//    @SerializedName("data")
//    private String mData;

    /**
     * Instantiates a new Dlink receive packet.
     */
    DlinkReceivePacket() {
    }

    /**
     * Helper method to get a receive dlink json object.
     *
     * @param service The service that is getting invoked
     */
    DlinkReceivePacket(Service service) {
        super(Command.RECEIVE);

        mMod = "proto_json_rpc";
        mService = service;


        // TODO: With this paradigm, if one of the parameters of mService changes, mData string will still be the same.
        //mData = mService.jsonString();//Base64.encodeToString(mService.jsonString().getBytes(), Base64.DEFAULT);
    }

//    public DlinkReceivePacket(HashMap jsonHash) {
//        super(Command.RECEIVE, jsonHash);
//
//        mMod = (String) jsonHash.get("mod");
//
//        mService = new Service(new String(Base64.decode((String)jsonHash.get("data"), Base64.DEFAULT)));
//    }

    /**
     * Gets the service that is being invoked over the network.
     *
     * @return the service that is being invoked
     */
    Service getService() {
//        if (mService == null && mData != null)
//            mService = new Service(mData);//new String(Base64.decode(mData, Base64.DEFAULT)));

        return mService;
    }

    /**
     * Sets the service that is being invoked over the network.
     *
     * @param service the service
     */
    void setService(Service service) {
        mService = service;
    }
}
