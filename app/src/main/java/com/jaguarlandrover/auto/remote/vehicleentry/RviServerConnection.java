/**
 *  Copyright (C) 2015, Jaguar Land Rover
 *
 *  This program is licensed under the terms and conditions of the
 *  Mozilla Public License, version 2.0.  The full text of the
 *  Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 */

package com.jaguarlandrover.auto.remote.vehicleentry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class RviServerConnection extends RviProtocol {
    private static final String TAG = "RVI";

    Socket socket;
    public RviServerConnection (String host, int port) {
        try {
            socket = new Socket(host,port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public void connect() {
//        socket.connect();
//    }


    public void disconnect() throws IOException {
        if( socket != null )
            socket.close();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if( socket == null ) throw new IOException("Socket to RVI not open");
        return socket.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if( socket == null ) throw new IOException("Socket to RVI not open");
        return socket.getOutputStream();
    }
}
