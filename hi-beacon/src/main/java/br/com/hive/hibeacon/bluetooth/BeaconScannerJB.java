package br.com.hive.hibeacon.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;

import br.com.hive.hibeacon.core.model.Device;


/**
 * Created by FBisca on 16/09/2015.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BeaconScannerJB extends BeaconScanner implements BluetoothAdapter.LeScanCallback {


    BeaconScannerJB(Context context) {
        super(context);
    }

    @Override
    protected void startScanImpl() {
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.startLeScan(this);
        }
    }

    @Override
    protected void stopScanImpl() {
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.stopLeScan(this);
        }
    }

    @Override
    public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
        Device device = new Device(mContext, bluetoothDevice, rssi, scanRecord);
        if (device.getUUID() != null) {
            onDeviceScanned(device);
        }
    }
}
