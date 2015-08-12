package com.jaguarlandrover.auto.remote.vehicleentry;

/**
 * Created by rdz on 8/12/2015.
 */
public class User {
    public String username;
    public String vehicle;
    public String validfrom;
    public String validto;
    public boolean lock_unlock;
    public boolean enginestart;

    public User(String username, String vehicle, String validfrom, String validto,
                boolean lock_unlock, boolean enginestart){
        this.username = username;
        this.vehicle = vehicle;
        this.validfrom = validfrom;
        this.validto = validto;
        this.lock_unlock = lock_unlock;
        this.enginestart = enginestart;
    }

}
