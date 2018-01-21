package com.example.sonia.asystentgotowania.onerecipe.questions;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;
import java.util.ArrayList;

import com.example.sonia.asystentgotowania.Constants;



public class QuestionsListener {
    private static final String TAG = Constants.APP_TAG.concat(QuestionsListener.class.getSimpleName());
    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechRecognizerIntent;
    private Context mContext;
    private QuestionsHandler mQuestionsHandler;

    public QuestionsListener(Context context, String instruction, String preparation) {
        mContext = context;
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(mContext);
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                mContext.getPackageName());

        SpeechRecognitionListener listener = new SpeechRecognitionListener();
        mSpeechRecognizer.setRecognitionListener(listener);
        mQuestionsHandler = new QuestionsHandler(mContext, instruction, preparation);
    }

    public void start() {
        String message = "Zadaj mi pytanie:\n" +
                "Czy występuje... składnik?\n" +
                "Ile potrzeba... składnik?\n" +
                "O liczbę porcji\n" +
                "O czas przygotowania\n" +
                "O temperaturę";
        final Toast invite = Toast.makeText(mContext, message, Toast.LENGTH_LONG);
        invite.setGravity(Gravity.CENTER, 0, 0);

        new CountDownTimer(5000, 1000)
        {

            public void onTick(long millisUntilFinished) {invite.show();}
            public void onFinish() {invite.show();}

        }.start();
    }

    public void stop(){
        mSpeechRecognizer.stopListening();
    }

    public void finish() {
        mSpeechRecognizer.destroy();
    }

    public void ask() {
        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
    }

    protected class SpeechRecognitionListener implements RecognitionListener
    {

        @Override
        public void onBeginningOfSpeech()
        {
            Log.d(TAG, "onBeginingOfSpeech");
        }

        @Override
        public void onBufferReceived(byte[] buffer)
        {

        }

        @Override
        public void onEndOfSpeech()
        {
            Log.d(TAG, "onEndOfSpeech");
        }

        @Override
        public void onError(int error)
        {

            Log.d(TAG, "error = " + error);
        }

        @Override
        public void onEvent(int eventType, Bundle params)
        {

        }

        @Override
        public void onPartialResults(Bundle partialResults)
        {

        }

        @Override
        public void onReadyForSpeech(Bundle params)
        {
            Log.d(TAG, "onReadyForSpeech"); //$NON-NLS-1$
        }

        @Override
        public void onResults(Bundle results)
        {
            Log.d(TAG, "onResults"); //$NON-NLS-1$
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            Log.d(TAG, matches.get(0));
            if (matches != null) {
                mQuestionsHandler.ask(matches.get(0));
            }
            // matches are the return values of speech recognition engine
            // Use these values for whatever you wish to do
        }

        @Override
        public void onRmsChanged(float rmsdB)
        {
        }
    }

}



