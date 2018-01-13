package com.example.sonia.asystentgotowania.onerecipe.listening;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.sonia.asystentgotowania.Constants;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;


public class CommandsRecognitionListener implements RecognitionListener {
    private static final String TAG = Constants.APP_TAG.concat(CommandsRecognitionListener.class.getSimpleName());
    private static final String SEARCH_KEYWORDS = "jarvis";
    private Context mContext;

    private Runnable mRunnableGo = null;
    private Runnable mRunnableStop = null;
    private Runnable mRunnableIngredients = null;
    private Runnable mRunnablePreparation = null;
    private Runnable mRunnableAll = null;


    public CommandsRecognitionListener(Context context) {
        mContext = context;
        new SetupTask(this).execute();
    }

    private static class SetupTask extends AsyncTask<Void, Void, Exception> {
        WeakReference<CommandsRecognitionListener> commandsRecognitionListener;

        SetupTask(CommandsRecognitionListener commandsRecognitionListener) {
            Log.i(TAG, "SetupTask");
            this.commandsRecognitionListener = new WeakReference<>(commandsRecognitionListener);
        }

        @Override
        protected Exception doInBackground(Void... params) {
            Log.i(TAG, "doInBackground");
            try {
                Assets assets = new Assets(commandsRecognitionListener.get().mContext);
                File assetDir = assets.syncAssets();
                commandsRecognitionListener.get().setupRecognizer(assetDir);
            } catch (IOException e) {
                return e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Exception result) {
            if (result != null) {
                Log.i(TAG, "onPostExecute");
            } else {
                commandsRecognitionListener.get().switchSearch(SEARCH_KEYWORDS);
            }
        }
    }

    private void switchSearch(String searchName) {
        recognizer.stop();
        Log.i(TAG, "say command");
        recognizer.startListening(searchName);
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                .setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                .getRecognizer();
        recognizer.addListener(this);


        File menuGrammar = new File(assetsDir, "commands.gram");
        recognizer.addKeywordSearch(SEARCH_KEYWORDS, menuGrammar);
    }

    SpeechRecognizer recognizer;

    public void destroy() {
        Log.i(TAG, "destroy");
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onEndOfSpeech() {
        switchSearch(SEARCH_KEYWORDS);

    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        Log.i(TAG, "onPartialResult");
        if (hypothesis == null) {
            return;
        }
        String text = hypothesis.getHypstr();
        if (text.contains("stop")) {
            reactOn(mRunnableStop);
        } else if (text.contains("go")) {
            reactOn(mRunnableGo);
        } else if (text.contains("ingredients")) {
            reactOn(mRunnableIngredients);
        } else if (text.contains("preparation")) {
            reactOn(mRunnablePreparation);
        } else if (text.contains("everything")) {
            reactOn(mRunnableAll);
        }
        Log.i(TAG, "onPartialResult: " + text);
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        Log.i(TAG, "onResult");
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            if (text.contains("stop")) {
                reactOn(mRunnableStop);
            } else if (text.contains("go")) {
                reactOn(mRunnableGo);
            } else if (text.contains("ingredients")) {
                reactOn(mRunnableIngredients);
            } else if (text.contains("preparation")) {
                reactOn(mRunnablePreparation);
            } else if (text.contains("everything")) {
                reactOn(mRunnableAll);
            }
            Log.i(TAG, "Result: " + hypothesis.getHypstr());
        }
    }

    @Override
    public void onError(Exception e) {
        Log.i(TAG, "Error: ", e);
    }

    @Override
    public void onTimeout() {
        switchSearch(SEARCH_KEYWORDS);
    }

    private void reactOn(Runnable runnable) {
        Log.i(TAG, "reactOn: " + runnable.toString());
        if (runnable != null) {
            runnable.run();
        } else {
            Log.i(TAG, "runnable null");
        }
    }

    public void setOnGoThread(Runnable runnableGo, Runnable runnableStop,
                              Runnable runnableIngredients, Runnable runnablePreparation,
                              Runnable runnableAll) {
        mRunnableGo = runnableGo;
        mRunnableStop = runnableStop;
        mRunnableIngredients = runnableIngredients;
        mRunnablePreparation = runnablePreparation;
        mRunnableAll = runnableAll;
    }
}
