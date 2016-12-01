package org.genivi.rvi;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Copyright (c) 2016 Jaguar Land Rover.
 *
 * This program is licensed under the terms and conditions of the
 * Mozilla Public License, version 2.0. The full text of the
 * Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
 *
 * File:    PrivilegeManager.java
 * Project: RVI
 *
 * Created by Lilli Szafranski on 10/4/16.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import java.util.ArrayList;

/**
 * Singleton class that handles some privilege related stuff like tracking expiration and stuff.
 */
class PrivilegeManager {
    private final static String TAG = "RVI/PrivilegeManager___";

    private static PrivilegeManager ourInstance = new PrivilegeManager();

    private static Long nextRevalidationRemotePrivileges = 0L;

    private static Long nextRevalidationLocalPrivileges = 0L;

    private PrivilegeManager() {
    }

    static ArrayList<String> toPrivilegeStringArray(ArrayList<Privilege> privilegeObjects) {
        ArrayList<String> privilegeStrings = new ArrayList<>();

        if (privilegeObjects != null)
            for (Privilege privilege : privilegeObjects)
                privilegeStrings.add(privilege.getJwt());

        return privilegeStrings;
    }

    static ArrayList<Privilege> fromPrivilegeStringArray(ArrayList<String> privilegeStrings) {
        ArrayList<Privilege> privilegeObjects = new ArrayList<>();

        if (privilegeStrings != null)
            for (String privilege : privilegeStrings)
                privilegeObjects.add(new Privilege(privilege));

        return privilegeObjects;
    }

    static Boolean remotePrivilegesRevalidationNeeded() {
        if (PrivilegeManager.nextRevalidationRemotePrivileges == 0L) return true;

        Long currentTime = System.currentTimeMillis() / 1000;

        if (currentTime > PrivilegeManager.nextRevalidationRemotePrivileges) {
            PrivilegeManager.nextRevalidationRemotePrivileges = 0L;
            return true;
        }

        return false;
    }

    static void updateRemotePrivilegesRevalidationTime(Long time) {
        if (time < PrivilegeManager.nextRevalidationRemotePrivileges || PrivilegeManager.nextRevalidationRemotePrivileges == 0L && time != 0L)
            PrivilegeManager.nextRevalidationRemotePrivileges = time;
    }

    static void clearRemotePrivilegesRevalidationTime() {
        PrivilegeManager.nextRevalidationRemotePrivileges = 0L;
    }

    static Boolean localPrivilegesRevalidationNeeded() {
        if (PrivilegeManager.nextRevalidationLocalPrivileges == 0L) return true;

        Long currentTime = System.currentTimeMillis() / 1000;

        if (currentTime > PrivilegeManager.nextRevalidationLocalPrivileges) {
            PrivilegeManager.nextRevalidationLocalPrivileges = 0L;
            return true;
        }

        return false;
    }

    static void updateLocalPrivilegesRevalidationTime(Long time) {
        if (time < PrivilegeManager.nextRevalidationLocalPrivileges || PrivilegeManager.nextRevalidationLocalPrivileges == 0L && time != 0L)
            PrivilegeManager.nextRevalidationLocalPrivileges = time;
    }

    static void clearLocalPrivilegesRevalidationTime() {
        PrivilegeManager.nextRevalidationLocalPrivileges = 0L;
    }
}
