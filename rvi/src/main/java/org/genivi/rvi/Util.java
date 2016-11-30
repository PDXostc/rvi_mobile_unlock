package org.genivi.rvi;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2015 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    Util.java
 * Project: RVI
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
    private static final String TAG = "RVI/Util_______________";

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

    static String rfc1035(String domain) {

        /* Domains can't be null and can't be empty. */
        if (domain == null) throw new IllegalArgumentException("Domain can't be null.");
        if (domain.equals("")) throw new IllegalArgumentException("Domain can't be an empty string.");

        // TODO: Validation for reserved services; service_component_parts with '$' or something; maybe check against known list

        /* Whitespace isn't allowed. */
        String regex = "\\s";
        boolean hasWhiteSpace = domain.matches(regex);

        if (hasWhiteSpace)
            throw new IllegalArgumentException("Domain \"" + domain + "\" contains a white-space character. Per the RFC1035 specification, only the following characters are allowed: a-z, A-Z, 0-9, '.', and '-'.");

        /* Only the following characters are allowed: a-z, A-Z, 0-9, '.', and '-'. */
        regex = "^[a-zA-Z0-9-\\.]+$";

        boolean hasSpecialChar = !domain.matches(regex);

        if (hasSpecialChar)
            throw new IllegalArgumentException("Domain \"" + domain + "\" contains an illegal character. Per the RFC1035 specification, only the following characters are allowed: a-z, A-Z, 0-9, '.', and '-'.");

        /* Check that the domain does not begin or end with a period */
        if (domain.startsWith("."))
            throw new IllegalArgumentException("Domain \"" + domain + "\" cannot begin with a '.' character, per the RFC1035 specification.");

        if (domain.endsWith("."))
            throw new IllegalArgumentException("Domain \"" + domain + "\" cannot begin with a '.' character, per the RFC1035 specification.");

        /* Check that each label of the domain is at least one character and that the first character is a letter. */
        String[] domainLabels = domain.split(".");

        for (String domainLabel : domainLabels) {
            if (domainLabel.equals(""))
                throw new IllegalArgumentException("Domain \"" + domain + "\" contains an empty label/subdomain.");

            regex = "^[a-zA-Z]";

            boolean startsWithNonLetter = !domainLabel.matches(regex);

            if (startsWithNonLetter)
                throw new IllegalArgumentException("Domain \"" + domain + "\" contains a label/subdomain that begins with a non-letter: \"" + domainLabel + "\". Per the RFC1035 specification, labels/subdomains must begin with a letter.");
        }

        return domain;
    }

    // TODO: Test!
    /* An identifierComponent can have multiple topics in one string, e.g., "foo/bar/baz", but can't have empty topics, e.g., "foo//bar/baz", or begin
       or end with a topic separator string, e.g., "/foo/bar/baz/'. They can't be empty or null. They can't contain illegal characters: '+' and '#'.
       Topics that begin with the '$' are reserved, but we aren't checking that at this point. */
    static String validated(String identifierComponent) {

        /* Components can't be null and can't be empty. */
        if (identifierComponent == null) throw new IllegalArgumentException("Component can't be null.");
        if (identifierComponent.equals("")) throw new IllegalArgumentException("Component can't be an empty string.");

//        /* Whitespace isn't allowed. */
//        String regex = "\\s";
//        boolean hasWhiteSpace = identifierComponent.matches(regex);
//
//        if (hasWhiteSpace)
//            throw new IllegalArgumentException("Component \"" + identifierComponent + "\" contains a white-space character. Only the following characters are allowed: a-z, A-Z, 0-9, '-', '_', '.', and '/'.");

//        /* Only the following characters are allowed: a-z, A-Z, 0-9, '-', '_', '.' (for domains), and '/' (for non-domains). */
//        if (isDomain)
//            regex = "^[a-zA-Z0-9-_\\.]+$";
//        else
//            regex = "^[a-zA-Z0-9-_/]+$";
//
//        boolean hasSpecialChar = !identifierComponent.matches(regex);
//
//        if (hasSpecialChar)
//            throw new IllegalArgumentException("Component \"" + identifierComponent + "\" contains an illegal character. Only the following characters are allowed: a-z, A-Z, 0-9, '-', '_', '.', and '/'.");

        /* Look for repeating '/'s. */
        String regex = "/\1{2,}";
        boolean hasRepeatingSlash = identifierComponent.matches(regex);

        if (hasRepeatingSlash)
            throw new IllegalArgumentException("Component \"" + identifierComponent + "\" contains an illegal character sequence: two or more '/'s in a row.");

        /* Split the string by the topic separator character, and check each bit to make sure it's legal */
        /* Check that each label of the domain is at least one character and that the first character is a letter. */
        String[] topicLevels = identifierComponent.split("/");

        for (String topicLevel : topicLevels) {
            if (topicLevel.equals("")) /* This should have already been caught above... */
                throw new IllegalArgumentException("Identifier component \"" + identifierComponent + "\" contains an empty topic level.");

            if (topicLevel.contains("+"))
                throw new IllegalArgumentException("Identifier component \"" + identifierComponent + "\" contains an illegal character: '+'.");

            if (topicLevel.contains("#"))
                throw new IllegalArgumentException("Identifier component \"" + identifierComponent + "\" contains an illegal character: '#'.");

            // TODO: Validation for reserved services; service_component_parts with '$' or something; maybe check against known list
        }

        return identifierComponent;
    }
}
