package com.example.cs179j_ble;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ListView;


import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    // TAG is used for informational messages
    private final static String TAG = MainActivity.class.getSimpleName();

    // Variables to access objects from the layout such as buttons, switches, values
    private static TextView mCapsenseValue;
    private static Button start_button;
    private static Button search_button;
    private static Button connect_button;
    private static Button discover_button;
    private static Button disconnect_button;
    private static Switch led_switch;
    private static Switch cap_switch;
    private static Camera camera;
    private static ImageButton upButton;
    private static ImageButton leftButton;
    private static ImageButton rightButton;
    private static ImageButton centerButton;
    private static ImageButton downButton;
    private static ImageButton linearActButton;
    private static ImageButton panTiltButton;
    private static ImageButton cameraButton;
    private static ImageButton flashButton;


    // Variables to manage BLE connection
    private static boolean mConnectState;
    private static boolean mServiceConnected;
    private static BLEModuleService BLEModuleService;

    private static final int REQUEST_ENABLE_BLE = 1;

    //This is required for Android 6.0 (Marshmallow)
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    // Keep track of whether CapSense Notifications are on or off
    private static boolean CapSenseNotifyState = false;

    /**
     * This manages the lifecycle of the BLE service.
     * When the service starts we get the service object and initialize the service.
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        /**
         * This is called when the BLEModuleService is connected
         *
         * @param componentName the component name of the service that has been connected
         * @param service service being bound
         */
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.i(TAG, "onServiceConnected");
            BLEModuleService = ((BLEModuleService.LocalBinder) service).getService();
            mServiceConnected = true;
            BLEModuleService.initialize();
        }

        /**
         * This is called when the PSoCCapSenseService is disconnected.
         *
         * @param componentName the component name of the service that has been connected
         */
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG, "onServiceDisconnected");
            BLEModuleService = null;
        }
    };

    /**
     * This is called when the main activity is first created
     *
     * @param savedInstanceState is any state saved from prior creations of this activity
     */
    @TargetApi(Build.VERSION_CODES.M) // This is required for Android 6.0 (Marshmallow) to work
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Set up a variable to point to the CapSense value on the display
        mCapsenseValue = findViewById(R.id.capsense_value);

        // Set up variables for accessing buttons and slide switches
        start_button = findViewById(R.id.start_button);
        search_button = findViewById(R.id.search_button);
        connect_button = findViewById(R.id.connect_button);
        discover_button = findViewById(R.id.discoverSvc_button);
        disconnect_button = findViewById(R.id.disconnect_button);
        led_switch = findViewById(R.id.led_switch);
        cap_switch = findViewById(R.id.capsense_switch);
        upButton = findViewById(R.id.upButton);
        leftButton = findViewById(R.id.leftButton);
        rightButton = findViewById(R.id.rightButton);
        downButton = findViewById(R.id.downButton);
        centerButton = findViewById(R.id.centerButton);
        linearActButton = findViewById(R.id.lineaerActButton);
        panTiltButton = findViewById(R.id.panTiltButton);
        cameraButton = findViewById(R.id.cameraButton);
        flashButton = findViewById(R.id.flashButton);




        // Initialize service and connection state variable
        mServiceConnected = false;
        mConnectState = false;

        //This section required for Android 6.0 (Marshmallow)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access ");
                builder.setMessage("Please grant location access so this app can detect devices.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        } //End of section for Android 6.0 (Marshmallow)

        /* This will be called when the LED On/Off switch is touched */
        led_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Turn the LED on or OFF based on the state of the switch
                BLEModuleService.writeLedCharacteristic(isChecked);
            }
        });

        /* This will be called when the CapSense Notify On/Off switch is touched */
        cap_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Turn CapSense Notifications on/off based on the state of the switch
                BLEModuleService.writeCapSenseNotification(isChecked);
                CapSenseNotifyState = isChecked;  // Keep track of CapSense notification state
                if(isChecked) { // Notifications are now on so text has to say "No Touch"
                    mCapsenseValue.setText(R.string.NoTouch);
                } else { // Notifications are now off so text has to say "Notify Off"
                    mCapsenseValue.setText(R.string.NotifyOff);
                }
            }
        });

    }


    //This method required for Android 6.0 (Marshmallow)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permission for 6.0:", "Coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
            }
        }
    } //End of section for Android 6.0 (Marshmallow)

    @Override
    protected void onResume() {
        super.onResume();
        // Register the broadcast receiver. This specified the messages the main activity looks for from the BLEModuleService
        final IntentFilter filter = new IntentFilter();
        filter.addAction(com.example.cs179j_ble.BLEModuleService.ACTION_BLESCAN_CALLBACK);
        filter.addAction(com.example.cs179j_ble.BLEModuleService.ACTION_CONNECTED);
        filter.addAction(com.example.cs179j_ble.BLEModuleService.ACTION_DISCONNECTED);
        filter.addAction(com.example.cs179j_ble.BLEModuleService.ACTION_SERVICES_DISCOVERED);
        filter.addAction(com.example.cs179j_ble.BLEModuleService.ACTION_DATA_RECEIVED);
        registerReceiver(mBleUpdateReceiver, filter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BLE && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBleUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close and unbind the service when the activity goes away
        BLEModuleService.close();
        unbindService(mServiceConnection);
        BLEModuleService = null;
        mServiceConnected = false;
    }

    /**
     * This method handles the start bluetooth button
     *
     * @param view the view object
     */
    public void startBluetooth(View view) {

        // Find BLE service and adapter
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLE);
        }

        // Start the BLE Service
        Log.d(TAG, "Starting BLE Service");
        Intent gattServiceIntent = new Intent(this, BLEModuleService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        // Disable the start button and turn on the search  button
        start_button.setEnabled(false);
        search_button.setEnabled(true);
        Log.d(TAG, "Bluetooth is Enabled");
    }

    /**
     * This method handles the Search for Device button
     *
     * @param view the view object
     */
    public void searchBluetooth(View view) {
        if(mServiceConnected) {
            BLEModuleService.scan();
        }

        /* After this we wait for the scan callback to detect that a device has been found */
        /* The callback broadcasts a message which is picked up by the mGattUpdateReceiver */
    }

    /**
     * This method handles the Connect to Device button
     *
     * @param view the view object
     */
    public void connectBluetooth(View view) {
        BLEModuleService.connect();

        /* After this we wait for the gatt callback to report the device is connected */
        /* That event broadcasts a message which is picked up by the mGattUpdateReceiver */
    }

    /**
     * This method handles the Discover Services and Characteristics button
     *
     * @param view the view object
     */
    public void discoverServices(View view) {
        /* This will discover both services and characteristics */
        BLEModuleService.discoverServices();

        /* After this we wait for the gatt callback to report the services and characteristics */
        /* That event broadcasts a message which is picked up by the mGattUpdateReceiver */
    }

    /**
     * This method handles the Disconnect button
     *
     * @param view the view object
     */
    public void Disconnect(View view) {
        BLEModuleService.disconnect();

        /* After this we wait for the gatt callback to report the device is disconnected */
        /* That event broadcasts a message which is picked up by the mGattUpdateReceiver */
    }

    /**
    * This section is for button activity
    * Each button gets an activity
    * */

    public void upButton_activity(View view)
    {
        // Create context for application context for toast to know where it's being displayed
        Context context = getApplicationContext();
        CharSequence text = "Up button pressed!";
        int duration = Toast.LENGTH_SHORT;

        // toast is the notification system that allows us to display text
        Toast toast = Toast.makeText(context,text, duration);
        toast.show();
    }

    public void leftButton_activity(View view)
    {
        Context context = getApplicationContext();
        CharSequence text = "Left button pressed!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context,text, duration);
        toast.show();
    }

    public void centerButton_activity(View view)
    {
        Context context = getApplicationContext();
        CharSequence text = "Center button pressed!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context,text, duration);
        toast.show();

        view.setVisibility(View.INVISIBLE);

        leftButton.setVisibility(View.INVISIBLE);
        rightButton.setVisibility(View.INVISIBLE);
        linearActButton.setVisibility(View.VISIBLE);



    }

    public void linearActButton_activity(View view)
    {
        Context context = getApplicationContext();
        CharSequence text = "Linear Actuator button pressed!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context,text, duration);
        toast.show();

        view.setVisibility(View.INVISIBLE);
        panTiltButton.setVisibility(View.VISIBLE);
        leftButton.setVisibility(View.VISIBLE);
        rightButton.setVisibility(View.VISIBLE);
    }


    public void panTiltButton_activity(View view)
    {
        Context context = getApplicationContext();
        CharSequence text = "Pan Tilt button pressed!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context,text, duration);
        toast.show();

        view.setVisibility(View.INVISIBLE);
        centerButton.setVisibility(View.VISIBLE);
    }


    public void rightButton_activity(View view)
    {
        Context context = getApplicationContext();
        CharSequence text = "Right button pressed!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context,text, duration);
        toast.show();
    }

    public void downButton_activity(View view)
    {
        Context context = getApplicationContext();
        CharSequence text = "Down button pressed!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context,text, duration);
        toast.show();
    }

    public void flashButton_activity(View view)
    {

    }

    public void cameraButton_activity(View view)
    {

    }

    /**
    * End of button activity section
    * */



    /**
     * Listener for BLE event broadcasts
     */
    private final BroadcastReceiver mBleUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case com.example.cs179j_ble.BLEModuleService.ACTION_BLESCAN_CALLBACK:
                    // Disable the search button and enable the connect button
                    search_button.setEnabled(false);
                    connect_button.setEnabled(true);
                    break;

                case com.example.cs179j_ble.BLEModuleService.ACTION_CONNECTED:
                    /* This if statement is needed because we sometimes get a GATT_CONNECTED */
                    /* action when sending Capsense notifications */
                    if (!mConnectState) {
                        // Dsable the connect button, enable the discover services and disconnect buttons
                        connect_button.setEnabled(false);
                        discover_button.setEnabled(true);
                        disconnect_button.setEnabled(true);
                        mConnectState = true;
                        Log.d(TAG, "Connected to Device");
                    }
                    break;
                case com.example.cs179j_ble.BLEModuleService.ACTION_DISCONNECTED:
                    // Disable the disconnect, discover svc, discover char button, and enable the search button
                    disconnect_button.setEnabled(false);
                    discover_button.setEnabled(false);
                    search_button.setEnabled(true);
                    // Turn off and disable the LED and CapSense switches
                    led_switch.setChecked(false);
                    led_switch.setEnabled(false);
                    cap_switch.setChecked(false);
                    cap_switch.setEnabled(false);
                    mConnectState = false;
                    Log.d(TAG, "Disconnected");
                    break;
                case com.example.cs179j_ble.BLEModuleService.ACTION_SERVICES_DISCOVERED:
                    // Disable the discover services button
                    discover_button.setEnabled(false);
                    // Enable the LED and CapSense switches
                    led_switch.setEnabled(true);
                    cap_switch.setEnabled(true);
                    Log.d(TAG, "Services Discovered");
                    break;
                case com.example.cs179j_ble.BLEModuleService.ACTION_DATA_RECEIVED:
                    // This is called after a notify or a read completes
                    // Check LED switch Setting
                    if(BLEModuleService.getLedSwitchState()){
                        led_switch.setChecked(true);
                    } else {
                        led_switch.setChecked(false);
                    }
                    // Get CapSense Slider Value
                    String CapSensePos = BLEModuleService.getCapSenseValue();
                    if (CapSensePos.equals("-1")) {  // No Touch returns 0xFFFF which is -1
                        if(!CapSenseNotifyState) { // Notifications are off
                            mCapsenseValue.setText(R.string.NotifyOff);
                        } else { // Notifications are on but there is no finger on the slider
                            mCapsenseValue.setText(R.string.NoTouch);
                        }
                    } else { // Valid CapSense value is returned
                        mCapsenseValue.setText(CapSensePos);
                    }
                default:
                    break;
            }
        }
    };



}




