package com.ericsson.auto.remote.vehicleentry;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A placeholder fragment containing a simple view.
 */
public class LockActivityFragment extends Fragment {

    public LockActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lock, container, false);

        Button lock = (Button) view.findViewById(R.id.lock);
        Button unlock = (Button) view.findViewById(R.id.unlock);
        Button start = (Button) view.findViewById(R.id.start);
        Button stop = (Button) view.findViewById(R.id.stop);
        Button trunk = (Button) view.findViewById(R.id.trunk);
        Button panic = (Button) view.findViewById(R.id.panic);

        unlock.setEnabled(true);
        start.setEnabled(true);
        trunk.setEnabled(true);

        return view;
    }
}
