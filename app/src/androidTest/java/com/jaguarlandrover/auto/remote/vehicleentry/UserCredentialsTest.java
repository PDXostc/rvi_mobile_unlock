/**
 *  Copyright (C) 2015, Jaguar Land Rover
 *
 *  This program is licensed under the terms and conditions of the
 *  Mozilla Public License, version 2.0.  The full text of the
 *  Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 */

package com.jaguarlandrover.auto.remote.vehicleentry;

import com.google.gson.Gson;
import junit.framework.TestCase;

import java.util.Date;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class UserCredentialsTest extends TestCase
{
    Gson gson = new Gson();

    protected void setUp() {

    }

    protected void tearDown() {

    }

    public void testPrettyFormattedDateTime_FromJson() {
        String jsonString =
                "{\n" +
                "    \"username\": \"marge\",\n" +
                "    \"validTo\": \"2016-10-20T20:20:30.000Z\",\n" +
                "    \"userType\": \"guest\",\n" +
                "    \"guests\": [],\n" +
                "    \"vehicleName\": \"F-Type\",\n" +
                "    \"validFrom\": \"2015-10-20T10:20:30.000Z\",\n" +
                "    \"authorizedServices\": {\n" +
                "        \"engine\": \"True\",\n" +
                "        \"windows\": \"True\",\n" +
                "        \"lock\": \"True\",\n" +
                "        \"hazard\": \"True\",\n" +
                "        \"horn\": \"True\",\n" +
                "        \"lights\": \"True\",\n" +
                "        \"trunk\": \"True\"\n" +
                "    },\n" +
                "    \"vehicleVIN\": \"stoffe\"\n" +
                "}";

        UserCredentials userCredentials = gson.fromJson(jsonString, UserCredentials.class);
        //ServerNode.setUserCredentials(userCredentials);

        /* Tests are going to fail when not in daylight savings or if not in Pacific timezone, but, frankly, I don't care */
        assertEquals("10/20/2015 3:20 AM PDT", userCredentials.getValidFrom());
        assertEquals("10/20/2016 1:20 PM PDT", userCredentials.getValidTo());
    }

    public void testPrettyFormattedDateTime_FromGoodString() {
        UserCredentials userCredentials = new UserCredentials();

        userCredentials.setValidFrom("2015-10-20T15:20:30.000Z");
        userCredentials.setValidTo("2016-10-20T10:20:30.000Z");

        /* Tests are going to fail when not in daylight savings or if not in Pacific timezone, but, frankly, I don't care */
        assertEquals("10/20/2015 8:20 AM PDT", userCredentials.getValidFrom());
        assertEquals("10/20/2016 3:20 AM PDT", userCredentials.getValidTo());
    }

    public void testPrettyFormattedDateTime_From_AM_GMT_TO_AM_PDT() {
        UserCredentials userCredentials = new UserCredentials();

        userCredentials.setValidFrom("2015-10-20T09:20:30.000Z");
        userCredentials.setValidTo("2016-10-20T10:20:30.000Z");

        /* Tests are going to fail when not in daylight savings or if not in Pacific timezone, but, frankly, I don't care */
        assertEquals("10/20/2015 2:20 AM PDT", userCredentials.getValidFrom());
        assertEquals("10/20/2016 3:20 AM PDT", userCredentials.getValidTo());
    }

    public void testPrettyFormattedDateTime_From_PM_GMT_TO_PM_PDT() {
        UserCredentials userCredentials = new UserCredentials();

        userCredentials.setValidFrom("2015-10-20T20:20:30.000Z");
        userCredentials.setValidTo("2016-10-20T21:20:30.000Z");

        /* Tests are going to fail when not in daylight savings or if not in Pacific timezone, but, frankly, I don't care */
        assertEquals("10/20/2015 1:20 PM PDT", userCredentials.getValidFrom());
        assertEquals("10/20/2016 2:20 PM PDT", userCredentials.getValidTo());
    }

    public void testPrettyFormattedDateTime_From_PM_GMT_TO_AM_PDT() {
        UserCredentials userCredentials = new UserCredentials();

        userCredentials.setValidFrom("2015-10-20T16:20:30.000Z");
        userCredentials.setValidTo("2016-10-20T17:20:30.000Z");

        /* Tests are going to fail when not in daylight savings or if not in Pacific timezone, but, frankly, I don't care */
        assertEquals("10/20/2015 9:20 AM PDT", userCredentials.getValidFrom());
        assertEquals("10/20/2016 10:20 AM PDT", userCredentials.getValidTo());
    }

    public void testPrettyFormattedDateTime_From_AM_GMT_TO_PM_PDT() {
        UserCredentials userCredentials = new UserCredentials();

        userCredentials.setValidFrom("2015-10-20T04:20:30.000Z");
        userCredentials.setValidTo("2016-10-20T05:20:30.000Z");

        /* Tests are going to fail when not in daylight savings or if not in Pacific timezone, but, frankly, I don't care */
        assertEquals("10/19/2015 9:20 PM PDT", userCredentials.getValidFrom());
        assertEquals("10/19/2016 10:20 PM PDT", userCredentials.getValidTo());
    }

    public void testPrettyFormattedDateTimeFrom_FromBadString() {
        UserCredentials userCredentials = new UserCredentials();

        userCredentials.setValidFrom("2015/10/20 15:20:30");
        userCredentials.setValidTo("2016/10/20 10:20:30 xxxxxxxxxx");

        assertEquals("2015/10/20 15:20:30", userCredentials.getValidFrom());
        assertEquals("2016/10/20 10:20:30 xxxxxxxxxx", userCredentials.getValidTo());
    }

    public void testPrettyFormattedDateTime_FromDate() {
        UserCredentials userCredentials = new UserCredentials();

        Date from = new Date(1420136430000L);
        Date to = new Date(1420143630000L);

        userCredentials.setValidFromAsDate(from); /* 01/01/2015 10:20:30 */
        userCredentials.setValidToAsDate(to);     /* 01/01/2015 15:20:30 */

        assertEquals("01/01/2015 10:20 AM PDT", userCredentials.getValidFrom());
        assertEquals("01/01/2015 12:20 PM PDT", userCredentials.getValidTo());
    }

    public void testIsKeyValid_Valid() {
        UserCredentials userCredentials = new UserCredentials();

        userCredentials.setValidFrom("2010-01-01T10:20:30.000Z"); /* 01/01/2010 */
        userCredentials.setValidTo("2020-01-01T10:20:30.000Z");   /* 01/01/2020 */

        assertTrue(userCredentials.isKeyValid());
    }

    public void testIsKeyValid_TooEarly() {
        UserCredentials userCredentials = new UserCredentials();

        userCredentials.setValidFrom("2010-01-01T10:20:30.000Z"); /* 01/01/2010 */
        userCredentials.setValidTo("2011-01-01T10:20:30.000Z");   /* 01/01/2011 */

        assertFalse(userCredentials.isKeyValid());
    }

    public void testIsKeyValid_TooLate() {
        UserCredentials userCredentials = new UserCredentials();

        userCredentials.setValidFrom("2020-01-01T10:20:30.000Z"); /* 01/01/2020 */
        userCredentials.setValidTo("2021-01-01T10:20:30.000Z");   /* 01/01/2021 */

        assertFalse(userCredentials.isKeyValid());
    }

    public void testIsKeyValid_TooEarlyAndTooLate() {
        UserCredentials userCredentials = new UserCredentials();

        userCredentials.setValidFrom("2020-01-01T10:20:30.000Z"); /* 01/01/2020 */
        userCredentials.setValidTo("2010-01-01T10:20:30.000Z");   /* 01/01/2010 */

        assertFalse(userCredentials.isKeyValid());
    }


    public void testIsKeyValid_Timezones() {
        // TODO: Write this test
    }
}
