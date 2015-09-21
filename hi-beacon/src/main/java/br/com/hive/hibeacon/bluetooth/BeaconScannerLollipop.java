package br.com.hive.hibeacon.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;

import java.util.List;

import br.com.hive.hibeacon.core.model.Device;


/**
 * Created by FBisca on 16/09/2015.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BeaconScannerLollipop extends BeaconScanner  {

    protected BluetoothLeScanner mBluetoothLeScanner;
    protected ScanSettings mScanSettings;
    protected List<ScanFilter> mScanFilters = null;

    BeaconScannerLollipop(Context context) {
        super(context);
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (result.getDevice() != null
                    && result.getScanRecord() != null) {
                Device device = new Device(mContext, result.getDevice(), result.getRssi(),result.getScanRecord());
                if (device.getUUID() != null) {
                    onDeviceScanned(device);
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    @Override
    protected void startScanImpl() {
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (mBluetoothLeScanner == null)  {
            return;
        }

        mBluetoothLeScanner.startScan(getScanFilters(),  getScanSettings(), mScanCallback);
    }

    @Override
    protected void stopScanImpl() {
        if (mBluetoothAdapter != null
                && mBluetoothAdapter.isEnabled()
                && mBluetoothLeScanner != null) {
            mBluetoothLeScanner.stopScan(mScanCallback);
        }
    }

    private ScanSettings createDefaultSetting() {
        return new ScanSettings.Builder()
                .setReportDelay(0)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
    }

    public ScanSettings getScanSettings() {
        if (mScanSettings == null) {
            mScanSettings = createDefaultSetting();
        }

        return mScanSettings;
    }

    public void setScanFilters(List<ScanFilter> mFilters) {
        this.mScanFilters = mFilters;
    }

    public List<ScanFilter> getScanFilters() {
        return mScanFilters;
    }

}
