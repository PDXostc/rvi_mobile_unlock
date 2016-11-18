package org.genivi.rvitest;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

    protected TextView testNameLabel;
    protected TextView testResultLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_set_base);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        testNameLabel   = (TextView) this.findViewById(R.id.test_name_label);
        testResultLabel = (TextView) this.findViewById(R.id.test_result_label);

        testButton1 .setOnClickListener(buttonListener);
        testButton2 .setOnClickListener(buttonListener);
        testButton3 .setOnClickListener(buttonListener);
        testButton4 .setOnClickListener(buttonListener);
        testButton5 .setOnClickListener(buttonListener);
        testButton6 .setOnClickListener(buttonListener);
        testButton7 .setOnClickListener(buttonListener);
        testButton8 .setOnClickListener(buttonListener);
        testButton9 .setOnClickListener(buttonListener);
        testButton10.setOnClickListener(buttonListener);
        testButton11.setOnClickListener(buttonListener);
        testButton12.setOnClickListener(buttonListener);
    }

    Button.OnClickListener buttonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if      (view == testButton1 ) runTest1 ("Running test 1");
            else if (view == testButton2 ) runTest2 ("Running test 2");
            else if (view == testButton3 ) runTest3 ("Running test 3");
            else if (view == testButton4 ) runTest4 ("Running test 4");
            else if (view == testButton5 ) runTest5 ("Running test 5");
            else if (view == testButton6 ) runTest6 ("Running test 6");
            else if (view == testButton7 ) runTest7 ("Running test 7");
            else if (view == testButton8 ) runTest8 ("Running test 8");
            else if (view == testButton9 ) runTest9 ("Running test 9");
            else if (view == testButton10) runTest10("Running test 10");
            else if (view == testButton11) runTest11("Running test 11");
            else if (view == testButton12) runTest12("Running test 12");
        }
    };

    private void unimplemented() {
        testResultLabel.setText("Test Unimplemented");
    }

    protected void runTest1(String testName) {
        Log.d(TAG, "runTest1");

        testNameLabel.setText(testName);

        unimplemented();
    }

    protected void runTest2(String testName) {
        Log.d(TAG, "runTest2");

        testNameLabel.setText(testName);

        unimplemented();
    }

    protected void runTest3(String testName) {
        testNameLabel.setText(testName);

        unimplemented();
    }

    protected void runTest4(String testName) {
        testNameLabel.setText(testName);

        unimplemented();
    }

    protected void runTest5(String testName) {
        testNameLabel.setText(testName);

        unimplemented();
    }

    protected void runTest6(String testName) {
        testNameLabel.setText(testName);

        unimplemented();
    }

    protected void runTest7(String testName) {
        testNameLabel.setText(testName);

        unimplemented();
    }

    protected void runTest8(String testName) {
        testNameLabel.setText(testName);

        unimplemented();
    }

    protected void runTest9(String testName) {
        testNameLabel.setText(testName);

        unimplemented();
    }

    protected void runTest10(String testName) {
        testNameLabel.setText(testName);

        unimplemented();
    }

    protected void runTest11(String testName) {
        testNameLabel.setText(testName);

        unimplemented();
    }

    protected void runTest12(String testName) {
        testNameLabel.setText(testName);

        unimplemented();
    }
}
