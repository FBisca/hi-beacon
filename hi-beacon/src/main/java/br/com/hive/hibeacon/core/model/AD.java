package br.com.hive.hibeacon.core.model;

import java.util.UUID;

/**
 * Created by FBisca on 16/09/2015.
 */
public class AD {

    private ADType type;
    private int manufacterId;
    private int beaconPrefix;
    private UUID uuid;
    private int major;
    private int minor;
    private int txPower;
    private byte[] data;

    public ADType getType() {
        return type;
    }

    public void setType(ADType type) {
        this.type = type;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getManufacterId() {
        return manufacterId;
    }

    public void setManufacterId(int manufacterId) {
        this.manufacterId = manufacterId;
    }

    public int getBeaconPrefix() {
        return beaconPrefix;
    }

    public void setBeaconPrefix(int beaconPrefix) {
        this.beaconPrefix = beaconPrefix;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public int getTxPower() {
        return txPower;
    }

    public void setTxPower(int txPower) {
        this.txPower = txPower;
    }

    public enum ADType {
        FLAG, DATA, UNKNOWN
    }

}
