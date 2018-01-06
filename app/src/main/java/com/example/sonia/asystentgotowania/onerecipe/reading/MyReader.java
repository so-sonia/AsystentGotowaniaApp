package com.example.sonia.asystentgotowania.onerecipe.reading;

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

    public MyObservableBoolean mShouldReadIngredients = new MyObservableBoolean(true);
    public MyObservableBoolean mShouldReadPreparation = new MyObservableBoolean(true);
    private boolean mProcessed = false;
    private IntToListen mMyReaderStatus; //STATUS_SPEAKING or STATUS_NOT_SPEAKING
    private String mIngredientsText;
    private String mPreparationText;

    private Context mContext;
    private final String FILENAME = "/wpta_tts.wav";
    private TextToSpeech mTextToSpeech;
    private MediaPlayer mMediaPlayer;
    private MediaPlayer.OnCompletionListener mediaPlayerCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            Log.i(TAG, "media finnished");
            mMyReaderStatus.setValue(STATUS_NOT_SPEAKING);
        }
    };

    public MyReader(Context context, String ingredientsText, String preparationText) {
        Log.i(TAG, "new MyReader");
        mContext = context;
        mMyReaderStatus = new IntToListen(STATUS_NOT_SPEAKING);
        mMediaPlayer = new MediaPlayer();
        initializeTextToSpeech();
        mMediaPlayer.setOnCompletionListener(mediaPlayerCompletionListener);
        mIngredientsText = ingredientsText;
        mPreparationText = preparationText;
    }

    public void read() {
        if (!mProcessed) {
            String toSpeak = "";
            if (mShouldReadIngredients.getValue()) {
                toSpeak = toSpeak.concat(mIngredientsText + "\n");
            }
            if (mShouldReadPreparation.getValue()) {
                toSpeak = toSpeak.concat(mPreparationText + "\n");
            }
            Log.i(TAG, "to speak: " + toSpeak);

            File destinationFile = new File(mContext.getExternalCacheDir().getAbsolutePath(), FILENAME);
            Log.i(TAG, "file created");
            String utteranceID = "wpta";
            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "");
            Log.i(TAG, "params.putString ");
            mTextToSpeech.synthesizeToFile(toSpeak, params, destinationFile, utteranceID);
            Log.i(TAG, "mTextToSpeech");
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
            Log.e(TAG, "media problem: ", e);
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
                mMediaPlayer.reset(); // to set source in media, media must be in idle state (Android Developer)
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

    private void stopReading() {
        Log.i(TAG, "stop reading");
        if (mMediaPlayer.isPlaying()) {
            pauseMedia();
        }
        mMediaPlayer.reset();
        initializeMediaPlayer();
        mProcessed = false;
    }

    public void readButtonsChanged(boolean readIngredients, boolean readPreparation) {
        stopReading();
        mShouldReadIngredients.setValue(readIngredients);
        mShouldReadPreparation.setValue(readPreparation);
        //read();
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
