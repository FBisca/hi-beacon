package br.com.hive.hibeacon.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.hive.hibeacon.core.model.Device;


/**
 * Created by FBisca on 16/09/2015.
 */
public abstract class BeaconScanner {

    protected Map<String, Device> mLastScanDevices = new HashMap<>();
    protected Map<String, Device> mCurrentScanDevices = new HashMap<>();
    protected Context mContext;
    protected BluetoothAdapter mBluetoothAdapter;
    protected DeviceCallback mDeviceCallback;
    protected ScannerCallback mCallback;
    protected long mIntervalScan = 1000 * 60;
    protected long mScanTime = 1000 * 10;
    protected boolean mKeepAlive = false;
    protected Handler mHandler;


    private Runnable mStartDelayed = new Runnable() {
        @Override
        public void run() {
            startScan();
        }
    };

    private Runnable mStopDelayed = new Runnable() {
        @Override
        public void run() {
            stopScan(mKeepAlive);
        }
    };

    BeaconScanner(Context context) {
        this.mContext = context;
        this.mHandler = new Handler();
    }


    /**
     * Scan the region for BLE Entries, any device found will trigger the <b>DeviceListener</b>.<br>
     * The scan will stop itself after the scan time.<br>
     * Only one scan can be active per application.
     *
     */
    public void startScan() {
        startScan(false);
    }

    /**
     * Scan the region for BLE Entries, any device found will trigger the <b>DeviceListener</b>.<br>
     * The scan will stop itself after the scan time, if <b>keepAlive</b> is true, after the interval time it will be reactivated<br>
     *
     * Only one scan can be active per application.
     *
     * @param keepAlive <b>true</b> if it should be reactivated after interval time.
     */
    public void startScan(boolean keepAlive) {
        this.mKeepAlive = keepAlive;

        mHandler.removeCallbacks(mStartDelayed);
        mHandler.removeCallbacks(mStopDelayed);

        if (!isAdapterEnabled())  {
            return;
        }

        stopScanImpl();
        startScanImpl();

        if (mCallback != null) {
            mCallback.onStartScan();
        }

        mHandler.postDelayed(mStopDelayed, mScanTime);
    }

    /**
     * Stop the BLE Scan.
     */
    public void stopScan() {
        stopScan(false);
    }


    /**
     * Stop the BLE Scan<br>
     *
     * @param keepAlive <b>true</b> if should be reactivated after interval time
     */
    public void stopScan(boolean keepAlive) {
        mHandler.removeCallbacks(mStartDelayed);
        mHandler.removeCallbacks(mStopDelayed);

        stopScanImpl();

        if (mCallback != null) {
            List<Device> arrayList = new ArrayList<>();
            arrayList.addAll(mCurrentScanDevices.values());
            mCallback.onStopScan(arrayList, keepAlive);
        }

        if (keepAlive) {
            finishScanIteration();
            mHandler.postDelayed(mStartDelayed, mIntervalScan);
        }
    }

    protected void onDeviceScanned(Device device) {
        if (mLastScanDevices.get(device.getAddress()) == null && mDeviceCallback != null) {
            if (mDeviceCallback != null) {
                mDeviceCallback.onFoundDevice(device);
            }
        } else if (mLastScanDevices.get(device.getAddress()) != null) {
            if (mDeviceCallback != null) {
                mDeviceCallback.onUpdateDevice(device);
            }
        }

        mCurrentScanDevices.put(device.getAddress(), device);
    }

    private void finishScanIteration() {
        checkForMissingDevices();

        mLastScanDevices.clear();
        mLastScanDevices.putAll(mCurrentScanDevices);
        mCurrentScanDevices.clear();
    }

    private void checkForMissingDevices() {
        if (mDeviceCallback == null) {
            return;
        }

        for (String keyLast : mLastScanDevices.keySet()) {
            if (mCurrentScanDevices.get(keyLast) == null) {
                mDeviceCallback.onDeviceMissing(mLastScanDevices.get(keyLast));
            }
        }
    }


    /**
     * Clear references and stop scanning
     */
    public void close() {
        mDeviceCallback = null;
        mCallback = null;
        stopScan(false);
    }


    /**
     * Return SO BluetoothAdapter
     *
     * @return SO Default BluetoothAdapter
     */
    public BluetoothAdapter getAdapter() {
        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        return mBluetoothAdapter;
    }


    /**
     * Check if the Bluetooth Adapter is enabled, and ready to start scanning
     *
     * @return <b>true</b> if adapter is enabled
     */
    public boolean isAdapterEnabled() {
        return getAdapter() != null && getAdapter().isEnabled();
    }


    /**
     * Set a callback to all devices interactions.
     *
     * @param callback DeviceCallback
     */
    public void setDeviceListener(DeviceCallback callback) {
        this.mDeviceCallback = callback;
    }

    /**
     * Set a callback to the scanner
     *
     * @param mCallback ScannerCallback
     */
    public void setListener(ScannerCallback mCallback) {
        this.mCallback = mCallback;
    }

    /**
     * Set the interval time between scans
     *
     * @param mIntervalScan interval in milliseconds between scans
     */
    public void setIntervalScan(long mIntervalScan) {
        this.mIntervalScan = mIntervalScan;
    }

    /**
     * Set the scans duration
     *
     * @param mScanTime scans duration in milliseconds
     */
    public void setScanTime(long mScanTime) {
        this.mScanTime = mScanTime;
    }

    protected abstract void startScanImpl();
    protected abstract void stopScanImpl();


    public interface DeviceCallback {
        /**
         * On a new device is found
         *
         * @param device Device instance
         */
        void onFoundDevice(Device device);

        /**
         * On a device already found has being scanned once more.<br>
         * It may not be the same instance that the old one, use the method <b>equals</b> to compare them.
         *
         * @param device The new Device Instance.
         */
        void onUpdateDevice(Device device);

        /**
         * Called when a device that was previously found, it's no longer visible in the scan
         * It may not be the same instance that the old one, use the method <b>equals</b> to compare them.
         *
         * @param device The new Device Instance.
         */
        void onDeviceMissing(Device device);
    }

    public interface ScannerCallback {
        /**
         * Called when the scanner has started
         */
        void onStartScan();

        /**
         * Called when the scanner has stoped
         *
         * @param foundDevices all devices that has being found in this scanner iteration
         * @param willRestart <b>true</b> if the scanner will start again after the interval time
         */
        void onStopScan(List<Device> foundDevices, boolean willRestart);
    }
}
