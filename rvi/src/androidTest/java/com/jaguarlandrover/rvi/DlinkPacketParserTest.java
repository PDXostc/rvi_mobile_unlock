package com.jaguarlandrover.rvi;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2015 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    DlinkPacketParserTest.java
 * Project: RVI SDK
 *
 * Created by Lilli Szafranski on 7/6/15.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import android.test.AndroidTestCase;
import android.util.Log;

public class DlinkPacketParserTest extends AndroidTestCase implements DlinkPacketParser.DlinkPacketParserTestCaseListener, DlinkPacketParser.DlinkPacketParserListener
{
    private final static String TAG = "RVI:DlinkPacketP...Test";

    private enum TestState {
        TEST1,
        TEST2,
        TEST3,
        TEST4,
        TEST5
    }

    private TestState mTestState;
    private interface StringCallbackInterface
    {
        void handleString(String string);
    }

    private interface ObjectCallbackInterface
    {
        void handleObject(Object object);
    }

    private interface PacketCallbackInterface
    {
        void handlePacket(DlinkPacket packet);
    }

    private DlinkPacketParser mDataParser;

    private StringCallbackInterface mStringHandler;
    private ObjectCallbackInterface mObjectHandler;
    private PacketCallbackInterface mPacketHandler;

    private boolean mInStringHandler;
    private boolean mInObjectHandler;
    private boolean mInPacketHandler;

    /**
     *
     */
    public DlinkPacketParserTest() {
        super();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        mDataParser = new DlinkPacketParser(this);

        mTestState = TestState.TEST1;
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();

        mStringHandler = null;
        mObjectHandler = null;
        mPacketHandler = null;

        mInStringHandler = false;
        mInObjectHandler = false;
        mInPacketHandler = false;

        mDataParser.clear();
    }

    /**
     * Test something
     */
    public final void testSomething() {
        mStringHandler = new StringCallbackInterface()
        {
            @Override
            public void handleString(String string) {
                Log.d(TAG, "HERE: " + string);
            }
        };

        mDataParser.parseData("{\"foo\":\"bar\"}");
    }

    public final void testNull() {

        mDataParser.parseData(null);

        assertFalse(mInStringHandler);
        assertFalse(mInObjectHandler);
        assertFalse(mInPacketHandler);

        assertEquals(mDataParser.toString(), "");
    }

    public final void testEmptyString() {

        mDataParser.parseData("");

        assertFalse(mInStringHandler);
        assertFalse(mInObjectHandler);
        assertFalse(mInPacketHandler);

        assertEquals(mDataParser.toString(), "");
    }

    public final void testEmptyObject() {

        mDataParser.parseData("{}");

        assertTrue(mInStringHandler);
        assertTrue(mInObjectHandler);

        assertFalse(mInPacketHandler);

        assertEquals(mDataParser.toString(), "");
    }

    public final void testEmptyArray() {

        mDataParser.parseData("[]");

        assertTrue(mInStringHandler);
        assertTrue(mInObjectHandler);

        assertFalse(mInPacketHandler);

        assertEquals(mDataParser.toString(), "");
    }

    public final void testMalformedObject() {

        mDataParser.parseData("{foo}");

        assertTrue(mInStringHandler);

        assertFalse(mInObjectHandler);
        assertFalse(mInPacketHandler);

        assertEquals(mDataParser.toString(), "");
    }

    public final void testMalformedArray() {

        mDataParser.parseData("[\"foo\",]");

        assertTrue(mInStringHandler);

        assertFalse(mInObjectHandler);
        assertFalse(mInPacketHandler);

        assertEquals(mDataParser.toString(), "");
    }

    public final void testSimpleObject() {

        mStringHandler = new StringCallbackInterface()
        {
            @Override
            public void handleString(String string) {
                assertEquals(string, "{\"foo\":\"bar\"}");
            }
        };

        mDataParser.parseData("{\"foo\":\"bar\"}");

        assertTrue(mInStringHandler);
        assertTrue(mInObjectHandler);

        assertFalse(mInPacketHandler);

        assertEquals(mDataParser.toString(), "");
    }

    public final void testSimpleArray() {

        mStringHandler = new StringCallbackInterface()
        {
            @Override
            public void handleString(String string) {
                assertEquals(string, "[\"foo\",\"bar\"]");
            }
        };

        mDataParser.parseData("[\"foo\",\"bar\"]");

        assertTrue(mInStringHandler);

        assertFalse(mInObjectHandler);
        assertFalse(mInPacketHandler);

        assertEquals(mDataParser.toString(), "");
    }

    public final void testOpenObject() {

        mDataParser.parseData("{\"foo\":\"bar\",");

        assertFalse(mInStringHandler);
        assertFalse(mInObjectHandler);
        assertFalse(mInPacketHandler);

        assertEquals(mDataParser.toString(), "{\"foo\":\"bar\",");
    }

