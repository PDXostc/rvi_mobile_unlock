package com.jaguarlandrover.rvi;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2015 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    DlinkPacket.java
 * Project: RVI SDK
 *
 * Created by Lilli Szafranski on 6/15/15.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

/**
 * The Dlink request packet base class
 */
class DlinkPacket
{
    private final static String TAG = "RVI:DlinkPacket";

    /**
     * The Command enumeration, used to enumerate the different commands, or dlink packet types
     */
    protected enum Command
    {
        /**
         * The AUTHORIZE dlink packet type ("cmd":"au").
         */
        @SerializedName("au")AUTHORIZE("au"),
        /**
         * The SERVICE_ANNOUNCE dlink packet type ("cmd":"sa").
         */
        @SerializedName("sa")SERVICE_ANNOUNCE("sa"),
        /**
         * The RECEIVE dlink packet type ("cmd":"rcv").
         */
        @SerializedName("rcv")RECEIVE("rcv"),
        /**
         * The PING dlink packet type ("cmd":"ping").
         */
        @SerializedName("ping")PING("ping");

        private final String mString;

        /**
         * Instantiates a new Command enumerated string.
         *
         * @param string the string expected in the dlink request packet ("au", "sa", "rcv", or "ping")
         */
        Command(String string) {
            mString = string;
        }
    }

    private static Integer tidCounter = 0;

    /**
     * The transaction id.
     */
    @SerializedName("tid")
    protected Integer mTid = null;

    /**
     * The cmd that was used in the request ("au", "sa", "rcv", or "ping").
     */
    @SerializedName("cmd")
    protected Command mCmd = null;

//    /**
//     * The signature.
//     */
//    @SerializedName("sign")
//    protected String mSig = null;

    @SerializedName("rvi_log_id") /* Note: try to keep less than 20 characters */
    protected String mLogId = null;

    /**
     * Serializes the object into json strVal
     * @return the serialized json string
     */
    protected String toJsonString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    /**
     * Instantiates a new Dlink packet.
     */
    protected DlinkPacket() {

    }

    /**
     * Base constructor of the DlinkPacket
     * @param command the command ("au", "sa", "rcv", or "ping")
     */
    protected DlinkPacket(Command command) {
        if (command == null) {
            throw new IllegalArgumentException("Command can't be null");
        }

        mCmd = command;

        mTid = tidCounter++;
        //mSig = "";
    }

    // TODO: 47765, probably need to remove this constructor and update tests
    /**
     * Instantiates a new Dlink packet.
     *
     * @param command the command
     * @param jsonHash the json hash
     */
    protected DlinkPacket(Command command, HashMap jsonHash) {
        if (command == null || jsonHash == null) {
            throw new IllegalArgumentException("Constructor arguments can't be null");
        }

        mCmd = command;

        // TODO: What other args should be required?
        if (jsonHash.containsKey("tid"))
            mTid = ((Double) jsonHash.get("tid")).intValue();

//        if (jsonHash.containsKey("sign"))
//            mSig = (String) jsonHash.get("sign"); // TODO: Push for sign->sig

    }
}
