package com.example.ctwoodcustoms;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class Controlling extends Activity {
    private static final String TAG = "BlueTest5-Controlling";
    private int mMaxChars = 50000;//Default//change this to string..........
    private UUID mDeviceUUID;
    private BluetoothSocket mBTSocket;
    private ReadInput mReadThread = null;
    private boolean mIsUserInitiatedDisconnect = false;
    private boolean mIsBluetoothConnected = false;
    private Button mBtnDisconnect;
    private BluetoothDevice mDevice;
    final static String on = "92";//on
    final static String off = "79";//off
    public BluetoothHelper bluetoothHelper;
    private Boolean isBusy = false;

    private ProgressDialog progressDialog;
    Button btnOpen, btnClose, btnLidUp, btnLidDn, btnTrayUp, btnTrayDn;

    enum ArdySignal {
        Tyler(0, "tyler"),
        Motor1Up(6, "Motor1Up"),
        Motor1Down(2, "Motor1Down"),
        Motor2Up(3, "Motor2Up"),
        Motor2Down(4, "Motor2Down");

        private java.lang.String name;
        private java.lang.Integer id;

        ArdySignal(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        public Integer getId() {
            return id;
        }

        public byte[] getBytes() {

            return id.toString().getBytes(StandardCharsets.UTF_8);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controlling);

        ActivityHelper.initialize(this);
        // mBtnDisconnect = (Button) findViewById(R.id.btnDisconnect);
        btnOpen = (Button) findViewById(R.id.open);
        btnClose = (Button) findViewById(R.id.close);
        btnLidUp = (Button) findViewById(R.id.lidup);
        btnLidDn = (Button) findViewById(R.id.liddn);
        btnTrayUp = (Button) findViewById(R.id.trayup);
        btnTrayDn = (Button) findViewById(R.id.traydn);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        mDevice = b.getParcelable(MainActivity.DEVICE_EXTRA);
        mDeviceUUID = UUID.fromString(b.getString(MainActivity.DEVICE_UUID));
        mMaxChars = b.getInt(MainActivity.BUFFER_SIZE);

        Log.d(TAG, "Ready");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        bluetoothHelper = new BluetoothHelper(mDevice.getName(), mDevice.getAddress());

        try {
            bluetoothHelper.Connect();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        setListeners();
    }

    protected Thread openController() {
        Runnable runnable = () -> {
            if (isBusy) {
                System.out.println("busy in close controller");
                return;
            }

            isBusy = true;

            System.out.println("Motor1Up");
            sendArdyMessage(ArdySignal.Motor1Up.getBytes());

            wait(30000);

            System.out.println("Motor2Up");
            sendArdyMessage(ArdySignal.Motor2Up.getBytes());

            isBusy = false;
        };
        return new Thread(runnable);
    }

    protected Thread closeController() {
        Runnable runnable = () -> {
            if (isBusy) {
                System.out.println("busy in close controller");
                return;
            }

            isBusy = true;

            System.out.println("Motor1Down");
            sendArdyMessage(ArdySignal.Motor1Down.getBytes());

            wait(30000);

            System.out.println("Motor2Down");
            sendArdyMessage(ArdySignal.Motor2Down.getBytes());
            isBusy = false;
        };
        return new Thread(runnable);
    }

    protected void sendArdyMessage(byte[] output) {
        bluetoothHelper.SendMessage(output);
    }

    protected void sendArdyMessage(String output) {
        bluetoothHelper.SendMessage(output);
    }

    private class ReadInput implements Runnable {

        private boolean bStop = false;
        private Thread t;

        public ReadInput() {
            t = new Thread(this, "Input Thread");
            t.start();
        }

        public boolean isRunning() {
            return t.isAlive();
        }

        @Override
        public void run() {
            InputStream inputStream;

            try {
                inputStream = mBTSocket.getInputStream();
                while (!bStop) {
                    byte[] buffer = new byte[256];
                    if (inputStream.available() > 0) {
                        inputStream.read(buffer);
                        int i = 0;
                        /*
                         * This is needed because new String(buffer) is taking the entire buffer i.e. 256 chars on Android 2.3.4 http://stackoverflow.com/a/8843462/1287554
                         */
                        for (i = 0; i < buffer.length && buffer[i] != 0; i++) {
                        }
                        final String strInput = new String(buffer, 0, i);
                        /*
                         * If checked then receive text, better design would probably be to stop thread if unchecked and free resources, but this is a quick fix
                         */
                    }
                    Thread.sleep(500);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        public void stop() {
            bStop = true;
        }

    }

    private class DisConnectBT extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {//cant inderstand these dotss

            if (mReadThread != null) {
                mReadThread.stop();
                while (mReadThread.isRunning())
                    ; // Wait until it stops
                mReadThread = null;

            }

            try {
                mBTSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mIsBluetoothConnected = false;
            if (mIsUserInitiatedDisconnect) {
                finish();
            }
        }

    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        if (mBTSocket != null && mIsBluetoothConnected) {
            new DisConnectBT().execute();
        }
        Log.d(TAG, "Paused");
        super.onPause();
    }

//    @Override
//    protected void onResume() {
//        if (mBTSocket == null || !mIsBluetoothConnected) {
//            new ConnectBT().execute();
//        }
//        Log.d(TAG, "Resumed");
//        super.onResume();
//    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Stopped");
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
// TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
    }

//    private class ConnectBT extends AsyncTask<Void, Void, Void> {
//        private boolean mConnectSuccessful = true;
//
//        @Override
//        protected void onPreExecute() {
//
////            progressDialog = ProgressDialog.show(Controlling.this, "Hold on", "Connecting");// http://stackoverflow.com/a/11130220/1287554
//
//        }
//
//        @Override
//        protected Void doInBackground(Void... devices) {
//
//            try {
//                if (mBTSocket == null || !mIsBluetoothConnected) {
//
//                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                        Toast.makeText(getApplicationContext(), "Could not select paired device.", Toast.LENGTH_LONG).show();
//                        return null;
//                    }
//
//                    mBTSocket = mDevice.createInsecureRfcommSocketToServiceRecord(mDeviceUUID);
//                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
//                    mBTSocket.connect();
//                }
//            } catch (IOException e) {
//                mConnectSuccessful = false;
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void result) {
//            super.onPostExecute(result);
//
//            if (!mConnectSuccessful) {
//                Toast.makeText(getApplicationContext(), "Could not connect to device.Please turn on your Hardware", Toast.LENGTH_LONG).show();
//                finish();
//            } else {
//                msg("Connected to device");
//                mIsBluetoothConnected = true;
//                mReadThread = new ReadInput(); // Kick off input reader
//            }
//
//            progressDialog.dismiss();
//        }
//
//    }
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    private static void wait(int ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch (InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }

    private void setListeners() {

        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openController().start();
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeController().start();
            }
        });

        btnLidUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendArdyMessage(ArdySignal.Motor1Up.getBytes());
            }
        });

        btnLidDn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendArdyMessage(ArdySignal.Motor1Down.getBytes());
            }
        });

        btnTrayUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendArdyMessage(ArdySignal.Motor2Up.getBytes());
            }
        });

        btnTrayDn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendArdyMessage(ArdySignal.Motor2Down.getBytes());
            }
        });
    }
}