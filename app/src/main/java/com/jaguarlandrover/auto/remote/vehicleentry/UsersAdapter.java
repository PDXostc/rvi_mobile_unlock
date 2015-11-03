package com.jaguarlandrover.auto.remote.vehicleentry;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by rdz on 8/13/2015.
 */
public class UsersAdapter extends ArrayAdapter<User>{
    public UsersAdapter(Context context, ArrayList<User> users){
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        User user = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_layout, parent, false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.name);
        TextView vehicleName = (TextView) convertView.findViewById(R.id.vehicle_name);
        TextView validFrom = (TextView) convertView.findViewById(R.id.valid_from);
        TextView validTo = (TextView) convertView.findViewById(R.id.valid_to);
        CheckBox lockUnlock = (CheckBox) convertView.findViewById(R.id.lock_unlock);
        CheckBox engineStart = (CheckBox) convertView.findViewById(R.id.engine_start);

        name.setText(user.getUserName());
        vehicleName.setText(user.getVehicleName());
        validFrom.setText(user.getValidFrom());
        validTo.setText(user.getValidTo());
        lockUnlock.setChecked(user.isLockUnlock());
        engineStart.setChecked(user.isEngineStart());

        return convertView;
    }
}
