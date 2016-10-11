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

import java.util.IllegalFormatCodePointException;

/**
 * The type Util.
 */
class Util
{
    private static final String TAG = "RVI/Util               ";

    /**
     * Gets the name of the current method on the stack.
     *
     * @return the method name
     */
    static String getMethodName() {
        return Thread.currentThread().getStackTrace()[3].getMethodName();
    }

    /**
     * Print view information.
     *
     * @param view the view
     */
    static void printView(View view) {
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

    // TODO: Test!
    /* Strings that begin or end with '/' have the '/'s removed. Domain component can have '.'s and not '/'s. All other components can have '/'s and not '.'s. */
    static String validated(String identifierComponent, boolean isDomain) {

        /* Components can't be null and can't be empty. */
        if (identifierComponent == null) throw new IllegalArgumentException("Component can't be null.");
        if (identifierComponent.equals("")) throw new IllegalArgumentException("Component can't be an empty string.");

        // TODO: Validation for reserved services; service_component_parts with '$' or something; maybe check against known list
        // TODO: Validation for domain component: check correctness of '.' usage (starting, multiple, repeating, etc.)
        // TODO: If we decide to allow '.'s for non-domain components, update
        // TODO: If we decide that components can't start with '_' or '-', have to split on '/' and check that too

        /* Whitespace isn't allowed. */
        String regex = "\\s";
        boolean hasWhiteSpace = identifierComponent.matches(regex);

        if (hasWhiteSpace)
            throw new IllegalArgumentException("Component \"" + identifierComponent + "\" contains a white-space character. Only the following characters are allowed: a-z, A-Z, 0-9, '-', '_', '.', and '/'.");

        /* Only the following characters are allowed: a-z, A-Z, 0-9, '-', '_', '.' (for domains), and '/' (for non-domains). */
        if (isDomain)
            regex = "^[a-zA-Z0-9-_\\.]+$";
        else
            regex = "^[a-zA-Z0-9-_/]+$";

        boolean hasSpecialChar = !identifierComponent.matches(regex);

        if (hasSpecialChar)
            throw new IllegalArgumentException("Component \"" + identifierComponent + "\" contains an illegal character. Only the following characters are allowed: a-z, A-Z, 0-9, '-', '_', '.', and '/'.");

        /* Look for repeating '/'s. */
        regex = "/\1{2,}";
        boolean hasRepeatingSlash = identifierComponent.matches(regex);

        if (hasRepeatingSlash)
            throw new IllegalArgumentException("Component \"" + identifierComponent + "\" contains an illegal character sequence: two or more '/'s in a row.");

        /* Trim leading and trailing '/'s for non-domain components. (If it was a domain, they already would have thrown an exception.) */
        return identifierComponent.replaceAll("^/", "").replaceAll("/$", "");
    }
}
