package com.jaguarlandrover.auto.remote.vehicleentry.wear;

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
