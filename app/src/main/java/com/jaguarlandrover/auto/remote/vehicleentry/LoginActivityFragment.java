package com.jaguarlandrover.auto.remote.vehicleentry;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.rengwuxian.materialedittext.MaterialEditText;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class LoginActivityFragment extends Fragment {

    //private OnFragmentInteractionListener mListener;
    private Button login;
    public MaterialEditText userName;
    public MaterialEditText password;
    private LoginFragmentButtonListener buttonListener;
    private SharedPreferences sharedPref;
    private ImageView logo;
    public LoginActivityFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_activity, container, false);
        Typeface fontawesome = Typeface.createFromAsset(getActivity().getAssets(), "fonts/fontawesome-webfont.ttf");
        login = (Button)view.findViewById(R.id.loginBtn);
//        login.setTypeface(fontawesome);
        userName = (MaterialEditText) view.findViewById(R.id.username);
        password = (MaterialEditText) view.findViewById(R.id.password);

        login.setOnClickListener(l);
            /*@Override
            public void onClick(View v) {
                submit(v);
            }
        });*/
        buttonListener = (LoginFragmentButtonListener) getActivity();
        // Inflate the layout for this fragment
        return view;
    }

    private View.OnClickListener l = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //SharedPreferences.Editor ed = sharedPref.edit();
            switch(v.getId()) {
                case R.id.loginBtn:
                    Log.i("RVI", "LockBtn");
                    //ed.putBoolean("Share", true);
                    buttonListener.onButtonCommand(v);
                    break;
            }

        }
    };
    public void onNewServiceDiscovered(String... service) {
        for(String s:service)
            Log.e("RVI", "Service = " + s);
    }

    public interface LoginFragmentButtonListener {
        public void onButtonCommand(View v);
    }
}
