package br.com.hive.hibeacon.core.exceptions;

/**
 * Created by FBisca on 16/09/2015.
 */
public class BluetoothOfflineException extends BeaconException {

    public BluetoothOfflineException() {
        super("Bluetooth is currently disabled");
    }
}
