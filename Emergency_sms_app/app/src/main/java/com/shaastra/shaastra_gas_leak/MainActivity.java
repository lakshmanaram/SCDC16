package com.shaastra.shaastra_gas_leak;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends Activity {
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    static String mess;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mDevice;
    OutputStream mmOutStream;
    InputStream mmInStream;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        final EditText phone = (EditText) findViewById(R.id.number);
        final EditText message = (EditText) findViewById(R.id.message);
        final Button addno = (Button) findViewById(R.id.addno);
        final Button chmes = (Button) findViewById(R.id.chmes);


        addno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String no = phone.getText().toString();
                SharedPreferences prefs = getSharedPreferences("nos", MODE_PRIVATE);
                    String num1 = prefs.getString("num1", ":(");
                    if(num1==null||num1.equals(":(")){
                        SharedPreferences.Editor editor = getSharedPreferences("nos", MODE_PRIVATE).edit();
                        editor.putString("num1",no);
                        editor.commit();
                        Toast.makeText(getApplicationContext(),no+" added1",Toast.LENGTH_SHORT).show();
                    }else{
                        String num2 = prefs.getString("num2", ":(");
                        if(num2==null||num2.equals(":(")){
                            SharedPreferences.Editor editor = getSharedPreferences("nos", MODE_PRIVATE).edit();
                            editor.putString("num2",no);
                            editor.commit();
                            Toast.makeText(getApplicationContext(),no+" added2",Toast.LENGTH_SHORT).show();
                        }else {
                            String num3 = prefs.getString("num3", ":(");
                            if(num3.equals(":(")){
                                SharedPreferences.Editor editor = getSharedPreferences("nos", MODE_PRIVATE).edit();
                                editor.putString("num3",no);
                                editor.commit();
                                Toast.makeText(getApplicationContext(),no+" added3",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            }
        });

        chmes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mess = message.getText().toString();

                SharedPreferences.Editor editor = getSharedPreferences("nos", MODE_PRIVATE).edit();
                editor.putString("message",mess);
                editor.commit();
                Toast.makeText(getApplicationContext(),mess+" updated",Toast.LENGTH_SHORT).show();
            }
        });

        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    mDevice = device;
                    Toast.makeText(getApplicationContext(), device.toString(), Toast.LENGTH_SHORT).show();
                }
            }

            ConnectThread mConnectThread = new ConnectThread(mDevice);
            mConnectThread.start();

            ConnectedThread mConnectedThread = new ConnectedThread();
            mConnectedThread.start();

        }
    }

    protected void sendsms(){
        SharedPreferences prefs = getSharedPreferences("nos", MODE_PRIVATE);
            String message1 = prefs.getString("message",":(");
            if(message1.equals(":(")){
                mess = "Emergency!!! :*";
            }else{
                mess = message1;
            }
        Toast.makeText(getApplicationContext(),mess,Toast.LENGTH_SHORT).show();
        String no = "9043804100";
            String num1 = prefs.getString("num1", ":(");
        Toast.makeText(getApplicationContext(),num1 + " "+ mess,Toast.LENGTH_SHORT).show();
            if(!num1.equals(":(")){
                no = num1;
                Toast.makeText(getApplicationContext(),no + " "+ mess,Toast.LENGTH_SHORT).show();
                SmsManager manager = SmsManager.getDefault();
                Log.d("number",num1);
                manager.sendTextMessage(no,null,mess,null,null);
            }
            String num2 = prefs.getString("num2", ":(");
            if(!num2.equals(":(")){
                no = num2;
                Toast.makeText(getApplicationContext(),no + " "+ mess,Toast.LENGTH_SHORT).show();
                SmsManager manager = SmsManager.getDefault();
                manager.sendTextMessage(no,null,mess,null,null);
            }
            String num3 = prefs.getString("num3", ":(");
            if(!num3.equals(":(")){
                no = num3;
                Toast.makeText(getApplicationContext(),no + " "+ mess,Toast.LENGTH_SHORT).show();
                SmsManager manager = SmsManager.getDefault();
                manager.sendTextMessage(no,null,mess,null,null);
            }

    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            byte[] writeBuf = (byte[]) msg.obj;
            int begin = (int) msg.arg1;
            int end = (int) msg.arg2;

            switch (msg.what) {
                case 1:
                    String writeMessage = new String(writeBuf);
                    writeMessage = writeMessage.substring(begin, end);
                    if(writeMessage.equals("123"))
                    sendsms();
                    Toast.makeText(getApplicationContext(), writeMessage, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;

        // private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
            }
            mmSocket = tmp;
        }

        public void run() {
            mBluetoothAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
            } catch (IOException connectException) {
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                }
                return;
            }
            try {
                mmInStream = mmSocket.getInputStream();
                mmOutStream = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void cancel() {

        }
    }

    private class ConnectedThread extends Thread {
        public ConnectedThread() {

        }
        public void run() {
            byte[] buffer = new byte[1024];
            int begin = 0;
            int bytes = 0;
            while (true) {
                try {
                    if(mmInStream!=null) {
                        bytes += mmInStream.read(buffer, bytes, buffer.length - bytes);
                        for (int i = begin; i < bytes; i++) {
                            if (buffer[i] == "#".getBytes()[0]) {
                                mHandler.obtainMessage(1, begin, i, buffer).sendToTarget();
                                begin = i + 1;
                                if (i == bytes - 1) {
                                    bytes = 0;
                                    begin = 0;
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }
        public void cancel() {
        }

    }

}
