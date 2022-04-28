package com.example.ctwoodcustoms;

import android.bluetooth.BluetoothAdapter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class BluetoothHelper {
    private String deviceName = null;
    private String deviceAddress = null;
    private Boolean isConnected = false;

    public static Handler handler;

    private BluetoothCreate bluetoothCreate;

    private final static int CONNECTING_STATUS = 1; // used in bluetooth handler to identify message status
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update

    public BluetoothHelper(String deviceName, String deviceAddress) {

        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg){
                System.out.println(msg + "helper");
//                switch (msg.what){
//                    case CONNECTING_STATUS:
//                        switch(msg.arg1){
//                            case 1:
////                                toolbar.setSubtitle("Connected to " + deviceName);
////                                progressBar.setVisibility(View.GONE);
////                                buttonConnect.setEnabled(true);
////                                buttonToggle.setEnabled(true);
//                                break;
//                            case -1:
////                                toolbar.setSubtitle("Device fails to connect");
////                                progressBar.setVisibility(View.GONE);
////                                buttonConnect.setEnabled(true);
//                                break;
//                        }
//                        break;
//
//                    case MESSAGE_READ:
//                        String arduinoMsg = msg.obj.toString(); // Read message from Arduino
//                        switch (arduinoMsg.toLowerCase()){
//                            case "led is turned on":
////                                imageView.setBackgroundColor(getResources().getColor(R.color.colorOn));
////                                textViewInfo.setText("Arduino Message : " + arduinoMsg);
//                                break;
//                            case "led is turned off":
////                                imageView.setBackgroundColor(getResources().getColor(R.color.colorOff));
////                                textViewInfo.setText("Arduino Message : " + arduinoMsg);
//                                break;
//                        }
//                        break;
//                }
            }
        };

    }

    public boolean Connect() throws InterruptedException {

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothCreate = new BluetoothCreate(bluetoothAdapter,deviceAddress, handler);
        bluetoothCreate.start();

        return true;

    }

    public void SendMessage(String input) {
        bluetoothCreate.transferThread.write(input);
    }

    public void SendMessage(byte[] input) {
        bluetoothCreate.transferThread.write(input);
    }

}
