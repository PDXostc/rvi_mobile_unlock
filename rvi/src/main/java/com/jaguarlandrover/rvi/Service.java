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

    private String mServiceIdentifier;

    private String mBundleIdentifier;

    private String mDomain;

    private String mNodeIdentifier;

    private Object mParameters;

    private Long mTimeout;

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
    }

    HashMap unwrap(ArrayList<LinkedTreeMap> parameters) {
        HashMap unwrapped = new HashMap();

        for (LinkedTreeMap element : parameters)
            for (Object key : element.keySet())
                unwrapped.put(key, element.get(key));

        return unwrapped;
    }
    /**
     * Instantiates a new Vehicle service.
     *
     * @param jsonString the json string
     */
    Service(String jsonString) {
        Log.d(TAG, "Service data: " + jsonString);

        Gson gson = new Gson();
        HashMap jsonHash = gson.fromJson(jsonString, HashMap.class);

        String[] serviceParts = ((String) jsonHash.get("service")).split("/");

        if (serviceParts.length != 5) return;

        mDomain = serviceParts[0];
        mNodeIdentifier = serviceParts[1] + "/" + serviceParts[2];
        mBundleIdentifier = serviceParts[3];
        mServiceIdentifier = serviceParts[4];

        // TODO: Why are parameters arrays of object, not just an object? This should probably get fixed everywhere.
        if (jsonHash.get("parameters").getClass().equals(ArrayList.class) && ((ArrayList<LinkedTreeMap>)jsonHash.get("parameters")).size() == 1)
            mParameters = ((ArrayList<LinkedTreeMap>) jsonHash.get("parameters")).get(0);
        else if (jsonHash.get("parameters").getClass().equals(ArrayList.class) && ((ArrayList<LinkedTreeMap>)jsonHash.get("parameters")).size() > 1)
            mParameters = unwrap((ArrayList<LinkedTreeMap>) jsonHash.get("parameters"));
        else
            mParameters = jsonHash.get("parameters");
    }

    /**
     * Gets parameters.
     *
     * @return the parameters
     */
    Object getParameters() {
        return mParameters;
    }

    /**
     * Sets parameters.
     *
     * @param parameters the parameters
     */
    void setParameters(Object parameters) {
        this.mParameters = parameters;
    }

    /**
     * Gets service identifier.
     *
     * @return the service identifier
     */
    String getServiceIdentifier() {
        return mServiceIdentifier;
    }

    /**
     * Gets fully qualified service name.
     *
     * @return the fully qualified service name
     */
    String getFullyQualifiedServiceName() {
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
     * Generate request params.
     *
     * @return the object
     */
    Object generateRequestParams() {
        HashMap<String, Object> params = new HashMap<>(4);

        params.put("service", getFullyQualifiedServiceName());
        params.put("parameters", Arrays.asList(mParameters));
        params.put("timeout", mTimeout);
        params.put("signature", "signature");
        params.put("certificate", "certificate");

        return params;
    }

    /**
     * Json string.
     *
     * @return the string
     */
    String jsonString() {
        Gson gson = new Gson();

        Log.d(TAG, "Service data: " + gson.toJson(generateRequestParams()));

        return gson.toJson(generateRequestParams());
    }

    /**
     * Gets bundle identifier.
     *
     * @return the bundle identifier
     */
    String getBundleIdentifier() {
        return mBundleIdentifier;
    }


    /**
     * Gets the domain.
     *
     * @return the domain
     */
    String getDomain() {
        return mDomain;
    }

    /**
     * Sets the node identifier portion of the fully-qualified service name
     *
     * @param nodeIdentifier the local or remote RVI node's identifier
     */
    void setNodeIdentifier(String nodeIdentifier) {
        mNodeIdentifier = nodeIdentifier;
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
