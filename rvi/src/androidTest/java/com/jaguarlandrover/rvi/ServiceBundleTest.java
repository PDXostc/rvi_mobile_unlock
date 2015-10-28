package com.jaguarlandrover.rvi;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2015 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    ServiceBundleTest.java
 * Project: RVI SDK
 *
 * Created by Lilli Szafranski on 7/6/15.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import android.content.Context;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

public class ServiceBundleTest extends AndroidTestCase
{
    private final static String TAG = "RVI:ServiceBundleTest";

    public ServiceBundleTest() {

    }

    private void initServiceBundleWithInvalidArgs(Context context, String domain, String bundleIdentifier, ArrayList<String> servicesIdentifiers) {
        try {
            ServiceBundle bundle = new ServiceBundle(context, domain, bundleIdentifier, servicesIdentifiers);
        } catch (Exception e) {
            return;
        }

        fail();
    }

    private void initServiceBundleWithGoodArgs(Context context, String domain, String bundleIdentifier, ArrayList<String> servicesIdentifiers) {
        try {
            ServiceBundle bundle = new ServiceBundle(context, domain, bundleIdentifier, servicesIdentifiers);
        } catch (Exception e) {
            fail();
        }

        return;
    }

    public final void testConstructorArgs() {
        initServiceBundleWithInvalidArgs(null,              "domain", "bundleID", new ArrayList<String>(Arrays.asList("foo", "bar")));
        initServiceBundleWithInvalidArgs(this.getContext(), null,     "bundleID", new ArrayList<String>(Arrays.asList("foo", "bar")));
        initServiceBundleWithInvalidArgs(this.getContext(), "domain", null,       new ArrayList<String>(Arrays.asList("foo", "bar")));
        initServiceBundleWithInvalidArgs(this.getContext(), "domain", "bundleID", null);

        initServiceBundleWithGoodArgs(this.getContext(), "domain_.1", "bundleID",    new ArrayList<String>(Arrays.asList("foo",    "bar")));
        initServiceBundleWithGoodArgs(this.getContext(), "domain",    "bundleID._1", new ArrayList<String>(Arrays.asList("foo",    "bar")));
        initServiceBundleWithGoodArgs(this.getContext(), "domain",    "bundleID",    new ArrayList<String>(Arrays.asList("foo_.1", "bar")));

        initServiceBundleWithInvalidArgs(this.getContext(), "domain/$*", "bundleID",    new ArrayList<String>(Arrays.asList("foo",    "bar")));
        initServiceBundleWithInvalidArgs(this.getContext(), "domain",    "bundleID/$*", new ArrayList<String>(Arrays.asList("foo",    "bar")));
        initServiceBundleWithInvalidArgs(this.getContext(), "domain",    "bundleID",    new ArrayList<String>(Arrays.asList("foo/$*", "bar")));


    }
}
