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
//public class RemoteCredentialsAdapter extends ArrayAdapter<UserCredentials>{
public class RemoteCredentialsAdapter extends ArrayAdapter<User>{
    //public RemoteCredentialsAdapter(Context context, ArrayList<UserCredentials> remoteCredentials){
    public RemoteCredentialsAdapter(Context context, ArrayList<User> remoteCredentials){
        super(context, 0, remoteCredentials);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        //UserCredentials userCredentials = getItem(position);
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
