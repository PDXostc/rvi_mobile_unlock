package org.genivi.rvi;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2015 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    Service.java
 * Project: RVI
 *
 * Created by Lilli Szafranski on 5/19/15.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A class that encapsulates a service that can be received (locally) or invoked (remotely)
 */
class Service
{
    private final static String TAG = "RVI/Service____________";

    @SerializedName("service")
    private String mFullyQualifiedServiceIdentifier = null;

    @SerializedName("parameters")
    private Object mJsonParameters = null;

    private transient String mServiceIdentifier = null;

    private transient String mDomain = null;

    private transient String mNodeIdentifier = null;

    private transient Object mParameters = null;

    @SerializedName("timeout")
    private Long mTimeout;

    Service() {}

    /**
     * Instantiates a new service object.
     *   @param domain The domain. Must conform to RFC1035. Cannot contain any '/'s. Must only contain 'a-z', 'A-Z', '0-9', and '-' characters.
     *
     *   @param nodeIdentifier The service's nodeIdentifier. Must be two parts, separated by a '/', the device type token and a unique device id.
     *                         E.g., "android/12345" or "vehicle/54321".
     *
     *   @param serviceIdentifier The rest of the fully qualified service identifier. Should be more "topic levels" separated by '/'s. Shouldn't
     *                            begin or end with a '/'.
     */
    Service(String domain, String nodeIdentifier, String serviceIdentifier) {
        mDomain            = domain == null            ? null : Util.rfc1035(domain);
        mNodeIdentifier    = nodeIdentifier == null    ? null : Util.validated(nodeIdentifier);
        mServiceIdentifier = serviceIdentifier == null ? null : Util.validated(serviceIdentifier);

        if (!(mDomain == null || mNodeIdentifier == null || mServiceIdentifier == null))
            mFullyQualifiedServiceIdentifier = getFullyQualifiedServiceIdentifier();
    }

    /**
     * Unwraps the parameters json thing.
     *
     * @param parameters The parameters.
     * @return The parameters unwrapped.
     */
    HashMap unwrap(ArrayList<LinkedTreeMap> parameters) {
        HashMap unwrapped = new HashMap();

        for (LinkedTreeMap element : parameters)
            for (Object key : element.keySet())
                unwrapped.put(key, element.get(key));

        return unwrapped;
    }

    /**
     * Should we parse (deserialized) the parameters (from the json)? I wouldn't change this code if I were you!!
     * Pretty sure there's some touchy and complicated logic going on in here!
     *
     * @return If we should.
     */
    /* If the Service object was deserialized from json, mParameters field might not yet have been unwrapped, but the mJsonParameters
       field will be set. If this is the case, unwrap the mJsonParameters field into something more easily consumable and set mParameters.
       We only have the intermediary value here, because RVI sends parameters as a _list_ of single-kvpair objects, instead of one
       multi-kvpair object. This gives up the opportunity to unwrap the list into an object. */
    private Boolean shouldParseParameters() {
        return mJsonParameters != null && mParameters == null;
    }

    /**
     * If we should parse (deserialize) the parameters (from the json), then parse them!
     */
    private void parseParameters() {
        if (mJsonParameters.getClass().equals(ArrayList.class) && ((ArrayList<LinkedTreeMap>)mJsonParameters).size() == 1)
            mParameters = ((ArrayList<LinkedTreeMap>) mJsonParameters).get(0);
        else if (mJsonParameters.getClass().equals(ArrayList.class) && ((ArrayList<LinkedTreeMap>)mJsonParameters).size() > 1)
            mParameters = unwrap((ArrayList<LinkedTreeMap>) mJsonParameters);
        else
            mParameters = mJsonParameters;
    }

