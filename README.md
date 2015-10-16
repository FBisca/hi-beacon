hi-beacon
=======

SDK created to scan Beacons


Example
--------
First, retrieve BeaconManager and BeaconScanner instance
```java
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get a new instance of the BeaconManager
        mBeaconManager = BeaconManager.getInstance(this);

        // Only call the getBeaconScanner() if the system support BLE
        if (mBeaconManager.isBLESupported(this)) {

            mBeaconScanner = mBeaconManager.getBeaconScanner(); // Get the preferable system BLE Scanner
            mBeaconScanner.setDeviceListener(this); // Set a listener for devices interactions
            mBeaconScanner.setListener(this); // Set a listener for scanner status
        }
    }
```

Start scan
```java
	@Override
    protected void onResume() {
        super.onResume();
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
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mBeaconManager != null && mBeaconManager.onActivityResult(requestCode, resultCode, data)) {
            startScan();
        }
    }
```

Receive listener calls
```java
	@Override
    public void onFoundDevice(Device device) {
		// Called when a new device has being detected
    }

    @Override
    public void onUpdateDevice(Device device) {
		// Called when a previously detected device has being found
    }

    @Override
    public void onDeviceMissing(Device device) {
		// Called when a previously detected device it no longer visible
    }

    @Override
    public void onStartScan() {
		// Called when the Scanner has started
    }

    @Override
    public void onStopScan(List<Device> foundDevices, boolean willRestart) {
		// Called when the Scanner has stopped, with all devices found
    }
```

Get Offers
```java
	//Access token for the Offer API
	String accessToken = "95ac2da6a85c8ac3914fe22366380a27";
	
	// This method needs a internet connection
	mDevice.getOffers(accessToken, new OfferListener() {
		@Override
		public void onOffersObtained(Place place, List<Offer> offers) {
			
		}

		@Override
		public void onError(ApiException error) {
			
			if (error instanceof ServiceException) { // Check if it's an error from API Service
				String message = error.getMessage(); // Retrieves the API Service error message;
				
			} else if (error instanceof TimeoutException) { // Check if a timeout was fired before the request completion
			
			} else { // A Generic API error, check the message for details. May be a JSONException, Parameters missing, UnknownHostException, etc.
			}
		}
	});
	
```

When the operation is finished, stop the scanner
```java
	@Override
    protected void onStop() {
        super.onStop();
        stopScan();
    }
	
	private void stopScan() {
        if (mBeaconScanner != null) {
            mBeaconScanner.stopScan(); // Stop all scans
        }
    }
```

Developed By
------------
* Felippe Bisca

License
-------

    Copyright 2015 Hive Digital Media
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
    http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.