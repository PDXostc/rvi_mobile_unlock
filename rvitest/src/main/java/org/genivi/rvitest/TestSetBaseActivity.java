package org.genivi.rvitest;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Set;

public class TestSetBaseActivity extends AppCompatActivity {
    private static final String TAG = "RVITest/TestSetBaseActv";

    protected ArrayList<String> localCredentials  = null;
    protected ArrayList<String> remoteCredentials = null;

    protected Set<String> expectedLocalServices  = null;
    protected Set<String> expectedRemoteServices = null;

    protected Button testButton1;
    protected Button testButton2;
    protected Button testButton3;
    protected Button testButton4;
    protected Button testButton5;
    protected Button testButton6;
    protected Button testButton7;
    protected Button testButton8;
    protected Button testButton9;
    protected Button testButton10;
    protected Button testButton11;
    protected Button testButton12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_set_base);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        testButton1  = (Button) this.findViewById(R.id.test_button_1 );
        testButton2  = (Button) this.findViewById(R.id.test_button_2 );
        testButton3  = (Button) this.findViewById(R.id.test_button_3 );
        testButton4  = (Button) this.findViewById(R.id.test_button_4 );
        testButton5  = (Button) this.findViewById(R.id.test_button_5 );
        testButton6  = (Button) this.findViewById(R.id.test_button_6 );
        testButton7  = (Button) this.findViewById(R.id.test_button_7 );
        testButton8  = (Button) this.findViewById(R.id.test_button_8 );
        testButton9  = (Button) this.findViewById(R.id.test_button_9 );
        testButton10 = (Button) this.findViewById(R.id.test_button_10);
        testButton11 = (Button) this.findViewById(R.id.test_button_11);
        testButton12 = (Button) this.findViewById(R.id.test_button_12);
    }
}
