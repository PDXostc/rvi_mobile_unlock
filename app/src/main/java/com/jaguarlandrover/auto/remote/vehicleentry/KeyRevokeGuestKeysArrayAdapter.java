package com.jaguarlandrover.auto.remote.vehicleentry;
/**
 * Copyright (C) 2015, Jaguar Land Rover
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0.  The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 */

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
public class KeyRevokeGuestKeysArrayAdapter extends ArrayAdapter<User>{
    public KeyRevokeGuestKeysArrayAdapter(Context context, ArrayList<User> remotePrivileges){
        super(context, 0, remotePrivileges);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        User user = ServerNode.getUserData();
        User guestUser = getItem(position);

        Integer selectedVehicleIndex = user.getSelectedVehicleIndex();
        Vehicle vehicle = (selectedVehicleIndex != -1) ? user.getVehicles().get(selectedVehicleIndex) : new Vehicle();

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_layout, parent, false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.name);
        TextView vehicleName = (TextView) convertView.findViewById(R.id.vehicle_name);
        TextView validFrom = (TextView) convertView.findViewById(R.id.valid_from);
        TextView validTo = (TextView) convertView.findViewById(R.id.valid_to);
        CheckBox lockUnlock = (CheckBox) convertView.findViewById(R.id.lock_unlock);
        CheckBox engineStart = (CheckBox) convertView.findViewById(R.id.engine_start);

        name.setText(guestUser != null ? guestUser.getUserName() : "");

        vehicleName.setText(vehicle.getDisplayName());
        validFrom.setText(vehicle.getValidFrom());
        validTo.setText(vehicle.getValidTo());
        lockUnlock.setChecked(vehicle.isLockUnlock());
        engineStart.setChecked(vehicle.isEngineStart());

        return convertView;
    }
}
