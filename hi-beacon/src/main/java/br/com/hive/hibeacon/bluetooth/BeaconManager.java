package br.com.hive.hibeacon.bluetooth;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import br.com.hive.hibeacon.core.exceptions.BeaconException;
import br.com.hive.hibeacon.core.parsers.BeaconParser;


/**
 * Created by FBisca on 16/09/2015.
 */
public class BeaconManager {

    private static final int REQUEST_BT = 0xFE;
    private static BeaconManager mInstance;
    private BeaconParser mBeaconParser;
    private Context mContext;

    private BeaconManager(Context context) {
        super();
        this.mContext = context;
    }

    /**
     * Get current BeaconManager instance or creates a new one if it isn't registred
     *
     * @param mContext Current context
     * @return BeaconManager instance
     */
    public static BeaconManager getInstance(Context mContext) {
        if (mInstance == null) {
            mInstance = new BeaconManager(mContext);
        }
        return mInstance;
    }

    /**
     * Return the preferable Scanner based in the running system, a BeaconException will be thrown if your system does not support BLE Operations or if the bluetooth adapter is turned off.
     *
     * @return a BeaconScanner instance
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public BeaconScanner getBeaconScanner() {
        validateSystem();

        BeaconScanner scanner;
        if (Build.VERSION.SDK_INT >= 21) {
            scanner = new BeaconScannerLollipop(mContext);
        } else {
            scanner = new BeaconScannerJB(mContext);
        }

        return scanner;
    }

    public BeaconParser getBeaconParser() {
        if (mBeaconParser == null) {
            mBeaconParser = new BeaconParser.AltBeaconParser();
        }
        return mBeaconParser;
    }

    /**
     * Request to user the activation of the Bluetooth Adapter
     *
     * @param mActivity Activity that should receive onActivityResult
     */
    public void requestBluetooth(Activity mActivity) {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        mActivity.startActivityForResult(enableBtIntent, REQUEST_BT);
    }

    /**
     * Validate if the result of bluetooth activation was successful
     *
     * @param requestCode RequestCode received on the onActivityResult
     * @param resultCode ResultCode received on the onActivityResult
     * @param data Data received on the onActivityResult
     *
     * @return <b>true</b> if the adapter is active, <b>false</b> otherwise
     */
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return requestCode == REQUEST_BT && resultCode == Activity.RESULT_OK;
    }


    /**
     * Check if the system has BLE Support
     * @param context current context
     * @return <b>true</b> if the system has BLE support, <b>false</b> otherwise
     */
    public boolean isBLESupported(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    private void validateSystem() {
        if (!isBLESupported(mContext)) {
            throw new BeaconException("Your Device do not support Bluetooth Low Energy");
        }

        if (Build.VERSION.SDK_INT < 18) {
            throw new BeaconException("Your Application must be API 18 or later");
        }

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            throw new BeaconException("Your Device do not support Bluetooth operations");
        }
    }


}
