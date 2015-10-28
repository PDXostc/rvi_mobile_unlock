package com.jaguarlandrover.rvi;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2015 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    Util.java
 * Project: RVI SDK
 *
 * Created by Lilli Szafranski on 5/19/15.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * The type Util.
 */
public class Util
{
    private static final String TAG = "RVI:Util";

    /**
     * Gets the name of the current method on the stack.
     *
     * @return the method name
     */
    public static String getMethodName() {
        return Thread.currentThread().getStackTrace()[3].getMethodName();
    }

    /**
     * Print view information.
     *
     * @param view the view
     */
    public static void printView(View view) {
        Log.d(TAG, view.getClass().toString() + " frame:    (x:" + view.getLeft() + ", " +
                                                            "y:" + view.getTop() + ", " +
                                                            "w:" + view.getMeasuredWidth() + ", " +
                                                            "h:" + view.getMeasuredHeight() + ")");

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if(layoutParams instanceof ViewGroup.MarginLayoutParams)
            Log.d(TAG, view.getClass().toString() + " margin:   (l:" + ((ViewGroup.MarginLayoutParams)layoutParams).leftMargin + ", " +
                                                                "t:" + ((ViewGroup.MarginLayoutParams)layoutParams).topMargin + ", " +
                                                                "r:" + ((ViewGroup.MarginLayoutParams)layoutParams).rightMargin + ", " +
                                                                "b:" + ((ViewGroup.MarginLayoutParams)layoutParams).bottomMargin + ")");

        Log.d(TAG, view.getClass().toString() + " padding:  (l:" + view.getPaddingLeft() + ", " +
                                                            "t:" + view.getPaddingTop() + ", " +
                                                            "r:" + view.getPaddingRight() + ", " +
                                                            "b:" + view.getPaddingBottom() + ")");

    }

}
