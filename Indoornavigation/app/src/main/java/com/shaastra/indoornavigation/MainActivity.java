package com.shaastra.indoornavigation;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.security.acl.AclNotFoundException;
import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends Activity {

    static Button Speak_Button;
    static TextToSpeech TTSobject;
    static int TTSresult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Speak_Button = (Button) findViewById(R.id.speak_button);
        Speak_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String current = Speak_Button.getText().toString();
                if(current.equals("Start Navigation")) {
                    promptSpeechInput("Navigate to? ");
                    Speak_Button.setText("Facing ?");
                }else if(current.equals("Facing ?")){
                    promptSpeechInput("Facing?");
                    Speak_Button.setText("Done ?");
                }else if(current.equals("Done ?")){
                    speak_this("You have reached your destination");
                    Speak_Button.setText("Start Navigation");
                }
            }
        });

        TTSobject = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){

                    TTSresult = TTSobject.setLanguage(Locale.UK);

                }else{

                    Log.i("tts initialization","not supported");

                }

            }
        });

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
                    speak_this(result.toString());
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
