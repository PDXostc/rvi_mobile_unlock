package com.jaguarlandrover.rvi;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2015 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    Service.java
 * Project: RVI SDK
 *
 * Created by Lilli Szafranski on 5/19/15.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * The type Vehicle service.
 */
class Service
{
    private final static String TAG = "RVI:Service";

    @SerializedName("service")
    private String mJsonService = null;

    @SerializedName("parameters")
    private Object mJsonParameters = null;

    private String mServiceIdentifier = null;

    private String mBundleIdentifier = null;

    private String mDomain = null;

    private String mNodeIdentifier = null;

    private Object mParameters = null;

    @SerializedName("timeout")
    private Long mTimeout;

    Service() {}

    /**
     * Instantiates a new Vehicle service.
     *
     * @param serviceIdentifier the service identifier
     * @param domain the domain
     * @param bundleIdentifier the bundle identifier
     * @param prefix the service's prefix
     */
    Service(String serviceIdentifier, String domain, String bundleIdentifier, String prefix) {
        mServiceIdentifier = serviceIdentifier;
        mBundleIdentifier = bundleIdentifier;
        mDomain = domain;
        mNodeIdentifier = prefix;

        mJsonService = getFullyQualifiedServiceName();
    }

    HashMap unwrap(ArrayList<LinkedTreeMap> parameters) {
        HashMap unwrapped = new HashMap();

        for (LinkedTreeMap element : parameters)
            for (Object key : element.keySet())
                unwrapped.put(key, element.get(key));

        return unwrapped;
    }

    /* If the Service object was deserialized from json, mParameters field might not yet have been unwrapped, but the mJsonParameters
       field will be set. If this is the case, unwrap the mJsonParameters field into something more easily consumable and set mParameters.
       We only have the intermediary value here, because RVI sends parameters as a _list_ of single-kvpair objects, instead of one
       multi-kvpair object. This gives up the opportunity to unwrap the list into an object. */
    private Boolean shouldParseParameters() {
        return mJsonParameters != null && mParameters == null;
    }

    private void parseParamters() {
        // TODO: Why are parameters arrays of object, not just an object? This should probably get fixed everywhere.
        if (mJsonParameters.getClass().equals(ArrayList.class) && ((ArrayList<LinkedTreeMap>)mJsonParameters).size() == 1)
            mParameters = ((ArrayList<LinkedTreeMap>) mJsonParameters).get(0);
        else if (mJsonParameters.getClass().equals(ArrayList.class) && ((ArrayList<LinkedTreeMap>)mJsonParameters).size() > 1)
            mParameters = unwrap((ArrayList<LinkedTreeMap>) mJsonParameters);
        else
            mParameters = mJsonParameters;
    }

    /* If the Service object was deserialized from json, some of its fields might not be set, but the mJsonService field will be set.
       If this is the case, parse out the mJsonService field into its parts and set the rest of the fields. */
    private Boolean shouldParseServiceName() {
        return mJsonService != null && (mDomain == null || mNodeIdentifier == null || mBundleIdentifier == null || mServiceIdentifier == null);
    }

    private void parseFullyQualifiedServiceName() {
        String[] serviceParts = mJsonService.split("/");

        if (serviceParts.length != 5) return;

        mDomain = serviceParts[0];
        mNodeIdentifier = serviceParts[1] + "/" + serviceParts[2];
        mBundleIdentifier = serviceParts[3];
        mServiceIdentifier = serviceParts[4];
    }

    /**
     * Gets fully qualified service name.
     *
     * @return the fully qualified service name
     */
    String getFullyQualifiedServiceName() {
        if (shouldParseServiceName())
            parseFullyQualifiedServiceName();

        return mDomain + "/" + mNodeIdentifier + "/" + mBundleIdentifier + "/" + mServiceIdentifier;
    }

    /**
     * Has the node identifier portion of the fully-qualified service name. This happens if the remote node is
     * connected and has announced this service.
     *
     * @return the boolean
     */
    boolean hasNodeIdentifier() {
        return mNodeIdentifier != null;
    }

    /**
     * Sets the node identifier portion of the fully-qualified service name
     *
     * @param nodeIdentifier the local or remote RVI node's identifier
     */
    void setNodeIdentifier(String nodeIdentifier) {
        mNodeIdentifier = nodeIdentifier;
        mJsonService = getFullyQualifiedServiceName();
    }

    /**
     * Gets the domain.
     *
     * @return the domain
     */
    String getDomain() {
        if (shouldParseServiceName())
            parseFullyQualifiedServiceName();

        return mDomain;
    }

    /**
     * Gets bundle identifier.
     *
     * @return the bundle identifier
     */
    String getBundleIdentifier() {
        if (shouldParseServiceName())
            parseFullyQualifiedServiceName();

        return mBundleIdentifier;
    }

    /**
     * Gets service identifier.
     *
     * @return the service identifier
     */
    String getServiceIdentifier() {
        if (shouldParseServiceName())
            parseFullyQualifiedServiceName();

        return mServiceIdentifier;
    }

    /**
     * Gets parameters.
     *
     * @return the parameters
     */
    Object getParameters() {
        if (shouldParseParameters())
            parseParamters();

        return mParameters;
    }

    /**
     * Sets parameters.
     *
     * @param parameters the parameters
     */
    void setParameters(Object parameters) {
        this.mParameters = this.mJsonParameters = parameters;
    }

    /**
     * Gets the timeout. This value is the timeout, in milliseconds, from the epoch.
     *
     * @return the timeout
     */
    Long getTimeout() {
        return mTimeout;
    }

    /**
     * Sets the timeout.
     *
     * @param timeout the timeout in milliseconds from the epoch.
     */
    void setTimeout(Long timeout) {
        mTimeout = timeout;
    }
}
