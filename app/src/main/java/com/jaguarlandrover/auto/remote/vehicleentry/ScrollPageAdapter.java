package com.jaguarlandrover.auto.remote.vehicleentry;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Objects;

/**
 * Created by rdz on 8/18/2015.
 */
class ScrollPageAdapter extends PagerAdapter {
    Context mContext;
    LayoutInflater mLayoutInflater;
    int[] mResources = null;
    public ScrollPageAdapter(Context context, int[] resources){
        mContext=context;
        mResources = resources;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount(){
        return mResources.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object){
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem (ViewGroup container, int position){
        View ItemView = mLayoutInflater.inflate(R.layout.user_scroll, container,false);
        ImageView imageView = (ImageView) ItemView.findViewById(R.id.imageView);
        imageView.setImageResource(mResources[position]);

        container.addView(ItemView);
        return ItemView;

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object){
        container.removeView((LinearLayout) object);
    }

}
