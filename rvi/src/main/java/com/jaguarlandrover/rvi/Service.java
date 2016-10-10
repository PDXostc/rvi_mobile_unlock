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

import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The type Vehicle service.
 */
class Service
{
    private final static String TAG = "RVI:Service";

    @SerializedName("service")
    private String mFullyQualifiedServiceIdentifier = null;

    @SerializedName("parameters")
    private Object mJsonParameters = null;

    private transient String mServiceIdentifier = null;

    private transient String mBundleIdentifier = null;

    private transient String mDomain = null;

    private transient String mNodeIdentifier = null;

    private transient Object mParameters = null;

    @SerializedName("timeout")
    private Long mTimeout;

    Service() {}

    /**
     * Instantiates a new Vehicle service.
     *   @param domain the domain
     *
     *   @param nodeIdentifier the service's nodeIdentifier
     *
     *   @param bundleIdentifier the bundle identifier
     *
     *   @param serviceIdentifier the service identifier
     */
    Service(String domain, String nodeIdentifier, String bundleIdentifier, String serviceIdentifier) {
        mDomain            = domain;
        mNodeIdentifier    = nodeIdentifier;
        mBundleIdentifier  = bundleIdentifier;
        mServiceIdentifier = serviceIdentifier;

        mFullyQualifiedServiceIdentifier = getFullyQualifiedServiceIdentifier();
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

    private void parseParameters() {
        // TODO: Why are parameters arrays of object, not just an object? This should probably get fixed everywhere. Has it been?
        if (mJsonParameters.getClass().equals(ArrayList.class) && ((ArrayList<LinkedTreeMap>)mJsonParameters).size() == 1)
            mParameters = ((ArrayList<LinkedTreeMap>) mJsonParameters).get(0);
        else if (mJsonParameters.getClass().equals(ArrayList.class) && ((ArrayList<LinkedTreeMap>)mJsonParameters).size() > 1)
            mParameters = unwrap((ArrayList<LinkedTreeMap>) mJsonParameters);
        else
            mParameters = mJsonParameters;
    }

    /* If the Service object was deserialized from json, some of its fields might not be set, but the mFullyQualifiedServiceIdentifier field will be set.
       If this is the case, parse out the mFullyQualifiedServiceIdentifier field into its parts and set the rest of the fields. */
    private Boolean shouldParseServiceName() {
        return mFullyQualifiedServiceIdentifier != null && (mDomain == null || mNodeIdentifier == null || mServiceIdentifier == null);
    }

    private void parseFullyQualifiedServiceName() {
        String[] serviceParts = mFullyQualifiedServiceIdentifier.split("/");

        if (serviceParts.length < 4) return;

        mDomain = serviceParts[0];
        mNodeIdentifier = serviceParts[1] + "/" + serviceParts[2];

        StringBuilder builder = new StringBuilder();
        for (Integer i = 3; i < serviceParts.length; i++) {
            builder.append(serviceParts[i]);

            if (i < serviceParts.length - 1)
                builder.append("/");
        }

        mServiceIdentifier = builder.toString();
    }

    /**
     * Gets fully qualified service name.
     *
     * @return the fully qualified service name
     */
    String getFullyQualifiedServiceIdentifier() {
        if (shouldParseServiceName())
            parseFullyQualifiedServiceName();

        return mDomain + "/" + mNodeIdentifier + "/" + mServiceIdentifier;
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

    String getNodeIdentifier() {
        return mNodeIdentifier;
    }

    /**
     * Sets the node identifier portion of the fully-qualified service name
     *
     * @param nodeIdentifier the local or remote RVI node's identifier
     */
    void setNodeIdentifier(String nodeIdentifier) {
        mNodeIdentifier = nodeIdentifier;
        mFullyQualifiedServiceIdentifier = getFullyQualifiedServiceIdentifier();
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

    public void setDomain(String domain) {
        mDomain = domain;
        mFullyQualifiedServiceIdentifier = getFullyQualifiedServiceIdentifier();
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
            parseParameters();

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

    public Service copy() {
        Service copy = new Service(this.getDomain(), this.getNodeIdentifier(), this.getBundleIdentifier(), this.getServiceIdentifier());

        copy.setTimeout(this.getTimeout());
        copy.setParameters(this.getParameters());

        return copy;
    }
}
