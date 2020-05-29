package com.example.cs179j_ble;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
    private static TextView cameraArea_Text;
    private static FloatingActionButton upButton;
    private static FloatingActionButton leftButton;
    private static FloatingActionButton rightButton;
    private static FloatingActionButton centerButton;
    private static FloatingActionButton downButton;
    private static FloatingActionButton linearActButton;
    private static FloatingActionButton panTiltButton;
    private static FloatingActionButton cameraButton;
    private static FloatingActionButton flashButton;
    private static AlertDialog.Builder builder;
    private static TextView controllerState;
    private static ListView listView;
    private static ArrayList<String> tasks = new ArrayList<>();
    private static ArrayList<String> commandsList = new ArrayList<>(
            Arrays.asList("flashOff", "flashOn", "snapPicture","upPanTiltKit","downPanTiltKit","leftPanTiltKit","rightPanTiltKit",
                    "upLinearActuator","downLinearActuator","upCarChassis","downCarChassis","leftCarChassis","rightCarChassis"));

    // Bluetooth Global Variables
    private static boolean deviceFound = false;

    private static ArrayAdapter<String> adapter;
    private static String hc_06UUID = "00001101-0000-1000-8000-00805f9b34fb";
    private static String baseString = "Controlling: ";

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
    String guiState = "Car Chassis";
    String[] commands = new String[]{"flashOff", "flashOn", "snapPicture","upPanTiltKit","downPanTiltKit","leftPanTiltKit","rightPanTiltKit",
  "upLinearActuator","downLinearActuator","upBLEModuleService","downBLEModuleService","leftBLEModuleService","rightBLEModuleService"};

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
        controllerState = findViewById(R.id.controllerState);
        listView = findViewById(R.id.listView);
        connectButton = findViewById(R.id.connect);
        upButton = findViewById(R.id.upButton);
        linearActButton = findViewById(R.id.linearActuator);
        panTiltButton = findViewById(R.id.cameraPanTilt);
        cameraArea_Text = findViewById(R.id.cameraAreaText);

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
        else {
            search_button.setEnabled(true);
            /* -- Moved to connect_activity function
            controllerState.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            String state_text = baseString + guiState;
            controllerState.setText(state_text);
            */
        }
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

                    cameraArea_Text.setText(txt);
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
        view.setVisibility(View.GONE);
        view.setEnabled(false);

        search_button.setVisibility(View.GONE);
        start_button.setVisibility(View.GONE);
        connectButton.setVisibility(View.GONE);
        controllerState.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
        String state_text = baseString + guiState;
        controllerState.setText(state_text);
    }

    /**
    * This section is for button activity
    * Each button gets an activity
    * */

    public void upButton_activity(View view)
    {
        if(mmSocket.isConnected())
        {
          if(guiState == "Car Chassis")
          {

            Log.d("STATE: ", "Car Chassis");
            Log.d("SENDING DATA:", "Value sent: " + commandsList.indexOf("upCarChassis"));
            connectedThread.write(commandsList.indexOf("upCarChassis"));
            Log.d("SENDING DATA:", "Data sent!");
            Log.d("MOVING CAR CHASSIS:", "Car will move forward.");


          }
          else if (guiState == "Linear Actuator") {

              Log.d("STATE: ", "Linear Actuator");
              Log.d("SENDING DATA:", "Value sent: " + commandsList.indexOf("upLinearActuator"));
              connectedThread.write(commandsList.indexOf("upLinearActuator"));
              Log.d("SENDING DATA:", "Data sent!");
              Log.d("MOVING LA:", "Linear Actuator will move up.");
          }
          else if (guiState == "Pan Tilt") {

              Log.d("STATE: ", "Camera");
              Log.d("SENDING DATA:", "Value sent: " + commandsList.indexOf("upPanTiltKit"));
              connectedThread.write(commandsList.indexOf("upPanTiltKit"));
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
        if(mmSocket.isConnected())
        {
            if(guiState == "Car Chassis")
            {
                Log.d("STATE: ", "Car Chassis");
                Log.d("SENDING DATA:", "Value sent: " + commandsList.indexOf("leftCarChassis"));
                connectedThread.write(commandsList.indexOf("leftCarChassis"));
                Log.d("SENDING DATA:", "Data sent!");
                Log.d("MOVING CAR CHASSIS:", "Car will move left.");


            }
            else if(guiState == "Pan Tilt" )
            {
                Log.d("STATE: ", "Camera");
                Log.d("SENDING DATA:", "Value sent: " + commandsList.indexOf("leftPanTiltKit"));
                connectedThread.write(commandsList.indexOf("leftPanTiltKit"));
                Log.d("SENDING DATA:", "Data Sent!");
                Log.d("MOVING CAMERA:", "Pan Tilt Kit will move left.");
            }

        }
        else
            Log.d("SENDING DATA:", "mmSocket is NOT connected!");
    }

    public void centerButton_activity(View view)
    {
        guiState = "Linear Actuator";
        String state = baseString + guiState;
        controllerState.setText(state);



        Log.d("CHANGING STATE: ", "Buttons should now control the Linear Actuator");
        String state_text = baseString + guiState;
        controllerState.setText(state_text);
        leftButton.setVisibility(View.INVISIBLE);
        rightButton.setVisibility(View.INVISIBLE);
        view.setVisibility(View.INVISIBLE);
        linearActButton.setVisibility(View.VISIBLE);
    }

    public void linearActuator_activity(View view)
    {
        Log.d("CHANGING STATE:", "Buttons now control Pan Tilt Kit.");
        guiState = "Pan Tilt";

        // Change text view
        String state = baseString + guiState;
        controllerState.setText(state);

        String state_text = baseString + guiState;
        controllerState.setText(state_text);
        leftButton.setVisibility(View.VISIBLE);
        rightButton.setVisibility(View.VISIBLE);
        view.setVisibility(View.INVISIBLE);
        panTiltButton.setVisibility(View.VISIBLE);
    }


    public void cameraPanTilt_activity (View view)
    {
        Log.d("CHANGING STATE:", "Buttons now control Car Chassis.");
        guiState = "Car Chassis";

        String state_text = baseString + guiState;
        controllerState.setText(state_text);
        view.setVisibility(View.INVISIBLE);
        centerButton.setVisibility(View.VISIBLE);
    }

    public void rightButton_activity(View view)
    {
        if(mmSocket.isConnected())
        {
            if(guiState == "Car Chassis")
            {
                Log.d("STATE: ", "Car Chassis");
                Log.d("SENDING DATA:", "Value sent: " + commandsList.indexOf("rightCarChassis"));
                connectedThread.write(commandsList.indexOf("rightCarChassis"));
                Log.d("SENDING DATA:", "Data sent!");
                Log.d("MOVING CAR CHASSIS:", "Car will move right.");
            }
            else if(guiState == "Pan Tilt" )
            {
                Log.d("STATE: ", "Camera");
                Log.d("SENDING DATA:", "Value sent: " + commandsList.indexOf("rightPanTiltKit"));
                connectedThread.write(commandsList.indexOf("rightPanTiltKit"));
                Log.d("SENDING DATA:", "Data Sent!");
                Log.d("MOVING CAMERA:", "Pan Tilt Kit will move right.");
            }
        }
        else
            Log.d("SENDING DATA:", "mmSocket is NOT connected!");
    }

    public void downButton_activity(View view)
    {
          if(mmSocket.isConnected())
          {
            if(guiState == "Car Chassis")
            {

              Log.d("STATE: ", "Car Chassis");
              Log.d("SENDING DATA:", "Value sent: " + commandsList.indexOf("downCarChassis"));
              connectedThread.write(commandsList.indexOf("downCarChassis"));
              Log.d("SENDING DATA:", "Data sent!");
              Log.d("MOVING CAR CHASSIS:", "Car will move backward.");
            }
            else if (guiState == "Linear Actuator") {
//              int val = 0;
//              for (i = 0; i<commands.length; i++) {
//                if (commands[i]=="downLinearActuator") {
//                  val = i;
//                }
//              }
              Log.d("STATE: ", "Linear Actuator");
              Log.d("SENDING DATA:", "Value sent: " + commandsList.indexOf("downLinearActuator"));
              connectedThread.write(commandsList.indexOf("downLinearActuator"));
              Log.d("SENDING DATA:", "Data sent!");
              Log.d("MOVING LA:", "Linear Actuator will move down.");
            }
            else if (guiState == "Pan Tilt") {
//              int val = 0;
//              for (i = 0; i<commands.length; i++) {
//                if (commands[i]=="downPanTiltKit") {
//                  val = i;
//                }
//              }
              Log.d("SENDING DATA:", "Value sent: " + commandsList.indexOf("downPanTiltKit"));
              connectedThread.write(commandsList.indexOf("downPanTiltKit"));
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
                Log.d("SENDING DATA:", "Attempting to send data..." + commandsList.indexOf("flashOn"));
                data = 1;
                connectedThread.write(commandsList.indexOf("flashOn"));
                Log.d("SENDING DATA:", "Data sent!");
                Log.d("SENDING DATA:", "LED should turn ON now!");
            } else if(data == 1) {
                Log.d("SENDING DATA:", "Attempting to send data..." + commandsList.indexOf("flashOff"));
                data = 0;
                connectedThread.write(commandsList.indexOf("flashOff"));
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
            connectedThread.run();
            Log.d("CT-RUN:", " Message: " + mHandler.obtainMessage());
//          int val = 0;
//          for (i = 0; i<commands.length; i++) {
//            if (commands[i]=="snapPicture") {
//              val = i;
//            }
//          }
          Log.d("SENDING DATA:", "Value sent: " + commandsList.indexOf("snapPicture"));
          connectedThread.write(commandsList.indexOf("snapPicture"));
          Log.d("SENDING DATA:", "Data sent!");
          Log.d("SENDING DATA:", "Camera will snap a picture.");
        }
        else{
            Log.d("SENDING DATA:", "mmSocket is NOT connected");
        }
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
