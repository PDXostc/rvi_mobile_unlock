package com.jaguarlandrover.rvi;

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2016 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    RVIRemoteNodeListener.java
 * Project: UnlockDemo
 *
 * Created by Lilli Szafranski on 10/6/16.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import java.util.ArrayList;

public interface RVIRemoteNodeListener
{
    /**
     * Called when the local RVI node successfully connects to a remote RVI node.
     */
    void nodeDidConnect(RVIRemoteNode node);

    /**
     * Called when the local RVI node failed to connect to a remote RVI node.
     *
     *  @param node The rvi node
     *
     *  @param reason The reason the node failed to connect
     */
    void nodeDidFailToConnect(RVIRemoteNode node, Throwable reason);

    /**
     * Called when the local RVI node disconnects from a remote RVI node.
     *
     *  @param node The rvi node
     *
     *  @param reason The reason the node disconnected
     */
    void nodeDidDisconnect(RVIRemoteNode node, Throwable reason);

    /**
     * Called when the local RVI node sent a service invocation to the remote RVI node.
     *
     *  @param node The rvi node
     *
     *  @param serviceIdentifier The service that was invoked
     */
    void nodeSendServiceInvocationSucceeded(RVIRemoteNode node, String serviceIdentifier);

    /**
     * Called when the local RVI node tried to send a service invocation to the remote RVI node, but it failed.
     *
     *  @param node The rvi node
     *
     *  @param serviceIdentifier The service that failed to invoke
     *
     *  @param reason The reason the service invocation failed
     */
    void nodeSendServiceInvocationFailed(RVIRemoteNode node, String serviceIdentifier, Throwable reason);

    /**
     * Called when the remote RVI node sent a service invocation to the local RVI node.
     *
     *  @param node The rvi node
     *
     *  @param serviceIdentifier The service that was invoked
     *
     *  @param parameters The parameters received in the invocation
     */
    void nodeReceiveServiceInvocationSucceeded(RVIRemoteNode node, String serviceIdentifier, Object parameters);

    /**
     * Called when the remote RVI node tried to send a service invocation to the local RVI node, but it failed.
     *
     *  @param node The rvi node
     *
     *  @param serviceIdentifier The service that failed to be invoked
     *
     *  @param reason The reason the service invocation failed
     */
    void nodeReceiveServiceInvocationFailed(RVIRemoteNode node, String serviceIdentifier, Throwable reason);

    /**
     * Called when new local services become authorized for the remote RVI node to invoke.
     *
     *  @param node The rvi node
     *
     *  @param serviceIdentifiers The list of local service identifiers that are authorized
     */
    void nodeDidAuthorizeLocalServices(RVIRemoteNode node, ArrayList<String> serviceIdentifiers);

    /**
     * Called when new remote services become authorized for the local RVI node to invoke.
     *
     *  @param node The rvi node
     *
     *  @param serviceIdentifiers The list of local service identifiers that are authorized
     */
    void nodeDidAuthorizeRemoteServices(RVIRemoteNode node, ArrayList<String> serviceIdentifiers);
}
