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
    UNKNOWN("unknown"),
    NO_KEYS_GENERATED("no_keys_generated"),
    KEYS_GENERATED_CSR_SENT("keys_generated_csr_sent"),
    CSR_VALIDATION_SENT("csr_validation_sent"),
    CSR_VALIDATED_CERTS_RECEIVED("csr_validated_certs_received"),
    OTHER("other");

    private final String mState;

    PKIManagerState(String state) {
        mState = state;
    }

    public final String value() {
        return mState;
    }

    public static PKIManagerState get(String state) {
        switch (state) {
            case "no_keys_generated":            return NO_KEYS_GENERATED;
            case "keys_generated_csr_sent":      return KEYS_GENERATED_CSR_SENT;
            case "csr_validation_sent":          return CSR_VALIDATION_SENT;
            case "csr_validated_certs_received": return CSR_VALIDATED_CERTS_RECEIVED;
            case "other":                        return OTHER;
        }

        return UNKNOWN;
    }
}
