package com.jaguarlandrover.auto.remote.vehicleentry;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

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
        TextView vehicle = (TextView) convertView.findViewById(R.id.vehicle);
        TextView valid_from = (TextView) convertView.findViewById(R.id.validfrom);
        TextView valid_to = (TextView) convertView.findViewById(R.id.validto);
        CheckBox lock_unlock = (CheckBox) convertView.findViewById(R.id.lock_unlock);
        CheckBox engine_start = (CheckBox) convertView.findViewById(R.id.enginestart);

        name.setText(user.username);
        vehicle.setText(user.vehicle);
        valid_from.setText(user.validfrom);
        valid_to.setText(user.validto);
        lock_unlock.setChecked(user.lock_unlock);
        engine_start.setChecked(user.enginestart);

        return convertView;
    }



}
