package com.jaguarlandrover.auto.remote.vehicleentry.wear;
/**
 * Copyright (C) 2015, Jaguar Land Rover
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0.  The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 */

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by stoffe on 6/23/15.
 */
public class WatchDisplayActivity extends Activity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        mTextView = (TextView) findViewById(R.id.text);
        mTextView.setText("About");
    }
}
