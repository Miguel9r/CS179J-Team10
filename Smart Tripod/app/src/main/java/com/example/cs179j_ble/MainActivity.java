package com.example.cs179j_ble;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;

import android.util.Log;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    // TAG is used for informational messages
    private final static String TAG = MainActivity.class.getSimpleName();

    // Variables to access objects from the layout such as buttons, switches, values
    private static Button start_button;
    private static Button search_button;
    private static Button connectButton;

    private static Camera camera;
    private static FloatingActionButton upButton;
    private static FloatingActionButton leftButton;
    private static FloatingActionButton rightButton;
    private static FloatingActionButton centerButton;
    private static FloatingActionButton downButton;
    private static ImageButton linearActButton;
    private static ImageButton panTiltButton;
    private static ImageButton cameraButton;
    private static ImageButton flashButton;
    private static AlertDialog.Builder builder;
    private static ListView listView;
    private static ArrayList<String> tasks = new ArrayList<>();

    // Bluetooth Global Variables
    private static boolean deviceFound = false;

    private static ArrayAdapter<String> adapter;
    private static String hc_06UUID = "00001101-0000-1000-8000-00805f9b34fb";

    BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothDevice smartTripod;
    BluetoothDevice mmDevice;
    BluetoothSocket mmSocket;
    Handler mHandler;
    ConnectedThread connectedThread;

    private static final int REQUEST_ENABLE_BLE = 1;

    //This is required for Android 6.0 (Marshmallow)
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    int data = 0;
    int i = 0;
    String guiState = "CarChassis";
    String[] commands = new String[]{"flashOff", "flashOn", "snapPicture","upPanTiltKit","downPanTiltKit","leftPanTiltKit","rightPanTiltKit",
  "upLinearActuator","downLinearActuator","upCarChassis","downCarChassis","leftCarChassis","rightCarChassis"};

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

        downButton = findViewById(R.id.downButton);
        leftButton = findViewById(R.id.leftButton);
        rightButton = findViewById(R.id.rightButton);
        centerButton = findViewById(R.id.centerButton);// guiState: Camera, Car, LA
        cameraButton = findViewById(R.id.cameraButton);
        flashButton = findViewById(R.id.flashButton);
        listView = findViewById(R.id.listView);
        connectButton = findViewById(R.id.connect);
        upButton = findViewById(R.id.upButton);



        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, tasks);
        listView.setAdapter(adapter);

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
    public void startBluetooth(View view)
    {
        if (!BTAdapter.isEnabled())
        {
                    Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBT, REQUEST_ENABLE_BLE);
        }
        else
            search_button.setEnabled(true);

    }

    /**
     * This method handles the Search for Device button
     *
     * @param view the view object
     */
    public void searchBluetooth(View view)
    {
        ArrayList<String>  deviceNameList = new ArrayList<String>();
        Set<BluetoothDevice> pairedDevices = BTAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceNameList.add(device.getName());
                Log.d("Device: ", device.getName() + ": " + device.getAddress());

                if(device.getName().equals("HC-06"))
                {
                   smartTripod = device;
                }
            }
        }

        if(deviceNameList.size() > 0)
        {
            for(String deviceName : deviceNameList)
            {
               tasks.add(deviceName);
            }
            adapter.notifyDataSetChanged();
        }
        connectButton.setEnabled(true);
    }

    public void initiateBluetoothProcess()
    {
        if(BTAdapter.isEnabled()){
            //attempt to connect to bluetooth module
            BluetoothSocket tmp = null;
            mmDevice = smartTripod;
            //create socket
            try {
                if(mmDevice != null)
                {
                    tmp = mmDevice.createRfcommSocketToServiceRecord(UUID.fromString(hc_06UUID));
                    mmSocket = tmp;
                    mmSocket.connect();
                    Log.i("[BLUETOOTH]", "Connected to: " + mmDevice.getName());
                }
        }
        catch(IOException e)
        {
            try{mmSocket.close();}catch(IOException c){return;}
        }
        Log.i("[BLUETOOTH]", "Creating handler");
        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                //super.handleMessage(msg);
                if(msg.what == ConnectedThread.RESPONSE_MESSAGE){
                    String txt = (String)msg.obj;
                    //response.append("\n" + txt);
                }
            }
        };

        Log.i("[BLUETOOTH]", "Creating and running Thread");

        connectedThread = new ConnectedThread(mmSocket,mHandler);
        connectedThread.start();
      }
    }

    public void connect_activity(View view)
    {
        initiateBluetoothProcess();

        Log.d("HC-06:", "Device has been connected!");
        view.setVisibility(View.INVISIBLE);
        view.setEnabled(false);
        search_button.setVisibility(View.INVISIBLE);
        start_button.setText("Device Connected!");
        start_button.setEnabled(false);
        listView.setVisibility(View.INVISIBLE);
    }

    /**
    * This section is for button activity
    * Each button gets an activity
    * */

    public void upButton_activity(View view)
    {
        if(mmSocket.isConnected())
        {
          if(guiState == "CarChassis")
          {
            int val = 0;
            for (i = 0; i<commands.length; i++) {
              if (commands[i]=="upCarChassis") {
                val = i;
              }
            }
            Log.d("SENDING DATA:", "Value sent: " + val);
            connectedThread.write(val);
            Log.d("SENDING DATA:", "Data sent!");
            Log.d("MOVING CAR CHASSIS:", "Car will move forward.");
          }
          else if (guiState == "LinearActuator") {
            int val = 0;
            for (i = 0; i<commands.length; i++) {
              if (commands[i]=="upLinearActuator") {
                val = i;
              }
            }
            Log.d("SENDING DATA:", "Value sent: " + val);
            connectedThread.write(val);
            Log.d("SENDING DATA:", "Data sent!");
            Log.d("MOVING LA:", "Linear Actuator will move up.");
          }
          else if (guiState == "Camera") {
            int val = 0;
            for (i = 0; i<commands.length; i++) {
              if (commands[i]=="upPanTiltKit") {
                val = i;
              }
            }
            Log.d("SENDING DATA:", "Value sent: " + val);
            connectedThread.write(val);
            Log.d("SENDING DATA:", "Data sent!");
            Log.d("MOVING CAMERA:", "Pan Tilt Kit will move up.");
          }
          else {
            // empty for now
          }
        }
        else{
            Log.d("SENDING DATA:", "mmSocket is NOT connected");
        }
    }

    public void leftButton_activity(View view)
    {
        /*Context context = getApplicationContext();
        CharSequence text = "Left button pressed!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context,text, duration);
        toast.show();*/
    }

    public void centerButton_activity(View view)
    {
      if(mmSocket.isConnected())
      {
        if(guiState == "CarChassis")
        {
          Log.d("CHANGING STATE:", "Buttons now control Linear Actuator.");
          guiState = "LinearActuator";
          //text = "Linear Actuator";
          leftButton.setVisibility(View.INVISIBLE);
          rightButton.setVisibility(View.INVISIBLE);
        }
        else if (guiState == "LinearActuator") {
          Log.d("CHANGING STATE:", "Buttons now control Pan Tilt Kit.");
          guiState = "Camera";
          //text = "Camera";
          leftButton.setVisibility(View.VISIBLE);
          rightButton.setVisibility(View.VISIBLE);
        }
        else if (guiState == "Camera") {
          Log.d("CHANGING STATE:", "Buttons now control Car Chassis.");
          guiState = "CarChassis";
         // text = "Car Chassis";
          leftButton.setVisibility(View.VISIBLE);
          rightButton.setVisibility(View.VISIBLE);
        }
        else {
          guiState = "CarChassis";
          upButton.setVisibility(View.VISIBLE);
          downButton.setVisibility(View.VISIBLE);
          leftButton.setVisibility(View.VISIBLE);
          rightButton.setVisibility(View.VISIBLE);
        }
      }
      else{
          Log.d("SENDING DATA:", "mmSocket is NOT connected");
      }
    }

    public void rightButton_activity(View view)
    {
        /*Context context = getApplicationContext();
        CharSequence text = "Right button pressed!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context,text, duration);
        toast.show();*/
    }

    public void downButton_activity(View view)
    {
          if(mmSocket.isConnected())
          {
            if(guiState == "CarChassis")
            {
              int val = 0;
              for (i = 0; i<commands.length; i++) {
                if (commands[i]=="downCarChassis") {
                  val = i;
                }
              }
              Log.d("SENDING DATA:", "Value sent: " + val);
              connectedThread.write(val);
              Log.d("SENDING DATA:", "Data sent!");
              Log.d("MOVING CAR CHASSIS:", "Car will move backward.");
            }
            else if (guiState == "LinearActuator") {
              int val = 0;
              for (i = 0; i<commands.length; i++) {
                if (commands[i]=="downLinearActuator") {
                  val = i;
                }
              }
              Log.d("SENDING DATA:", "Value sent: " + val);
              connectedThread.write(val);
              Log.d("SENDING DATA:", "Data sent!");
              Log.d("MOVING LA:", "Linear Actuator will move down.");
            }
            else if (guiState == "Camera") {
              int val = 0;
              for (i = 0; i<commands.length; i++) {
                if (commands[i]=="downPanTiltKit") {
                  val = i;
                }
              }
              Log.d("SENDING DATA:", "Value sent: " + val);
              connectedThread.write(val);
              Log.d("SENDING DATA:", "Data sent!");
              Log.d("MOVING CAMERA:", "Pan Tilt Kit will move down.");
            }
            else {
              // empty for now
            }
          }
          else{
              Log.d("SENDING DATA:", "mmSocket is NOT connected");
          }
    }

    public void flashButton_activity(View view)
    {
        if(mmSocket.isConnected())
        {
            if (data == 0) {
                Log.d("SENDING DATA:", "Attempting to send data...");
                data = 1;
                connectedThread.write(data);
                Log.d("SENDING DATA:", "Data sent!");
                Log.d("SENDING DATA:", "LED should turn ON now!");
            } else if(data == 1) {
                Log.d("SENDING DATA:", "Attempting to send data...");
                data = 0;
                connectedThread.write(data);
                Log.d("SENDING DATA:", "Data sent!");
                Log.d("SENDING DATA:", "LED should turn OFF now!");
            }
        }
        else{
            Log.d("SENDING DATA:", "mmSocket is NOT connected");
        }
    }

    public void cameraButton_activity(View view)
    {
        if(mmSocket.isConnected())
        {
          int val = 0;
          for (i = 0; i<commands.length; i++) {
            if (commands[i]=="snapPicture") {
              val = i;
            }
          }
          Log.d("SENDING DATA:", "Value sent: " + val);
          connectedThread.write(val);
          Log.d("SENDING DATA:", "Data sent!");
          Log.d("SENDING DATA:", "Camera will snap a picture.");
        }
        else{
            Log.d("SENDING DATA:", "mmSocket is NOT connected");
        }
        /*builder = new AlertDialog.Builder(this);
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
        alert.show();*/
    }


    /**
    * End of button activity section
    * */
}
