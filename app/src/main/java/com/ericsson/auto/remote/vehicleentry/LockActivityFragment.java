package com.ericsson.auto.remote.vehicleentry;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;


public class LockActivityFragment extends FragmentActivity {

    public LockActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_lock);
        //View view = inflater.inflate(R.layout.fragment_lock, container, false);
        Typeface fontawesome = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont.ttf");
        Button lock = (Button)findViewById(R.id.lock);
        lock.setTypeface(fontawesome);
        Button unlock = (Button)findViewById(R.id.unlock);
        unlock.setTypeface(fontawesome);
        Button trunk = (Button)findViewById(R.id.trunk);
        trunk.setTypeface(fontawesome);
        Button panic = (Button)findViewById(R.id.panic);
        panic.setTypeface(fontawesome);
        Button panic_on = (Button)findViewById(R.id.panic_on);
        panic_on.setTypeface(fontawesome);


        /*        Button start = (Button) view.findViewById(R.id.start);
        Button stop = (Button) view.findViewById(R.id.stop);*/

        unlock.setEnabled(true);
        /*start.setEnabled(true);*/
        trunk.setEnabled(true);

        //return view;
    }

    public void sendPoptrunk(View view) {
    }

    public void sendPanic(View view) {
    }

    public void sendPanicOff(View view) {
    }
}