    /**
     * Should we parse the fully qualified service identifier?  I wouldn't change this code if I were you!! Definitely not this stuff!
     * Pretty sure there's some touchy and complicated logic going on in here!
     *
     * @return If we should.
     */
    /* If the Service object was deserialized from json, some of its fields might not be set, but the mFullyQualifiedServiceIdentifier field will be set.
       If this is the case, parse out the mFullyQualifiedServiceIdentifier field into its parts and set the rest of the fields. */
    private Boolean shouldParseServiceName() {
        return mFullyQualifiedServiceIdentifier != null && (mDomain == null || mNodeIdentifier == null || mServiceIdentifier == null);
    }

    /**
     * Parse the fully qualified service name from one long string into its parts (domain, node identifier, remainder of service identifier).
     */
    private void parseFullyQualifiedServiceName() {
        String[] serviceParts = mFullyQualifiedServiceIdentifier.split("/", -1);

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
     * @return The fully qualified service name.
     */
    String getFullyQualifiedServiceIdentifier() {
        if (shouldParseServiceName())
            parseFullyQualifiedServiceName();

        return mDomain + "/" + mNodeIdentifier + "/" + mServiceIdentifier;
    }

    /**
     * Has the node identifier portion of the fully-qualified service name (device type and device uuid). This happens if the remote node is
     * connected and has announced this service. E.g., "android/123" or "vehicle/321".
     *
     * @return The boolean.
     */
    boolean hasNodeIdentifier() {
        return mNodeIdentifier != null;
    }

    /**
     * Gets the node identifier. E.g., "android/123" or "vehicle/321".
     *
     * @return The node identifier. E.g., "android/123" or "vehicle/321".
     */
    String getNodeIdentifier() {
        return mNodeIdentifier;
    }

    /**
     * Sets the node identifier portion of the fully-qualified service name.
     *
     * @param nodeIdentifier the local or remote RVI node's identifier. E.g., "android/123" or "vehicle/321".
     */
    void setNodeIdentifier(String nodeIdentifier) {
        mNodeIdentifier = nodeIdentifier;
        mFullyQualifiedServiceIdentifier = getFullyQualifiedServiceIdentifier();
    }

    /**
     * Gets the domain.
     *
     * @return The domain.
     */
    String getDomain() {
        if (shouldParseServiceName())
            parseFullyQualifiedServiceName();

        return mDomain;
    }

    /**
     * Sets the domain.
     *
     * @param domain The domain.
     */
    void setDomain(String domain) {
        mDomain = domain;
        mFullyQualifiedServiceIdentifier = getFullyQualifiedServiceIdentifier();
    }

    /**
     * Gets service identifier.
     *
     * @return The service identifier.
     */
    String  getServiceIdentifier() {
        if (shouldParseServiceName())
            parseFullyQualifiedServiceName();

        return mServiceIdentifier;
    }

    /**
     * Gets parameters.
     *
     * @return The parameters.
     */
    Object getParameters() {
        if (shouldParseParameters())
            parseParameters();

        return mParameters;
    }

    /**
     * Sets parameters.
     *
     * @param parameters The parameters.
     */
    void setParameters(Object parameters) {
        if (parameters == null)
            this.mParameters = this.mJsonParameters = new Object();
        else
            this.mParameters = this.mJsonParameters = parameters;
    }

    /**
     * Gets the timeout. This value is the timeout, in milliseconds, from the epoch.
     *
     * @return The timeout.
     */
    Long getTimeout() {
        return mTimeout;
    }

    /**
     * Sets the timeout.
     *
     * @param timeout The timeout in milliseconds from the epoch.
     */
    void setTimeout(Long timeout) {
        mTimeout = timeout;
    }

    /**
     * Copies the service. Copy is somewhat deep. Probably should change this code either. Trace through to see what gets copied.
     *
     * @return The copy of the service.
     */
    public Service copy() {
        Service copy = new Service(this.getDomain(), this.getNodeIdentifier(), this.getServiceIdentifier());

        copy.setTimeout(this.getTimeout());
        copy.setParameters(this.getParameters());

        return copy;
    }
}
