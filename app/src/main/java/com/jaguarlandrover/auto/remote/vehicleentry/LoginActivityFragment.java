package com.jaguarlandrover.auto.remote.vehicleentry;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class LoginActivityFragment extends Fragment {

    private MaterialEditText mEmail;
    private Button           mVerifyButton;
    private TextView         mStatusText;
    private ImageView        mHasKeysImageView;
    private ImageView        mHasSignedCertsImageView;

    private LoginFragmentButtonListener mLoginFragmentButtonListener;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View     view        = inflater.inflate(R.layout.fragment_login_activity, container, false);
        Typeface fontawesome = Typeface.createFromAsset(getActivity().getAssets(), "fonts/fontawesome-webfont.ttf");

        mEmail                   = (MaterialEditText) view.findViewById(R.id.email);
        mVerifyButton            = (Button) view.findViewById(R.id.verifyButton);
        mStatusText              = (TextView) view.findViewById(R.id.statusText);
        mHasKeysImageView        = (ImageView) view.findViewById(R.id.has_keys_icon);
        mHasSignedCertsImageView = (ImageView) view.findViewById(R.id.has_signed_certs_icon);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mEmail.setText(sharedPref.getString("savedEmail", ""));

        mEmail.setCursorVisible(true);

        mVerifyButton.setOnClickListener(mOnClickListener);

        mLoginFragmentButtonListener = (LoginFragmentButtonListener) getActivity();

        return view;
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mEmail.getWindowToken(), 0);

            SharedPreferences.Editor ed = sharedPref.edit();
            ed.putString("savedEmail", mEmail.getText().toString());
            ed.commit();

            switch (view.getId()) {
                case R.id.verifyButton:
                    Log.i("RVI", "Verify button clicked.");

                    mLoginFragmentButtonListener.onButtonCommand(view);
                    break;
            }
        }
    };

    public void onNewServiceDiscovered(String... service) {
        for(String s : service)
            Log.e("RVI", "Service = " + s);
    }

    public interface LoginFragmentButtonListener {
        void onButtonCommand(View v);
    }

    void setVerifyButtonText(String text) {
        mVerifyButton.setText(text);
    }

    void setStatusTextText(String text) {
        mStatusText.setText(text);
    }

    void setVerifyButtonEnabled(Boolean enabled) {
        mVerifyButton.setEnabled(enabled);
    }

    void hideControls(Boolean hidden) {
        if (hidden) {
            mVerifyButton.setVisibility(View.GONE);
            mEmail.setVisibility(View.GONE);
        } else {
            mVerifyButton.setVisibility(View.VISIBLE);
            mEmail.setVisibility(View.VISIBLE);
        }
    }

    void setHasKeys(boolean hasKeys) {
        if (hasKeys)
            mHasKeysImageView.setImageResource(R.drawable.key_on);
        else
            mHasKeysImageView.setImageResource(R.drawable.key_off);
    }

    void setHasSignedCerts(boolean hasSignedCerts) {
        if (hasSignedCerts)
            mHasSignedCertsImageView.setImageResource(R.drawable.cert_on);
        else
            mHasSignedCertsImageView.setImageResource(R.drawable.cert_off);
    }
}
