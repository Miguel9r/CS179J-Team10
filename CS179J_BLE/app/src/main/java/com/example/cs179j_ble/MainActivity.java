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
    private static Button start_button;
    private static Button search_button;

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
    private static AlertDialog.Builder builder;
    BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();
    


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
     * This is called when the main activity is first created
     *
     * @param savedInstanceState is any state saved from prior creations of this activity
     */
    @TargetApi(Build.VERSION_CODES.M) // This is required for Android 6.0 (Marshmallow) to work
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start_button = findViewById(R.id.start_button);
        search_button = findViewById(R.id.search_button);

        upButton = findViewById(R.id.upButton);
        leftButton = findViewById(R.id.leftButton);
        rightButton = findViewById(R.id.rightButton);
        downButton = findViewById(R.id.downButton);
        centerButton = findViewById(R.id.centerButton);
        linearActButton = findViewById(R.id.lineaerActButton);
        panTiltButton = findViewById(R.id.panTiltButton);
        cameraButton = findViewById(R.id.cameraButton);
        flashButton = findViewById(R.id.flashButton);
        flashButton.setEnabled(false);


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

    


    /**
     * This method handles the start bluetooth button
     *
     * @param view the view object
     */
    public void startBluetooth(View view) {
        if (!BTAdapter.isEnabled()) {
                    Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBT, REQUEST_ENABLE_BLE);
                }
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
        builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to save this photo?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Toast.makeText(getApplicationContext(),"You saved this photo!", Toast.LENGTH_SHORT).show();
                
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Toast.makeText(getApplicationContext(), "Photo not saved!", Toast.LENGTH_SHORT).show();
            }
        });


        AlertDialog alert = builder.create();
        alert.setTitle("Photo Captured!");
        alert.show();

    }

    /**
    * End of button activity section
    * */







}




