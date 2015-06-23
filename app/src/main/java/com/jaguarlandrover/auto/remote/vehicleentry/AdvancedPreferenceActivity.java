package com.jaguarlandrover.auto.remote.vehicleentry;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;

/**
 * Created by stoffe on 6/20/15.
 */
public class AdvancedPreferenceActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        AdvancedPrefsFragment advancedPrefsFragment = new AdvancedPrefsFragment();
        fragmentTransaction.replace(android.R.id.content, advancedPrefsFragment);
        fragmentTransaction.commit();
    }
}
