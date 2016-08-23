package com.jaguarlandrover.auto.remote.vehicleentry;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    private Button mVerifyButton;
    public MaterialEditText mEmail;

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
        mVerifyButton = (Button)view.findViewById(R.id.verifyButton);
        mEmail = (MaterialEditText) view.findViewById(R.id.email);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mEmail.setText(sharedPref.getString("savedEmail", ""));

        mEmail.setCursorVisible(true);

        mVerifyButton.setOnClickListener(l);
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
            SharedPreferences.Editor ed = sharedPref.edit();

            ed.putString("savedEmail", mEmail.getText().toString());

            ed.commit();

            switch(v.getId()) {
                case R.id.verifyButton:
                    Log.i("RVI", "Verify button clicked.");
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
