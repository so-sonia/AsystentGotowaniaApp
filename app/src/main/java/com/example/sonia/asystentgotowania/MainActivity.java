package com.example.sonia.asystentgotowania;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.speech.tts.TextToSpeech;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import android.widget.Toast;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

import edu.cmu.pocketsphinx.Assets;


public class MainActivity extends AppCompatActivity {
    private Button b1;
    private TextToSpeech t1;
    private int mStatus = 0;
    private MediaPlayer mMediaPlayer;
    private boolean mProcessed = false;
    private final String FILENAME = "/wpta_tts.wav";
    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b1 = (Button)findViewById(R.id.button);
        // Creating an instance of MediaPlayer
        mMediaPlayer = new MediaPlayer();

        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(new Locale("pl_PL"));
                }
                mStatus = status;
//                setTts();
        }
        });

        t1.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onDone(String utteranceId) {

                Log.d(TAG, "juz zrobiłem plik");
                // Speech file is created
                mProcessed = true;

                // Initializes Media Player
                initializeMediaPlayer();
                Log.d(TAG, "wywołałem media");

                // Start Playing Speech
                playMediaPlayer(0);
            }

            @Override
            public void onError(String utteranceId) {
                Log.d(TAG, "coś nie tak");
            }

            @Override
            public void onStart(String utteranceId) {

                Log.d(TAG, "zacząłem robić");
            }
        });


        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toSpeak = "Spód migdałowo - cynamonowy: \n" +
                        "150 g zmielonych migdałów \n" +
                        "50 g cukru pudru \n" +
                        "1 duże białko (40 g)\n" +
                        "25 g mąki pszennej \n" +
                        "1 łyżeczka cynamonu \n" +
                        "25 g masła, roztopionego ";
                b1.setText("Pause");

                if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
                    playMediaPlayer(1);
                    b1.setText("Speak");
                    return;
                }


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    speakSpeech(toSpeak);
//                    t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
//                    Toast.makeText(getApplicationContext(), "już powiedziałem",Toast.LENGTH_SHORT).show();
                    File destinationFile = new File( getExternalCacheDir().getAbsolutePath(), FILENAME);
//                    Toast.makeText(getApplicationContext(), destinationFile.getAbsolutePath(),Toast.LENGTH_SHORT).show();
                    String utteranceID = "wpta";

                    if(!mProcessed){
                        Toast.makeText(getApplicationContext(), toSpeak,Toast.LENGTH_SHORT).show();
                        Bundle params = new Bundle();
                        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "");
                        Toast.makeText(getApplicationContext(), "robię plik",Toast.LENGTH_SHORT).show();
                        int status = t1.synthesizeToFile(toSpeak, params, destinationFile, utteranceID);
//                        Toast.makeText(getApplicationContext(), status,Toast.LENGTH_SHORT).show();
                    }else{
                        playMediaPlayer(0);
                    }


                } else {
                    ttsUnder20(toSpeak);
                }



            }
        });

        mMediaPlayer.setOnCompletionListener(mediaPlayerCompletionListener);

    }

    @Override
    protected void onDestroy() {

        // Stop the TextToSpeech Engine
        t1.stop();

        // Shutdown the TextToSpeech Engine
        t1.shutdown();

        // Stop the MediaPlayer
        mMediaPlayer.stop();

        // Release the MediaPlayer
        mMediaPlayer.release();

        super.onDestroy();
    }


    @SuppressWarnings("deprecation")
    private void ttsUnder20(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        Log.i("asda", "metoda nr 1");
        System.out.print("wywolano mnie");
        t1.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void speakSpeech(String speech) {

        String[] splitspeech = speech.split("\\n");

        for (int i = 0; i < splitspeech.length; i++) {

            if (i == 0) { // Use for the first splited text to flush on audio stream

                t1.speak(splitspeech[i].toString().trim(),TextToSpeech.QUEUE_FLUSH, null, null);

            } else { // add the new test on previous then play the TTS

                t1.speak(splitspeech[i].toString().trim(), TextToSpeech.QUEUE_ADD, null, null);
            }

//            t1.playSilentUtterance(0, TextToSpeech.QUEUE_ADD, null);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initializeMediaPlayer(){
        String fileName = getExternalCacheDir().getAbsolutePath() + FILENAME;
        Log.d(TAG, "media sie zaczyna");

        Uri uri  = Uri.parse("file://"+fileName);

        mMediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build());

        try {
            mMediaPlayer.setDataSource(getApplicationContext(), uri);
            mMediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playMediaPlayer(int status){
        // Start Playing
        if(status==0){
            mMediaPlayer.start();
        }
        // Pause Playing
        if(status==1){
            mMediaPlayer.pause();
        }
    }

    OnCompletionListener mediaPlayerCompletionListener = new OnCompletionListener() {

        @Override
        public void onCompletion(MediaPlayer mp) {
            // Getting reference to the button btn_speek
            Button btnSpeek = (Button) findViewById(R.id.button);

            // Changing button Text to Speek
            btnSpeek.setText("Speek");
        }
    };

    @SuppressWarnings("deprecation")
    @TargetApi(15)
    public void setTts() {
        if (Build.VERSION.SDK_INT >= 15) {
            t1.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onDone(String utteranceId) {

                    Log.d(TAG, "juz zrobiłem plik");
                    // Speech file is created
                    mProcessed = true;

                    // Initializes Media Player
                    initializeMediaPlayer();

                    // Start Playing Speech
                    playMediaPlayer(0);
                }

                @Override
                public void onError(String utteranceId) {
                    Log.d(TAG, "jest błąd");
                }

                @Override
                public void onStart(String utteranceId) {

                    Log.d(TAG, "zacząłem robić plik");
                }
            });
        }
    }

    

}
