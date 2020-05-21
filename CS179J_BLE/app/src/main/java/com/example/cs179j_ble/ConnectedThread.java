package com.example.cs179j_ble;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectedThread extends Thread
{
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    public static final int RESPONSE_MESSAGE = 10;
    Handler uih;

    public ConnectedThread(BluetoothSocket socket, Handler uih)
    {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        this.uih = uih;
        Log.i("[THREAD-CT]","Creating thread");
        try{
            Log.d("[THREAD-CT]", "Entered first try CT ");
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch(IOException e) {
            Log.e("[THREAD-CT]","Error:"+ e.getMessage());
        }
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
        try {
            mmOutStream.flush();
        } catch (IOException e) {
            return;
        }
        Log.i("[THREAD-CT]","IO's obtained");
    }
}
