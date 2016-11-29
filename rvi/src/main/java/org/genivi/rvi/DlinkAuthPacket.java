package org.genivi.rvi;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2015 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    DlinkAuthPacket.java
 * Project: RVI
 *
 * Created by Lilli Szafranski on 6/15/15.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * The Dlink "authorization" packet. This request is used to authorize an RVI node.
 */
class DlinkAuthPacket extends DlinkPacket
{
    private final static String TAG = "RVI/DlinkAuthPacket____";

    @SerializedName("addr")
    private String mAddr;

    @SerializedName("port")
    private Integer mPort;

    @SerializedName("ver")
    private String mVer;

    @SerializedName("creds")
    private ArrayList<String> mCreds;

    /**
     * Default constructor
     */
    DlinkAuthPacket(ArrayList<String> creds) {
        super(Command.AUTHORIZE);

        mAddr = "0.0.0.0";
        mPort = 0;
        mVer = "1.0";
        mCreds = creds;
    }

    public ArrayList<String> getCreds() {
        return mCreds;
    }

    public String getVer() {
        return mVer;
    }

    public Integer getPort() {
        return mPort;
    }

    public String getAddr() {
        return mAddr;
    }

    String getType() { return "AU"; }
}
