package com.ericsson.auto.remote.vehicleentry;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A placeholder fragment containing a simple view.
 */
public class LockActivityFragment extends Fragment {
    public static final String STOPPED_LBL="StartStop";
    public static final String LOCKED_LBL="OpenClose";

    private Button lock;
    private Button unlock;
    private Button start;
    private Button stop;
    private Button trunk;
    private Button panic;

    //Temp button press storage
    private SharedPreferences sharedPref;

    public LockActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lock, container, false);

        lock = (Button) view.findViewById(R.id.lock);
        unlock = (Button) view.findViewById(R.id.unlock);
        start = (Button) view.findViewById(R.id.start);
        stop = (Button) view.findViewById(R.id.stop);
        trunk = (Button) view.findViewById(R.id.trunk);
        panic = (Button) view.findViewById(R.id.panic);

        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

        lock.setOnClickListener(l);
        unlock.setOnClickListener(l);
        start.setOnClickListener(l);
        stop.setOnClickListener(l);
        trunk.setOnClickListener(l);
        panic.setOnClickListener(l);

        return view;
    }

    public void onViewStateRestored (Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        //Assume auto unlock
        sharedPref.edit().putBoolean(LOCKED_LBL, false).commit();
        //assume stopped
        sharedPref.edit().putBoolean(STOPPED_LBL, true).commit();

        toggleButtonsFromPref();
    }


    private View.OnClickListener l = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SharedPreferences.Editor ed = sharedPref.edit();
            switch(v.getId()) {
                case R.id.lock:
                    Log.i("STOFFE","LockBtn");
                    ed.putBoolean(LOCKED_LBL,true);
                    break;
                case R.id.unlock:
                    Log.i("STOFFE","UnlockBtn");
                    ed.putBoolean(LOCKED_LBL, false);
                    break;
                case R.id.start:
                    Log.i("STOFFE","StartBtn");
                    ed.putBoolean(STOPPED_LBL, false);
                    break;
                case R.id.stop:
                    Log.i("STOFFE","StopBtn");
                    ed.putBoolean(STOPPED_LBL,true);
                    break;
                case R.id.trunk:Log.i("STOFFE","TrunkBtn");break;
                case R.id.panic:Log.i("STOFFE","PanicBtn");break;
            }
            ed.apply();
            ed.commit();

            toggleButtonsFromPref();
        }
    };

    private void toggleButtonsFromPref() {

        boolean locked = sharedPref.getBoolean(LOCKED_LBL, true);
        boolean stopped = sharedPref.getBoolean(STOPPED_LBL,true);

        lock.setEnabled(!locked);
        unlock.setEnabled(locked);

        start.setEnabled(stopped);
        stop.setEnabled(!stopped);
        trunk.setEnabled(true);
    }

    public void onNewServiceDiscovered(String... service) {
        for(String s:service)
            Log.e("Stoffe", "Service = " + s);
    }

    public interface LockFragmentButtonListener {
        public void onButtonCommand(String cmd);
    }
}
