package br.com.hive.beacon.view;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import br.com.hive.beacon.R;
import br.com.hive.hibeacon.bluetooth.BeaconManager;
import br.com.hive.hibeacon.bluetooth.BeaconScanner;
import br.com.hive.hibeacon.core.model.Device;

public class MainActivity extends AppCompatActivity implements BeaconScanner.DeviceCallback, BeaconScanner.ScannerCallback, BeaconAdapter.AdapterListener {

    private List<Device> mDevices = new ArrayList<>();
    private BeaconAdapter mAdapter;
    private RecyclerView mListBeacon;
    private MenuItem mMenuScan;

    private BeaconManager mBeaconManager;
    private BeaconScanner mBeaconScanner;
    private boolean isScanning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get a new instance of the BeaconManager
        mBeaconManager = BeaconManager.getInstance(this);

        // Only call the getBeaconScanner() if the system support BLE
        if (mBeaconManager.isBLESupported(this)) {

            mBeaconScanner = mBeaconManager.getBeaconScanner(); // Get the preferable system BLE Scanner
            mBeaconScanner.setDeviceListener(this); // Set a listener for devices interactions
            mBeaconScanner.setListener(this); // Set a listener for scanner status
        }

        loadViews();
    }

    private void loadViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mListBeacon = (RecyclerView) findViewById(R.id.beacon_list);
        mListBeacon.setLayoutManager(new LinearLayoutManager(this));
        mListBeacon.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.bottom = (int) getResources().getDimension(R.dimen.divisor);
            }
        });
        mAdapter = new BeaconAdapter();
        mAdapter.setListener(this);
        mListBeacon.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startScan();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mBeaconManager != null && mBeaconManager.onActivityResult(requestCode, resultCode, data)) {
            startScan();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenuScan = menu.findItem(R.id.action_scan);
        mMenuScan.getIcon().mutate().setColorFilter(ContextCompat.getColor(this, android.R.color.white), PorterDuff.Mode.SRC_ATOP);
        if (isScanning) {
            mMenuScan.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_scan) {
            startScan();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEmptyClick() {
        startScan();
    }

    private void startScan() {
        if (mBeaconScanner != null) {

            // Verify if the adapter is enabled
            if (mBeaconScanner.isAdapterEnabled()) {
                mBeaconScanner.startScan(); // Start scan
            } else {
                mBeaconManager.requestBluetooth(this); // Request the bluetooth
            }
        }
    }

    private void stopScan() {
        if (mBeaconScanner != null) {
            mBeaconScanner.stopScan(); // Stop all scans
        }
    }

    @Override
    public void onFoundDevice(Device device) {
        if (mAdapter.isDeviceAdded(device)) {
            mAdapter.updateDevice(device);
        } else {
            mAdapter.addDevice(device);
        }
    }

    @Override
    public void onUpdateDevice(Device device) {
        mAdapter.updateDevice(device);
    }

    @Override
    public void onDeviceMissing(Device device) {
        mAdapter.removeDevice(device);
    }

    @Override
    public void onStartScan() {
        isScanning = true;
        if (mMenuScan != null) {
            mMenuScan.setVisible(false);
        }
        mAdapter.showLoading(true);
    }

    @Override
    public void onStopScan(List<Device> foundDevices, boolean willRestart) {
        isScanning = false;
        if (mMenuScan != null) {
            mMenuScan.setVisible(true);
        }
        mAdapter.showLoading(false);
    }

    @Override
    public void onDeviceClick(Device device) {
        Intent intent = new Intent(this, OfferActivity.class);
        intent.putExtra(OfferActivity.ARG_DEVICE, device);
        startActivity(intent);
    }
}
