package com.example.ctwoodcustoms;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class BluetoothTransfer extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final static int MESSAGE_READ = 2;
    public Handler handler;

    public BluetoothTransfer(BluetoothSocket socket, Handler handler) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        this.handler = handler;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes = 0; // bytes returned from read()
        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                    /*
                    Read from the InputStream from Arduino until termination character is reached.
                    Then send the whole String message to GUI Handler.
                     */
                buffer[bytes] = (byte) mmInStream.read();
                String readMessage;
                if (buffer[bytes] == '\n'){
                    readMessage = new String(buffer,0,bytes);
                    Log.e("Arduino Message",readMessage);
                    handler.obtainMessage(MESSAGE_READ,readMessage).sendToTarget();
                    bytes = 0;
                } else {
                    bytes++;
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(String input) {
        byte[] bytes = input.getBytes(); //converts entered String into bytes
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) {
            Log.e("Send Error","Unable to send message",e);
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] input) {
//        byte[] bytes = input.getBytes(); //converts entered String into bytes
        try {
            mmOutStream.write(input);
        } catch (IOException e) {
            Log.e("Send Error","Unable to send message",e);
        }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}
