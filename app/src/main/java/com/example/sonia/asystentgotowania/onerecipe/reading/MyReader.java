package com.example.sonia.asystentgotowania.onerecipe.reading;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.example.sonia.asystentgotowania.Constants;

import java.io.File;
import java.util.Locale;


public class MyReader {
    private static final String TAG = Constants.APP_TAG.concat(MyReader.class.getSimpleName());
    public final static int STATUS_SPEAKING = 1;
    public final static int STATUS_NOT_SPEAKING = 0;

    private final static int TYPE_INGREDIENTS = 1;
    private final static int TYPE_NOT_INGREDIENTS = 0;

    public MyObservableBoolean mShouldReadIngredients = new MyObservableBoolean(true);
    public MyObservableBoolean mShouldReadPreparation = new MyObservableBoolean(true);
    private boolean mProcessed = false;
    private boolean mIngredientsProcessed = false;
    private boolean mInstructionProcessed = false;
    private MyObservableInteger mMyReaderStatus; //STATUS_SPEAKING or STATUS_NOT_SPEAKING
    private MyObservableInteger mMyReaderType; //TYPE_INGREDIENTS or TYPE_NOT_INGREDIENTS
    private String mIngredientsText;
    private String mPreparationText;

    private Context mContext;
    private final String FILENAME_ingredients = "/wpta_ingredients.wav";
    private final String FILENAME_instructions = "/wpta_instructions.wav";
    private TextToSpeech mTextToSpeech;
    private MediaPlayer mMediaPlayer;
    private MediaPlayer.OnCompletionListener mediaPlayerCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            Log.i(TAG, "media finnished");
            if (mMyReaderType.getValue() == TYPE_INGREDIENTS && mShouldReadPreparation.getValue()) {
                mMyReaderType.setValue(TYPE_NOT_INGREDIENTS);
                initializeMediaPlayer();
                playMedia();
            } else {
                mMyReaderType.setValue(TYPE_NOT_INGREDIENTS);
                mMyReaderStatus.setValue(STATUS_NOT_SPEAKING);
            }
        }
    };

    public MyReader(Context context, String ingredientsText, String preparationText) {
        Log.i(TAG, "new MyReader");
        mContext = context;
        mMyReaderStatus = new MyObservableInteger(STATUS_NOT_SPEAKING);
        mMyReaderType = new MyObservableInteger(TYPE_INGREDIENTS);
        mMediaPlayer = new MediaPlayer();
        initializeTextToSpeech();
        mMediaPlayer.setOnCompletionListener(mediaPlayerCompletionListener);
        mIngredientsText = ingredientsText;
        mPreparationText = preparationText;
    }

    public void prepareFiles() {
        if (!mProcessed) {
            String toSpeakIngredients = "";
            String toSpeakInstructions = "";
            if (!mIngredientsProcessed) {
                toSpeakIngredients = toSpeakIngredients.concat(mIngredientsText + "\n");
                Log.i(TAG, "to speak ingredients: " + toSpeakIngredients);
                File destinationFileIngredients = new File(
                        mContext.getExternalCacheDir().getAbsolutePath(), FILENAME_ingredients);
                Log.i(TAG, "file ingredients created");
                String utteranceID = "wpta_ingredients";
                Bundle params = new Bundle();
                params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "");
                Log.i(TAG, "params.putString ");
                mTextToSpeech.synthesizeToFile(toSpeakIngredients, params, destinationFileIngredients, utteranceID);
                Log.i(TAG, "mTextToSpeech ingredients");
            }
            if (!mInstructionProcessed) {
                toSpeakInstructions = toSpeakInstructions.concat(mPreparationText + "\n");
                Log.i(TAG, "to speak instructions: " + toSpeakInstructions);
                File destinationFileInstructions = new File(
                        mContext.getExternalCacheDir().getAbsolutePath(), FILENAME_instructions);
                Log.i(TAG, "file instructions created");
                String utteranceID = "wpta_instructions";
                Bundle params = new Bundle();
                params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "");
                Log.i(TAG, "params.putString ");
                mTextToSpeech.synthesizeToFile(toSpeakInstructions, params, destinationFileInstructions, utteranceID);
                Log.i(TAG, "mTextToSpeech instructions");
            }
        }
    }

    public void read() {
        if (mProcessed) {
            playMedia();
        }
    }

    public void pauseReading() {
        pauseMedia();
    }

    private void initializeMediaPlayer() {
        mMediaPlayer.reset();
        String fileToPlay;

        if (mShouldReadIngredients.getValue() && mMyReaderType.getValue() == TYPE_INGREDIENTS) {
            fileToPlay = FILENAME_ingredients;
        } else if (mShouldReadPreparation.getValue()) {
            fileToPlay = FILENAME_instructions;
        } else {
            mMyReaderStatus.setValue(STATUS_NOT_SPEAKING);
            return;
        }

        String fileName = mContext.getExternalCacheDir().getAbsolutePath() + fileToPlay;
        Log.d(TAG, "media sie zaczyna: " + fileName);

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
                prepareFiles();
            }
        });
        mTextToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onDone(String utteranceId) {

                //TO DO: mProcessed = true, tylko jeżeli oba pliki zostały juz zrobione
                Log.d(TAG, "juz zrobiłem plik " + utteranceId);
                // Speech file is created
                if (utteranceId.equals("wpta_ingredients")) {
                    mIngredientsProcessed = true;
                }

                if (utteranceId.equals("wpta_instructions")) {
                    mInstructionProcessed = true;
                }

                if (mIngredientsProcessed & mInstructionProcessed) {
                    mProcessed = true;
                    // Initializes Media Player
                    mMediaPlayer.reset(); // to set source in media, media must be in idle state (Android Developer)
                    initializeMediaPlayer();
                    Log.d(TAG, "wywołałem media");

                    // Start Playing Speech
                    playMedia();

                }

            }

            @Override
            public void onError(String utteranceId) {
                Log.d(TAG, "coś nie tak");
            }

            @Override
            public void onStart(String utteranceId) {

                Log.d(TAG, "zacząłem robić" + utteranceId);
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

    public void readButtonsChanged(boolean readIngredients, boolean readPreparation) {
        pauseMedia();
        mShouldReadIngredients.setValue(readIngredients);
        mShouldReadPreparation.setValue(readPreparation);
        if (mShouldReadIngredients.getValue()) {
            mMyReaderType.setValue(TYPE_INGREDIENTS);
        }
        initializeMediaPlayer();
        Log.i(TAG, "readButtonsChanged: ing: " + mShouldReadIngredients.getValue()
                + " prep: " + mShouldReadPreparation.getValue());
    }

    public int getmMyReaderStatus() {
        return mMyReaderStatus.getValue();
    }

    public MyObservableInteger getStatusObservable() {
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

    public void compareContent(String ingredients, String instructions) {
        if (!ingredients.equals(mIngredientsText)) {
            mIngredientsText = ingredients;
            mProcessed = false;
            mIngredientsProcessed = false;
        }
        if (!instructions.equals(mPreparationText)) {
            mProcessed = false;
            mInstructionProcessed = false;
            mPreparationText = instructions;
        }
        prepareFiles();

    }


}
