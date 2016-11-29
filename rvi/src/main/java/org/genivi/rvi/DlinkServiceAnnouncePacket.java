package org.genivi.rvi;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2015 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    DlinkServiceAnnouncePacket.java
 * Project: RVI
 *
 * Created by Lilli Szafranski on 7/1/15.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * The type Dlink "service announce" request packet. This request is used to announce RVI node services.
 */
class DlinkServiceAnnouncePacket extends DlinkPacket
{
    private final static String TAG = "RVI/DlinkSrvcAnncPacket";

    enum Status {
        AVAILABLE,
        UNAVAILABLE
    }

    /**
     * The status.
     */
    @SerializedName("stat")
    private String mStatus;

    /**
     * The list of fully-qualified service names that are being announced.
     */
    @SerializedName("svcs")
    private ArrayList<String> mServices;

    /**
     * Instantiates a new Dlink service announce packet.
     */
    DlinkServiceAnnouncePacket() {
    }

    /**
     * Helper method to get a service announce dlink json object
     *
     * @param services The array of services to announce
     */
    DlinkServiceAnnouncePacket(ArrayList<String> services, Status status) {
        super(Command.SERVICE_ANNOUNCE);

        mStatus = status == Status.AVAILABLE ? "av" : "un";
        mServices = services;
    }

    /**
     * Gets list of fully-qualified local service names.
     *
     * @return the list of fully-qualified local service names
     */
    ArrayList<String> getServices() {
        return mServices;
    }

    Status getStatus() {
        return mStatus.equals("un") ? Status.UNAVAILABLE : Status.AVAILABLE;
    }

    String getType() { return "SA"; }
}
