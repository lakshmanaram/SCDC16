package com.shaastra.indoornavigation;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.acl.AclNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends Activity {

    static Button Speak_Button;
    static TextToSpeech TTSobject;
    static TextView navtext;
    static int TTSresult;
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mDevice;
    OutputStream mmOutStream;
    InputStream mmInStream;
    String message;
    ArrayList<String> speechResult = new ArrayList<>();
    List<String> possiblities = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "Stairs", "Entrance", "Exit");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navtext = (TextView) findViewById(R.id.navtext);
        Speak_Button = (Button) findViewById(R.id.speak_button);
        Speak_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String current = Speak_Button.getText().toString();
                if (current.equals("Start Navigation")) {
                    promptSpeechInput("Navigate to? ");
                    String text = "not found";
                    while(text.equals("not found"))
                    {
                        for(int i=0;i<speechResult.size();i++){
                            for(int j = 0;j < possiblities.size();j++)
                                if(speechResult.get(i).equalsIgnoreCase(possiblities.get(j)))
                                    text = possiblities.get(j);
                        }
                        if(text.equals("not found"))
                            promptSpeechInput("Didn't catch that! Please Retry");
                    }
                    sendDataToPairedDevice("N"+ String.valueOf(possiblities.indexOf(text)),mDevice);
                    Speak_Button.setText("Facing ?");
                } else if (current.equals("Facing ?")) {
                    promptSpeechInput("Facing?");
                    String text = "not found";
                    while(text.equals("not found"))
                    {
                        for(int i=0;i<speechResult.size();i++){
                            for(int j = 0;j < possiblities.size();j++)
                                if(speechResult.get(i).equalsIgnoreCase(possiblities.get(j)))
                                    text = possiblities.get(j);
                        }
                        if(text.equals("not found"))
                            promptSpeechInput("Didn't catch that! Please Retry");
                    }
                    sendDataToPairedDevice("F" + String.valueOf(possiblities.indexOf(text)), mDevice);
                    Speak_Button.setText("Done ?");
                } else if (current.equals("Done ?")) {
                    speak_this("You have reached your destination");
                    Speak_Button.setText("Start Navigation");
                    navtext.setText("");
                }
            }
        });

        Button resetbutton = (Button) findViewById(R.id.reset);
        resetbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Speak_Button.setText("Start Navigation");
                navtext.setText("");
            }
        });

        TTSobject = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {

                    TTSresult = TTSobject.setLanguage(Locale.UK);

                } else {

                    Log.i("tts initialization", "not supported");

                }

            }
        });

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
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

    private void sendDataToPairedDevice(String message, BluetoothDevice device) {
        byte[] toSend = message.getBytes();
        try {
            mmOutStream.write(toSend);
            // Your Data is sent to  BT connected paired device ENJOY.
        } catch (IOException e) {
            e.printStackTrace();
//Log.e(TAG, "Exception during write", e);
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
                    message = writeMessage;
                    Toast.makeText(getApplicationContext(), writeMessage, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;

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

    }

    public void promptSpeechInput(String prompt){
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, prompt);

        try {
            startActivityForResult(i, 100);
        }catch (ActivityNotFoundException a){
            Log.d("promptSpeechInput()","device doesn't support");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case 100:
                if(resultCode == RESULT_OK && data != null){
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Toast.makeText(getApplicationContext(),result.toString(),Toast.LENGTH_LONG).show();
                    //speak_this(result.toString());
                    speechResult = result;
                }
                break;

        }
    }

    public void speak_this(String message){

        if(TTSresult == TextToSpeech.LANG_NOT_SUPPORTED || TTSresult == TextToSpeech.LANG_MISSING_DATA){
            Log.i("speak_this","feature not supported");
        }else{
            TTSobject.speak(message,TextToSpeech.QUEUE_FLUSH,null);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        TTSobject.stop();
        TTSobject.shutdown();

    }
}
