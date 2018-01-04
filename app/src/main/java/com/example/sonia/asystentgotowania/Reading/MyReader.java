package com.example.sonia.asystentgotowania.Reading;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.io.File;
import java.util.Locale;


public class MyReader {
    private static final String TAG = MyReader.class.getSimpleName();
    public final static int STATUS_SPEAKING = 1;
    public final static int STATUS_NOT_SPEAKING = 0;
    public IntToListen mMyReaderStatus;

    private Context mContext;
    private final String FILENAME = "/wpta_tts.wav";
    private boolean mProcessed = false;
    private TextToSpeech mTextToSpeech;
    private MediaPlayer mMediaPlayer;
    private MediaPlayer.OnCompletionListener mediaPlayerCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            Log.i(TAG, "media finnished");
            mMyReaderStatus.setValue(STATUS_NOT_SPEAKING);
        }
    };

    public MyReader(Context context) {
        mContext = context;
        mMyReaderStatus = new IntToListen(STATUS_NOT_SPEAKING);
        mMediaPlayer = new MediaPlayer();
        initializeMediaPlayer();
        initializeTextToSpeech();
        mMediaPlayer.setOnCompletionListener(mediaPlayerCompletionListener);
    }

    public void read() {
        String toSpeak = "Spód migdałowo - cynamonowy:";

        File destinationFile = new File(mContext.getExternalCacheDir().getAbsolutePath(), FILENAME);
        String utteranceID = "wpta";

        if (!mProcessed) {
            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "");
            mTextToSpeech.synthesizeToFile(toSpeak, params, destinationFile, utteranceID);
        } else {
            playMedia();
        }

    }

    public void pauseReading() {
        pauseMedia();
    }


    private void initializeMediaPlayer() {
        String fileName = mContext.getExternalCacheDir().getAbsolutePath() + FILENAME;
        Log.d(TAG, "media sie zaczyna");

        Uri uri = Uri.parse("file://" + fileName);

        mMediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());
        try {
            mMediaPlayer.setDataSource(mContext.getApplicationContext(), uri);
            mMediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeTextToSpeech() {
        mTextToSpeech = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    mTextToSpeech.setLanguage(new Locale("pl_PL"));
                }
            }
        });
        mTextToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onDone(String utteranceId) {

                Log.d(TAG, "juz zrobiłem plik");
                // Speech file is created
                mProcessed = true;

                // Initializes Media Player
                initializeMediaPlayer();
                Log.d(TAG, "wywołałem media");

                // Start Playing Speech
                playMedia();
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
    }

    private void playMedia() {
        mMyReaderStatus.setValue(STATUS_SPEAKING);
        mMediaPlayer.start();
    }

    private void pauseMedia() {
        mMyReaderStatus.setValue(STATUS_NOT_SPEAKING);
        mMediaPlayer.pause();
    }

    public int getmMyReaderStatus() {
        return mMyReaderStatus.getValue();
    }

    public IntToListen getStatusObservable() {
        return mMyReaderStatus;
    }

    public void killReader() {
        // Stop the TextToSpeech Engine
        mTextToSpeech.stop();

        // Shutdown the TextToSpeech Engine
        mTextToSpeech.shutdown();

        // Stop the MediaPlayer
        pauseMedia();

        // Release the MediaPlayer
        mMediaPlayer.release();
    }


}
