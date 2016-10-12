package com.jaguarlandrover.auto.remote.vehicleentry;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by rdz on 8/18/2015.
 */
class ScrollPageAdapter extends PagerAdapter
{
    Context mContext;
    LayoutInflater mLayoutInflater;
    List<String> mGuestUsers = null;
    public ScrollPageAdapter(Context context, List<String> guestUsers){
        mContext = context;
        mGuestUsers = guestUsers;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount(){
        return mGuestUsers.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object){
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem (ViewGroup container, int position){
        View itemView = mLayoutInflater.inflate(R.layout.user_scroll, container,false);
//        ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView);
//        imageView.setImageResource(mGuestUsers[position]);

        TextView textView = (TextView) itemView.findViewById(R.id.textView);
        textView.setText(mGuestUsers.get(position));

        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object){
        container.removeView((LinearLayout) object);
    }
}
