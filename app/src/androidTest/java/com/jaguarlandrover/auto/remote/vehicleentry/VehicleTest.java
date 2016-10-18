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
public class VehicleTest extends TestCase
{
    Gson gson = new Gson();

    protected void setUp() {

    }

    protected void tearDown() {

    }

    public void testPrettyFormattedDateTime_FromJson() {
        String jsonString =
                "{\n" +
                "    \"vehicle_id\": \"12345\",\n" +
                "    \"display_name\": \"12345\",\n" +
                "    \"user_type\": \"guest\",\n" +
                "    \"valid_to\": \"2016-10-20T20:20:30.000Z\",\n" +
                "    \"valid_from\": \"2015-10-20T10:20:30.000Z\",\n" +
                "    \"authorized_services\": {\n" +
                "        \"engine\": \"True\",\n" +
                "        \"windows\": \"True\",\n" +
                "        \"lock\": \"True\",\n" +
                "        \"hazard\": \"True\",\n" +
                "        \"horn\": \"True\",\n" +
                "        \"lights\": \"True\",\n" +
                "        \"trunk\": \"True\"\n" +
                "    }\n" +
                "}";

        Vehicle vehicle = gson.fromJson(jsonString, Vehicle.class);

        /* Tests are going to fail when not in daylight savings or if not in Pacific timezone, but, frankly, I don't care */
        assertEquals("10/20/2015 3:20 AM PDT", vehicle.getValidFrom());
        assertEquals("10/20/2016 1:20 PM PDT", vehicle.getValidTo());
    }

    public void testPrettyFormattedDateTime_FromGoodString() {
        Vehicle vehicle = new Vehicle();

        vehicle.setValidFrom("2015-10-20T15:20:30.000Z");
        vehicle.setValidTo("2016-10-20T10:20:30.000Z");

        /* Tests are going to fail when not in daylight savings or if not in Pacific timezone, but, frankly, I don't care */
        assertEquals("10/20/2015 8:20 AM PDT", vehicle.getValidFrom());
        assertEquals("10/20/2016 3:20 AM PDT", vehicle.getValidTo());
    }

    public void testPrettyFormattedDateTime_From_AM_GMT_TO_AM_PDT() {
        Vehicle vehicle = new Vehicle();

        vehicle.setValidFrom("2015-10-20T09:20:30.000Z");
        vehicle.setValidTo("2016-10-20T10:20:30.000Z");

        /* Tests are going to fail when not in daylight savings or if not in Pacific timezone, but, frankly, I don't care */
        assertEquals("10/20/2015 2:20 AM PDT", vehicle.getValidFrom());
        assertEquals("10/20/2016 3:20 AM PDT", vehicle.getValidTo());
    }

    public void testPrettyFormattedDateTime_From_PM_GMT_TO_PM_PDT() {
        Vehicle vehicle = new Vehicle();

        vehicle.setValidFrom("2015-10-20T20:20:30.000Z");
        vehicle.setValidTo("2016-10-20T21:20:30.000Z");

        /* Tests are going to fail when not in daylight savings or if not in Pacific timezone, but, frankly, I don't care */
        assertEquals("10/20/2015 1:20 PM PDT", vehicle.getValidFrom());
        assertEquals("10/20/2016 2:20 PM PDT", vehicle.getValidTo());
    }

    public void testPrettyFormattedDateTime_From_PM_GMT_TO_AM_PDT() {
        Vehicle vehicle = new Vehicle();

        vehicle.setValidFrom("2015-10-20T16:20:30.000Z");
        vehicle.setValidTo("2016-10-20T17:20:30.000Z");

        /* Tests are going to fail when not in daylight savings or if not in Pacific timezone, but, frankly, I don't care */
        assertEquals("10/20/2015 9:20 AM PDT", vehicle.getValidFrom());
        assertEquals("10/20/2016 10:20 AM PDT", vehicle.getValidTo());
    }

    public void testPrettyFormattedDateTime_From_AM_GMT_TO_PM_PDT() {
        Vehicle vehicle = new Vehicle();

        vehicle.setValidFrom("2015-10-20T04:20:30.000Z");
        vehicle.setValidTo("2016-10-20T05:20:30.000Z");

        /* Tests are going to fail when not in daylight savings or if not in Pacific timezone, but, frankly, I don't care */
        assertEquals("10/19/2015 9:20 PM PDT", vehicle.getValidFrom());
        assertEquals("10/19/2016 10:20 PM PDT", vehicle.getValidTo());
    }

    public void testPrettyFormattedDateTimeFrom_FromBadString() {
        Vehicle vehicle = new Vehicle();

        vehicle.setValidFrom("2015/10/20 15:20:30");
        vehicle.setValidTo("2016/10/20 10:20:30 xxxxxxxxxx");

        assertEquals("2015/10/20 15:20:30", vehicle.getValidFrom());
        assertEquals("2016/10/20 10:20:30 xxxxxxxxxx", vehicle.getValidTo());
    }

    public void testPrettyFormattedDateTime_FromDate() {
        Vehicle vehicle = new Vehicle();

        Date from = new Date(1420136430000L);
        Date to = new Date(1420143630000L);

        vehicle.setValidFromAsDate(from); /* 01/01/2015 10:20:30 */
        vehicle.setValidToAsDate(to);     /* 01/01/2015 15:20:30 */

        assertEquals("01/01/2015 10:20 AM PDT", vehicle.getValidFrom());
        assertEquals("01/01/2015 12:20 PM PDT", vehicle.getValidTo());
    }

    public void testIsKeyValid_Valid() {
        Vehicle vehicle = new Vehicle();

        vehicle.setValidFrom("2010-01-01T10:20:30.000Z"); /* 01/01/2010 */
        vehicle.setValidTo("2020-01-01T10:20:30.000Z");   /* 01/01/2020 */

        assertTrue(vehicle.isKeyValid());
    }

    public void testIsKeyValid_TooEarly() {
        Vehicle vehicle = new Vehicle();

        vehicle.setValidFrom("2010-01-01T10:20:30.000Z"); /* 01/01/2010 */
        vehicle.setValidTo("2011-01-01T10:20:30.000Z");   /* 01/01/2011 */

        assertFalse(vehicle.isKeyValid());
    }

    public void testIsKeyValid_TooLate() {
        Vehicle vehicle = new Vehicle();

        vehicle.setValidFrom("2020-01-01T10:20:30.000Z"); /* 01/01/2020 */
        vehicle.setValidTo("2021-01-01T10:20:30.000Z");   /* 01/01/2021 */

        assertFalse(vehicle.isKeyValid());
    }

    public void testIsKeyValid_TooEarlyAndTooLate() {
        Vehicle vehicle = new Vehicle();

        vehicle.setValidFrom("2020-01-01T10:20:30.000Z"); /* 01/01/2020 */
        vehicle.setValidTo("2010-01-01T10:20:30.000Z");   /* 01/01/2010 */

        assertFalse(vehicle.isKeyValid());
    }

    public void testIsKeyValid_Timezones() {
        // TODO: Write this test
    }
}