    public final void testOpenArray() {

        mDataParser.parseData("[\"foo\",\"bar\",");

        assertFalse(mInStringHandler);
        assertFalse(mInObjectHandler);
        assertFalse(mInPacketHandler);

        assertEquals(mDataParser.toString(), "[\"foo\",\"bar\",");
    }

    public final void testClosedObjectOpenObject() {

        mStringHandler = new StringCallbackInterface()
        {
            @Override
            public void handleString(String string) {
                assertEquals(string, "{\"foo\":\"bar\"}");
            }
        };

        mDataParser.parseData("{\"foo\":\"bar\"}{\"foo\":\"bar\",");

        assertTrue(mInStringHandler);
        assertTrue(mInObjectHandler);

        assertEquals(mDataParser.toString(), "{\"foo\":\"bar\",");
    }


    public final void testClosedObjectOpenObjectFollowedByObjectClose() {

        mTestState = TestState.TEST1;

        mStringHandler = new StringCallbackInterface()
        {
            @Override
            public void handleString(String string) {
                if (mTestState == TestState.TEST1)
                    assertEquals(string, "{\"foo\":\"bar\"}");
                else
                    assertEquals(string, "{\"foo\":\"bar\",\"bar\":\"foo\"}");
            }
        };

        mDataParser.parseData("{\"foo\":\"bar\"}{\"foo\":\"bar\",");

        assertTrue(mInStringHandler);
        assertTrue(mInObjectHandler);

        assertEquals(mDataParser.toString(), "{\"foo\":\"bar\",");

        mTestState = TestState.TEST2;

        mInStringHandler = false;
        mInObjectHandler = false;

        mDataParser.parseData("\"bar\":\"foo\"}");

        assertTrue(mInStringHandler);
        assertTrue(mInObjectHandler);

        assertEquals(mDataParser.toString(), "");
    }

    public final void testOpenObjectFollowedByOpenObject() {

        mDataParser.parseData("{\"foo\":\"bar\",");

        assertFalse(mInStringHandler);
        assertFalse(mInObjectHandler);

        assertEquals(mDataParser.toString(), "{\"foo\":\"bar\",");

        mDataParser.parseData("{\"foo\":\"bar\",");

        assertFalse(mInStringHandler);
        assertFalse(mInObjectHandler);

        assertEquals(mDataParser.toString(), "{\"foo\":\"bar\",{\"foo\":\"bar\",");

    }

    public final void testClosedObjectFollowedByOpenedObject() {

        mStringHandler = new StringCallbackInterface()
        {
            @Override
            public void handleString(String string) {
                assertEquals(string, "{\"foo\":\"bar\"}");
            }
        };

        mDataParser.parseData("{\"foo\":\"bar\"}");

        assertTrue(mInStringHandler);
        assertTrue(mInObjectHandler);

        assertEquals(mDataParser.toString(), "");

        mInStringHandler = false;
        mInObjectHandler = false;

        mDataParser.parseData("{\"foo\":\"bar\",");

        assertFalse(mInStringHandler);
        assertFalse(mInObjectHandler);

        assertEquals(mDataParser.toString(), "{\"foo\":\"bar\",");
    }


    public final void testOpenObjectFollowedByObjectClose() {

        mStringHandler = new StringCallbackInterface()
        {
            @Override
            public void handleString(String string) {
                assertEquals(string, "{\"foo\":\"bar\",\"bar\":\"foo\"}");
            }
        };

        mDataParser.parseData("{\"foo\":\"bar\",");

        assertFalse(mInStringHandler);
        assertFalse(mInObjectHandler);

        assertEquals(mDataParser.toString(), "{\"foo\":\"bar\",");

        mDataParser.parseData("\"bar\":\"foo\"}");

        assertTrue(mInStringHandler);
        assertTrue(mInObjectHandler);

        assertEquals(mDataParser.toString(), "");
    }

    public final void testOpenObjectFollowedByObjectCloseWithNewOpen() {

        mStringHandler = new StringCallbackInterface()
        {
            @Override
            public void handleString(String string) {
                assertEquals(string, "{\"foo\":\"bar\",\"bar\":\"foo\"}");
            }
        };

        mDataParser.parseData("{\"foo\":\"bar\",");

        assertFalse(mInStringHandler);
        assertFalse(mInObjectHandler);

        assertEquals(mDataParser.toString(), "{\"foo\":\"bar\",");

        mDataParser.parseData("\"bar\":\"foo\"}{\"foo\":\"bar\",");

        assertTrue(mInStringHandler);
        assertTrue(mInObjectHandler);

        assertEquals(mDataParser.toString(), "{\"foo\":\"bar\",");

    }

