package com.example.cs179j_ble;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class ConnectedThread extends Thread
{

    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;

        // ... (Add other message types here as needed.)
    }

    private static final String TAG = "SENDING:";
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    public static final int RESPONSE_MESSAGE = 10;
    private byte[] mmBuffer; // mmBuffer store for the stream

    Handler uih = new Handler();

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

    // public void run(){
    //     BufferedReader br;
    //     br = new BufferedReader(new InputStreamReader(mmInStream));
    //     while(true){
    //         try{
    //             String resp = br.readLine();
    //             Message msg = new Message();
    //             msg.what = RESPONSE_MESSAGE;
    //             msg.obj = resp;
    //             uih.sendMessage(msg);
    //         }
    //         catch(IOException e){
    //             break;
    //         }
    //     }
    //     Log.i("[THREAD-CT]","While loop ended");
    // }

        public void run(){
            BufferedReader br;
            br = new BufferedReader(new InputStreamReader(mmInStream));
            byte[] rawBytes = null;
            boolean flag = true;
            while(true){
                try{
                    String incoming[];
                    String myString = br.readLine();
                    myString = myString.trim();
                    incoming = myString.split(",");
                    if(flag == true){
                        this.write(2);
                        flag = false;
                    }
                    if (incoming.length > 1)
                    {
                        if(incoming[0].equals("FifoLength:")){
                            rawBytes = new byte[Integer.parseInt(incoming[1])];

                            Log.d("RUN-CT:","Picture size: " + incoming[1] + " bytes");
                        }
                        else if(incoming[0].equals("Image:")){
                            int x = 0;

                            for(int i = 1; i < incoming.length; i++){
                                try {
                                    rawBytes[x] = (byte)(Integer.parseInt(incoming[i]));
                                    
                                    x++;
                                }
                                catch(RuntimeException e){
                                    Log.d("ERROR RUN-CT", e.getMessage());
                                }
                            }
                            try{
                                Message msg = new Message();
                                msg.what = RESPONSE_MESSAGE;
                                msg.obj = rawBytes;
                                msg.arg1 = incoming.length - 1;
                                flag = true;
                                uih.sendMessage(msg);
                            }
                            catch(RuntimeException e){
                                break;
                            }
                        }
                    }
                }
                catch(IOException e){
                    break;
                }
            }
            Log.i("[THREAD-CT]","While loop ended");
        }

    // Call this from the main activity to send data to the remote device.
    public void write(int data){
        try{
            Log.i("[THREAD-CT]", "Writting bytes\n" + data);
            mmOutStream.write(data);
        }catch(IOException e)
        {
            Log.d("[THREAD-CT]", "ERROR writing bytes");
        }
    }


    // Call this method from the main activity to shut down the connection.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }
}
