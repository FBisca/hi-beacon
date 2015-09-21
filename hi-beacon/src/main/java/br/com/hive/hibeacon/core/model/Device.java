package br.com.hive.hibeacon.core.model;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;
import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.UUID;

import br.com.hive.hibeacon.bluetooth.BeaconManager;
import br.com.hive.hibeacon.core.api.SendBeaconTask;
import br.com.hive.hibeacon.core.interfaces.OfferListener;
import br.com.hive.hibeacon.core.parsers.BeaconParser;

/**
 * Created by FBisca on 16/09/2015.
 */
public class Device implements Parcelable {

    public static final Creator<Device> CREATOR = new DeviceCreator();

    private String mName;
    private String mAddress;
    private BluetoothDevice mBluetoohDevice;
    private int rssi;
    private byte[] scanRecord;
    private ArrayList<AD> adList = new ArrayList<>();

    private SendBeaconTask mTask;

    private Device() {
        super();
    }

    public Device(Context context, BluetoothDevice mBluetoohDevice, int rssi, byte[] scanRecord) {
        this.mName = mBluetoohDevice.getName();
        this.mAddress = mBluetoohDevice.getAddress();
        this.mBluetoohDevice = mBluetoohDevice;
        this.scanRecord = scanRecord;
        this.rssi = rssi;

        this.adList = BeaconManager.getInstance(context).getBeaconParser().parseScanRecord(this.scanRecord);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Device(Context context, BluetoothDevice mBluetoohDevice, int rssi, ScanRecord scanRecord) {
        this.mName = mBluetoohDevice.getName();
        this.mAddress = mBluetoohDevice.getAddress();
        this.mBluetoohDevice = mBluetoohDevice;
        this.rssi = rssi;

        if (scanRecord != null) {
            this.scanRecord = scanRecord.getBytes();
        }

        this.adList = BeaconManager.getInstance(context).getBeaconParser().parseScanRecord(this.scanRecord);
    }


    public BluetoothDevice getBluetoohDevice() {
        return mBluetoohDevice;
    }

    public int getRssi() {
        return rssi;
    }

    public byte[] getScanRecord() {
        return scanRecord;
    }

    public ArrayList<AD> getAdList() {
        return adList;
    }

    public UUID getUUID() {
        if (adList == null) {
            return null;
        }

        for (AD ad : adList) {
            if (ad.getUuid() != null) {
                return ad.getUuid();
            }
        }

        return null;
    }

    public int getMajor() {
        if (adList == null) {
            return 0;
        }

        for (AD ad : adList) {
            if (ad.getMajor() != 0) {
                return ad.getMajor();
            }
        }

        return 0;
    }

    public int getMinor() {
        if (adList == null) {
            return 0;
        }

        for (AD ad : adList) {
            if (ad.getMinor() != 0) {
                return ad.getMinor();
            }
        }

        return 0;
    }

    public String getName() {
        return mName;
    }

    public String getAddress() {
        return mAddress;
    }

    public int getTxPower() {
        if (adList == null) {
            return 0;
        }

        for (AD ad : adList) {
            if (ad.getTxPower() != 0) {
                return ad.getTxPower();
            }
        }

        return 0;
    }

    public String getDistance() {
        double accuracy = getAccuracy();
        if (accuracy == -1.0) {
            return "Desconhecido";
        } else if (accuracy < 1) {
            return "Muito próximo";
        } else if (accuracy < 3) {
            return "Próximo";
        } else {
            return "Longe";
        }
    }

    public double getAccuracy() {
        int txPower = getTxPower();
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }
        double ratio = rssi * 1.0 / txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            return (0.89976) * Math.pow(ratio,7.7095) + 0.111;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Device device = (Device) o;

        return !(mAddress != null ? !mAddress.equals(device.mAddress) : device.mAddress != null);

    }

    @Override
    public int hashCode() {
        return mAddress != null ? mAddress.hashCode() : 0;
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mAddress);
        dest.writeParcelable(mBluetoohDevice, flags);
        dest.writeInt(rssi);
        dest.writeInt(scanRecord.length);
        dest.writeByteArray(scanRecord);
    }


    /**
     * Return the offers registered for this Device.<br>
     * This method requires internet connection
     *
     * @param listener OfferListener used to callback
     */
    public void getOffers(final OfferListener listener) {
        if (listener != null) {
            if (mTask != null) {
                mTask.cancel(true);
            }

            mTask = new SendBeaconTask() {
                @Override
                protected void onPostExecute(SendBeaconTask.Result result) {
                    super.onPostExecute(result);
                    if (result == null) {
                        listener.onError(errorMessage);
                    } else {
                        listener.onOffersObtained(result.getPlace(), result.getOfferList());
                    }
                }
            };
            mTask.execute(this);
        }
    }

    private static class DeviceCreator implements Creator<Device> {

        @Override
        public Device createFromParcel(Parcel source) {

            Device device = new Device();
            device.mName = source.readString();
            device.mAddress = source.readString();
            device.mBluetoohDevice = source.readParcelable(BluetoothDevice.class.getClassLoader());
            device.rssi = source.readInt();
            int scanLength = source.readInt();

            byte[] scanResult = new byte[scanLength];
            source.readByteArray(scanResult);
            device.scanRecord = scanResult;

            device.adList = new BeaconParser.AltBeaconParser().parseScanRecord(device.scanRecord);
            return device;
        }

        @Override
        public Device[] newArray(int size) {
            return new Device[size];
        }
    }
}