    public final void testMultipleClosedObjects() {

        mTestState = TestState.TEST1;
        final int[] testCounter = {0};

        mStringHandler = new StringCallbackInterface()
        {
            @Override
            public void handleString(String string) {
                testCounter[0]++;

                if (mTestState == TestState.TEST1) {
                    assertEquals(string, "{\"1A\":\"1B\"}");

                    mTestState = TestState.TEST2;
                } else if (mTestState == TestState.TEST2) {
                    assertEquals(string, "{\"2A\":\"2B\"}");

                    mTestState = TestState.TEST3;
                } else if (mTestState == TestState.TEST3) {
                    assertEquals(string, "{\"3A\":\"3B\"}");

                    mTestState = TestState.TEST4;
                } else if (mTestState == TestState.TEST4) {
                    assertEquals(string, "{\"4A\":\"4B\"}");

                    mTestState = TestState.TEST5;
                } else if (mTestState == TestState.TEST5) {
                    assertEquals(string, "{\"5A\":\"5B\"}");

                }
            }
        };

        mDataParser.parseData("{\"1A\":\"1B\"}{\"2A\":\"2B\"}{\"3A\":\"3B\"}{\"4A\":\"4B\"}{\"5A\":\"5B\"}");

        assertTrue(mInStringHandler);
        assertTrue(mInObjectHandler);

        assertEquals(testCounter[0], 5);

        assertEquals(mDataParser.toString(), "");
    }

    public final void testMultipleClosedObjectsWithNewOpen() {

        mTestState = TestState.TEST1;

        mStringHandler = new StringCallbackInterface()
        {
            @Override
            public void handleString(String string) {
                if (mTestState == TestState.TEST1) {
                    assertEquals(string, "{\"1A\":\"1B\"}");

                    mTestState = TestState.TEST2;
                } else if (mTestState == TestState.TEST2) {
                    assertEquals(string, "{\"2A\":\"2B\"}");

                    mTestState = TestState.TEST3;
                } else if (mTestState == TestState.TEST3) {
                    assertEquals(string, "{\"3A\":\"3B\"}");

                    mTestState = TestState.TEST4;
                } else if (mTestState == TestState.TEST4) {
                    assertEquals(string, "{\"4A\":\"4B\"}");

                    mTestState = TestState.TEST5;
                } else if (mTestState == TestState.TEST5) {
                    assertEquals(string, "{\"5A\":\"5B\"}");

                }
            }
        };

        mDataParser.parseData("{\"1A\":\"1B\"}{\"2A\":\"2B\"}{\"3A\":\"3B\"}{\"4A\":\"4B\"}{\"5A\":\"5B\"}{\"6A\":");

        assertTrue(mInStringHandler);
        assertTrue(mInObjectHandler);

        assertEquals(mDataParser.toString(), "{\"6A\":");
    }

    public final void testGarbage() {

        mDataParser.parseData("abcde");

        assertFalse(mInStringHandler);
        assertFalse(mInObjectHandler);
        assertFalse(mInPacketHandler);

        assertEquals(mDataParser.toString(), "");
    }

    public final void testGarbageWithNewClosedObject() {

        mDataParser.parseData("abcde{\"foo\":\"bar\"}");

        assertFalse(mInStringHandler);
        assertFalse(mInObjectHandler);

        assertEquals(mDataParser.toString(), "");
    }

    public final void testGarbageFollowedByClosedObject() {

        mStringHandler = new StringCallbackInterface()
        {
            @Override
            public void handleString(String string) {
                assertEquals(string, "{\"foo\":\"bar\"}");
            }
        };

        mDataParser.parseData("abcde");

        assertFalse(mInStringHandler);
        assertFalse(mInObjectHandler);

        assertEquals(mDataParser.toString(), "");

        mDataParser.parseData("{\"foo\":\"bar\"}");
    }


    public final void testClosedObjectWithGarbage() {

        mStringHandler = new StringCallbackInterface()
        {
            @Override
            public void handleString(String string) {
                assertEquals(string, "{\"foo\":\"bar\"}");
            }
        };

        mDataParser.parseData("{\"foo\":\"bar\"}abcde");

        assertEquals(mDataParser.toString(), "");
    }


    public final void testClosedObjectFollowedByGarbage() {

        mStringHandler = new StringCallbackInterface()
        {
            @Override
            public void handleString(String string) {
                assertEquals(string, "{\"foo\":\"bar\"}");
            }
        };

        mDataParser.parseData("{\"foo\":\"bar\"}");

        mInStringHandler = false;
        mInObjectHandler = false;

        mDataParser.parseData("abcde");


        assertFalse(mInStringHandler);
        assertFalse(mInObjectHandler);

        assertEquals(mDataParser.toString(), "");
    }

    @Override
    public void onPacketParsed(DlinkPacket packet) {
        mInPacketHandler = true;

        if (mPacketHandler != null)
            mPacketHandler.handlePacket(packet);
    }

    @Override
    public void onJsonStringParsed(String jsonString) {
        mInStringHandler = true;

        if (mStringHandler != null)
            mStringHandler.handleString(jsonString);
    }

    @Override
    public void onJsonObjectParsed(Object jsonObject) {
        mInObjectHandler = true;

        if (mObjectHandler != null)
            mObjectHandler.handleObject(jsonObject);
    }
}
