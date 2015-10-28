package com.jaguarlandrover.rvi;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2015 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    DlinkPacketParser.java
 * Project: RVI SDK
 *
 * Created by Lilli Szafranski on 7/2/15.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import android.util.Log;
import com.google.gson.Gson;

/**
 * The Dlink packet parser. Class that parses received strings first into json objects, and then parses complete json
 * objects into dlink packets based on their 'cmd'
 */
class DlinkPacketParser
{
    private final static String TAG = "RVI:DlinkPacketParser";

    private String                    mBuffer;
    private DlinkPacketParserListener mDataParserListener;

    /**
     * The interface Dlink packet parser listener. The object that's notified when complete dlink packets are parsed.
     */
    interface DlinkPacketParserListener
    {
        /**
         * On packet parsed. Callback method that notifies listener when a complete dlink packet was parsed out of the
         * input stream coming from an rvi node over the network.
         *
         * @param packet the dlink packet
         */
        void onPacketParsed(DlinkPacket packet);
    }

    /**
     * The interface Dlink packet parser test case listener. The test object that's notified when complete dlink
     * packets are parsed, with an extra method for test purposes.
     */
    interface DlinkPacketParserTestCaseListener
    {
        /**
         * On json string parsed. Callback method that notifies listener when a complete json string was parsed out of
         * the input stream coming from an rvi node over the network.
         *
         * @param jsonString the json string
         */
        void onJsonStringParsed(String jsonString);

        /**
         * On json object parsed. Callback method that notifies listener when a complete json object was parsed out of
         * the input stream coming from an rvi node over the network.
         *
         * @param jsonObject the json object
         */
        void onJsonObjectParsed(Object jsonObject);
    }

    /**
     * Instantiates a new Dlink packet parser.
     *
     * @param listener the listener
     */
    DlinkPacketParser(DlinkPacketParserListener listener) {
        mDataParserListener = listener;
    }

    /**
     *
     * @param  buffer String to parse out JSON objects from
     * @return The length of the first JSON object found, 0 if it is an incomplete object,
     *                -1 if the string does not start with a '{' or an '['
     */
    private int getLengthOfJsonObject(String buffer) {
        if (buffer.charAt(0) != '{' && buffer.charAt(0) != '[') return -1;

        int numberOfOpens  = 0;
        int numberOfCloses = 0;

        char open  = buffer.charAt(0) == '{' ? '{' : '[';
        char close = buffer.charAt(0) == '{' ? '}' : ']';

        for (int i = 0; i < buffer.length(); i++) {
            if (buffer.charAt(i) == open) numberOfOpens++;
            else if (buffer.charAt(i) == close) numberOfCloses++;

            if (numberOfOpens == numberOfCloses) return i + 1;
        }

        return 0;
    }

    private DlinkPacket stringToPacket(String string) {
        Log.d(TAG, "Received packet: " + string);

        if (mDataParserListener instanceof DlinkPacketParserTestCaseListener)
            ((DlinkPacketParserTestCaseListener) mDataParserListener).onJsonStringParsed(string);

        Gson gson = new Gson();
        DlinkPacket packet;

        try {
            packet = gson.fromJson(string, DlinkPacket.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (mDataParserListener instanceof DlinkPacketParserTestCaseListener)
            ((DlinkPacketParserTestCaseListener) mDataParserListener).onJsonObjectParsed(packet);

        DlinkPacket.Command command = packet.mCmd;

        if (command == null)
            return null;

        if (command == DlinkPacket.Command.AUTHORIZE) {
            return gson.fromJson(string, DlinkAuthPacket.class);
        } else if (command == DlinkPacket.Command.SERVICE_ANNOUNCE) {
            return gson.fromJson(string, DlinkServiceAnnouncePacket.class);
        } else if (command == DlinkPacket.Command.RECEIVE) {
            return gson.fromJson(string, DlinkReceivePacket.class);
        } else {
            return null;
        }
    }

    private String recurse(String buffer) {
        int lengthOfString     = buffer.length();
        int lengthOfJsonObject = getLengthOfJsonObject(buffer);

        DlinkPacket packet;

        if (lengthOfJsonObject == lengthOfString) { /* Current data is 1 json object */
            if ((packet = stringToPacket(buffer)) != null)
                mDataParserListener.onPacketParsed(packet);

            return "";

        } else if (lengthOfJsonObject < lengthOfString && lengthOfJsonObject > 0) { /* Current data is more than 1 json object */
            if ((packet = stringToPacket(buffer.substring(0, lengthOfJsonObject))) != null)
                mDataParserListener.onPacketParsed(packet);

            return recurse(buffer.substring(lengthOfJsonObject));

        } else if (lengthOfJsonObject == 0) { /* Current data is less than 1 json object */
            return buffer;

        } else { /* There was an error */
            return null;

        }
    }

    /**
     * Parse the data (consisting of 0-n partial or complete json objects) that was received over the network
     * from an rvi node. Method parses the string, recursively chomping off json objects as they come in,
     * deserializing them into dlink packets. Remaining string (thereby assumed to be only a partial json object)
     * is saved until rest of the json object is received over the networked and appended to the buffer.
     *
     * @param data a json string, consisting of 0-n partial or complete json objects.
     */
    void parseData(String data) {
        if (mBuffer == null) mBuffer = "";

        mBuffer = recurse(mBuffer + data);
    }

    /**
     * Clears the saved (unparsed) buffer of data.
     */
    void clear() {
        mBuffer = null;
    }

    @Override
    public String toString() {
        return mBuffer == null ? "" : mBuffer;
    }
}
