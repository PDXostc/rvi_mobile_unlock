package com.jaguarlandrover.pki;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2016 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    PKIManagerState.java
 * Project: UnlockDemo
 *
 * Created by Lilli Szafranski on 9/27/16.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

public enum PKIManagerState
{
    UNKNOWN,
    NO_KEYS_OR_CERTIFICATE_GENERATED,
    KEYS_AND_CERTIFICATE_GENERATED,
    CERTIFICATE_SIGNING_REQUEST_SENT_VERIFICATION_REQUIRED,
    CERTIFICATE_SIGNING_REQUEST_SENT_NO_VERIFICATION_REQUIRED,
    EXTRA_VERIFICATION_SENT,
    SIGNED_CERTIFICATES_RECEIVED,
    OTHER;
}

//public enum PKIManagerState
//{
//    UNKNOWN("unknown"),
//    NO_KEYS_OR_CERTIFICATE_GENERATED("no_keys_or_certificate_generated"),
//    KEYS_AND_CERTIFICATE_GENERATED("keys_and_certificate_generated"),
//    CERTIFICATE_SIGNING_REQUEST_SENT_VERIFICATION_REQUIRED("certificate_signing_request_sent_verification_required"),
//    CERTIFICATE_SIGNING_REQUEST_SENT_NO_VERIFICATION_REQUIRED("certificate_signing_request_sent_no_verification_required"),
//    EXTRA_VERIFICATION_SENT("extra_verification_sent"),
//    SIGNED_CERTIFICATES_RECEIVED("signed_certificates_received"),
//    OTHER("other");
//
//    private final String mState;
//
//    PKIManagerState(String state) {
//        mState = state;
//    }
//
//    public final String value() {
//        return mState;
//    }
//
//    public static PKIManagerState get(String state) {
//        switch (state) {
//            case "no_keys_or_certificate_generated":                          return NO_KEYS_OR_CERTIFICATE_GENERATED;
//            case "keys_and_certificate_generated":                            return KEYS_AND_CERTIFICATE_GENERATED;
//            case "certificate_signing_request_sent_verification_required":    return CERTIFICATE_SIGNING_REQUEST_SENT_VERIFICATION_REQUIRED;
//            case "certificate_signing_request_sent_no_verification_required": return CERTIFICATE_SIGNING_REQUEST_SENT_NO_VERIFICATION_REQUIRED;
//            case "extra_verification_sent":                                   return EXTRA_VERIFICATION_SENT;
//            case "signed_certificates_received":                              return SIGNED_CERTIFICATES_RECEIVED;
//            case "other":                                                     return OTHER;
//        }
//
//        return UNKNOWN;
//    }
//}
