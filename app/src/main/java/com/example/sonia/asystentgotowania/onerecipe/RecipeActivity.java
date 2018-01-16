package com.example.sonia.asystentgotowania.onerecipe;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.example.sonia.asystentgotowania.Constants;
import com.example.sonia.asystentgotowania.R;
import com.example.sonia.asystentgotowania.allrecipeview.AllRecipesActivity;
import com.example.sonia.asystentgotowania.databaseforrecipes.DataBaseSingleton;
import com.example.sonia.asystentgotowania.databaseforrecipes.RecipeEntity;
import com.example.sonia.asystentgotowania.databaseforrecipes.RecipeParser;
import com.example.sonia.asystentgotowania.onerecipe.listening.CommandsRecognitionListener;
import com.example.sonia.asystentgotowania.onerecipe.reading.MyJSONhelper;
import com.example.sonia.asystentgotowania.onerecipe.reading.MyReader;
import com.example.sonia.asystentgotowania.onerecipe.recipefromlink.RecipeFromLink;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RecipeActivity extends AppCompatActivity {
    private static final String TAG = Constants.APP_TAG.concat(RecipeActivity.class.getSimpleName());

    @BindView(R.id.btnIngredients)
    Button mbtnIngredients;
    @BindView(R.id.btnPreparation)
    Button mbtnPreparation;
    @BindView(R.id.etTitle)
    EditText metTitle;
    @BindView(R.id.etIngredients)
    EditText metIngredients;
    @BindView(R.id.etRecipe)
    EditText metRecipe;
    @BindView(R.id.btnPlayPause)
    Button mbtnPlayPause;
    @BindView(R.id.btnEdit)
    Button mbtnEdit;
    @BindView(R.id.btnSave)
    Button mbtnSave;
//    @BindView(R.id.btnAllRecipeMenu)
//    Button mbtnAllRecipeMenu;


    @BindDrawable(R.drawable.ic_pause_r)
    Drawable mpauseIcon;
    @BindDrawable(R.drawable.ic_play_r)
    Drawable mplayIcon;
    @BindDrawable(R.drawable.editing_pencil_f)
    Drawable editingIcon;
    @BindDrawable(R.drawable.check_mark_f)
    Drawable checkmarkIcon;


    //variable necessary while saving - if less than 0 recipe is inserted,
    // otherwise it exists already in database, so is updated
    static long mRecipeID = -1;
    boolean editable;
    String mTitleText;
    String mIngredientsText;
    String mPreparationText;
    String mPictureURL;
    String mPictureTitle;
    Target mTarget;
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    CommandsRecognitionListener mCommandsRecognitionListener;
    MyReader mmyReader;
    Observer mStatusObserver = new Observer() {
        @Override
        public void update(Observable observable, Object o) {
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int a = mmyReader.getmMyReaderStatus();
                        if (a == MyReader.STATUS_NOT_SPEAKING) {
                            mbtnPlayPause.setBackground(mplayIcon);
                        } else if (a == MyReader.STATUS_SPEAKING) {
                            mbtnPlayPause.setBackground(mpauseIcon);
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "error msg:", e);
            }
        }
    };
    Observer mButtonsObserver = new Observer() {
        @Override
        public void update(Observable observable, Object o) {
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setButtonsColors();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "error msg:", e);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.view_recipe);
        ButterKnife.bind(this);
        editable = false;

        //microfone permission
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }

        new putRecipeInView().execute();
        mCommandsRecognitionListener = new CommandsRecognitionListener(getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        if (id == R.id.btnAllRecipeMenu) {
            Intent i = new Intent(getApplicationContext(), AllRecipesActivity.class);
            startActivity(i);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class putRecipeInView extends AsyncTask<Void, Void, Void> {

        String title = "";
        String ingredients = "";
        String instructions = "";
        String pictureTitle = "";

        private void getRecipeFormLink(Intent intent) {
            String link = intent.getStringExtra(Intent.EXTRA_TEXT);
            Log.d(TAG, link);
            if (link != null) {
                RecipeFromLink newRecipe = new RecipeFromLink(link);
                JSONObject recipeJson = newRecipe.getRecipeInJSON();
                ingredients = MyJSONhelper.getIngredientsFromJSON(recipeJson);
                instructions = MyJSONhelper.getPreparationFromJSON(recipeJson);
                title = MyJSONhelper.getTitleFromJSON(recipeJson);
                mPictureURL = MyJSONhelper.getPictureURLFromJSON(recipeJson);
            }
        }

        private void setRecipeTextsInViews() {
            metTitle.setText(mTitleText);
            metIngredients.setText(mIngredientsText);
            metRecipe.setText(mPreparationText);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Intent intent = getIntent();
            String action = intent.getAction();
            String type = intent.getType();
            if (Intent.ACTION_SEND.equals(action) && type != null && "text/plain".equals(type)) {
                getRecipeFormLink(intent);
                mRecipeID = -1;
            } else if (intent.hasExtra(Constants.INTENT_WITH_RECIPE_FROM_MAIN)) {

                String intentMessage = intent.getStringExtra(Constants.INTENT_WITH_RECIPE_FROM_MAIN);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(intentMessage);
                } catch (JSONException e) {
                    Log.d(TAG, "JSON: ", e);
                }
                RecipeEntity recipeEntity = RecipeParser.JSONToRecipeEntity(jsonObject);
                mRecipeID = recipeEntity.getUid();
                title = recipeEntity.getTitle();
                ingredients = recipeEntity.getIngredients();
                instructions = recipeEntity.getPreparation();
                pictureTitle = recipeEntity.getPictureTitle();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mTitleText = title;
            mIngredientsText = ingredients;
            mPreparationText = instructions;
            mPictureTitle = pictureTitle;
            setRecipeTextsInViews();
            mmyReader = new MyReader(getApplicationContext(), "składniki:\n" + mIngredientsText,
                    "przygotowanie:\n" + mPreparationText);
            mmyReader.getStatusObservable().addObserver(mStatusObserver);
            mmyReader.mShouldReadPreparation.addObserver(mButtonsObserver);
            mmyReader.mShouldReadIngredients.addObserver(mButtonsObserver);
            setButtonsColors();

            mCommandsRecognitionListener.setOnGoThread(mRunnableGo, mRunnableStop,
                    mRunnableIng, mRunnablePrep, mRunnableAll);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mmyReader.killReader();
        if (mCommandsRecognitionListener != null) {
            mCommandsRecognitionListener.destroy();
        }
    }

    @OnClick(R.id.btnPlayPause)
    public void readText() {
        Log.i(TAG, "readText");
        if (mmyReader.getmMyReaderStatus() == MyReader.STATUS_NOT_SPEAKING) {
            mmyReader.read();
        } else if (mmyReader.getmMyReaderStatus() == MyReader.STATUS_SPEAKING) {
            mmyReader.pauseReading();
        }
    }

    @OnClick(R.id.btnEdit)
    public void editRecipe() {
        if (editable) {
            if (getCurrentFocus() != null) {
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
            metTitle.setFocusable(false);
            metTitle.setClickable(false);
            metTitle.setFocusableInTouchMode(false);
            metTitle.setCursorVisible(false);
            metIngredients.setFocusable(false);
            metIngredients.setClickable(false);
            metIngredients.setFocusableInTouchMode(false);
            metIngredients.setCursorVisible(false);
            metRecipe.setFocusable(false);
            metRecipe.setClickable(false);
            metRecipe.setFocusableInTouchMode(false);
            metRecipe.setCursorVisible(false);
            mbtnEdit.setBackground(editingIcon);
            editable = false;


            mTitleText = metTitle.getText().toString();
            mIngredientsText = metIngredients.getText().toString();
            mPreparationText = metRecipe.getText().toString();
            Log.d(TAG, "Recipe changed: " + mTitleText + "\n" + mIngredientsText + "\n" + mPreparationText);
            mmyReader.compareContent("składniki:\n" + mIngredientsText,
                    "przygotowanie:\n" + mPreparationText);
        } else {
            metTitle.setFocusable(true);
            metTitle.setClickable(true);
            metTitle.setFocusableInTouchMode(true);
            metTitle.setCursorVisible(true);
            metIngredients.setFocusable(true);
            metIngredients.setClickable(true);
            metIngredients.setFocusableInTouchMode(true);
            metIngredients.setCursorVisible(true);
            metRecipe.setFocusable(true);
            metRecipe.setClickable(true);
            metRecipe.setFocusableInTouchMode(true);
            metRecipe.setCursorVisible(true);
            mbtnEdit.setBackground(checkmarkIcon);
            editable = true;
        }
    }


    @OnClick(R.id.btnIngredients)
    public void readIngredientsButtonClicked() {
        mmyReader.readButtonsChanged(!mmyReader.mShouldReadIngredients.getValue(), mmyReader.mShouldReadPreparation.getValue());
    }

    @OnClick(R.id.btnPreparation)
    public void readPreparationButtonClicked() {
        mmyReader.readButtonsChanged(mmyReader.mShouldReadIngredients.getValue(), !mmyReader.mShouldReadPreparation.getValue());
    }

    public void setButtonsColors() {
        if (mmyReader.mShouldReadIngredients.getValue()) {
            int myColor = ContextCompat.getColor(getApplicationContext(), R.color.colorAccent);
            mbtnIngredients.setBackgroundColor(myColor);
        } else {
            mbtnIngredients.setBackgroundColor(Color.GRAY);
        }
        if (mmyReader.mShouldReadPreparation.getValue()) {
            int myColor = ContextCompat.getColor(getApplicationContext(), R.color.colorAccent);
            mbtnPreparation.setBackgroundColor(myColor);
        } else {
            mbtnPreparation.setBackgroundColor(Color.GRAY);
        }
    }

    @OnClick(R.id.btnSave)
    public void saveRecipe() {
        if (mRecipeID < 0) {
            String pictureTitle = mTitleText + ".jpeg";
            mPictureTitle = pictureTitle.replace(" ", "_");
        }

        Log.i(TAG, "try to save: " + String.valueOf(mRecipeID) + "\n" + mTitleText + "\n" +
                mIngredientsText + "\n" + mPreparationText + "\n" + mPictureTitle);
        new RecipeSaver().execute(String.valueOf(mRecipeID), mTitleText, mIngredientsText,
                mPreparationText, mPictureTitle);

//        Picasso.with(getApplicationContext()).setLoggingEnabled(true);

        if (mRecipeID < 0) {
            if (!"".equals(mPictureURL)) {
                Log.d(TAG, "adres zdjęcia " + mPictureURL);
                Log.d(TAG, "zapisane jako " + mPictureTitle);


                mTarget = picassoImageTarget(getApplicationContext(), mPictureTitle);

                Picasso mBuilder = new Picasso.Builder(getApplicationContext())
                        .loggingEnabled(true)
                        .indicatorsEnabled(true)
                        .downloader(new OkHttp3Downloader(getApplicationContext()))
                        .build();

                mBuilder.load(mPictureURL)
                        .noFade().resize(200, 200).centerCrop()
                        .into(mTarget);
            }
        }
        Intent i = new Intent(getApplicationContext(), AllRecipesActivity.class);
        startActivity(i);
        finish();
    }

    private final Target picassoImageTarget(Context context, final String imageName) {
        Log.d("picassoImageTarget", " picassoImageTarget");
        ContextWrapper cw = new ContextWrapper(context);
        final File directory = cw.getExternalFilesDir("pictures");
        Log.d("picassoImageTarget", directory.getAbsolutePath());
        return new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                Log.d("picassoImageTarget", "picture loaded");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final File myImageFile = new File(directory, imageName); // Create image file
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(myImageFile);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        Log.i("image", "image saved to >>>" + myImageFile.getAbsolutePath());

                    }
                }).start();
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                if (placeHolderDrawable != null) {
                }
            }
        };
    }

    private class RecipeSaver extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            Log.d(TAG, "zapisuję do Bazy Danych");
            long recipeID = Long.parseLong(params[0]);
            String recipeTitle = params[1];
            String ingredients = params[2];
            String preparation = params[3];
            String pictureTitle = params[4];

            Log.d(TAG, "RecipeSaver: " + recipeID + "\n" + recipeTitle + "\n" + ingredients + "\n" + preparation
                    + "\n" + pictureTitle);
            //not in database at this moment
            if (recipeID < 0) {
                RecipeEntity recipe = new RecipeEntity(recipeTitle, ingredients, preparation, pictureTitle);
                DataBaseSingleton.getInstance(getApplicationContext()).saveRecipe(recipe);
            }
            //already in database
            else {
                RecipeEntity recipe = new RecipeEntity(recipeID, recipeTitle, ingredients, preparation, pictureTitle);
                DataBaseSingleton.getInstance(getApplicationContext()).updateRecipe(recipe);
            }
            Log.d(TAG, "zapisany do bazy: " + recipeID + "\n" + recipeTitle + "\n" + ingredients + "\n" + preparation
                    + "\n" + pictureTitle);
            return null;
        }

    }


    Runnable mRunnableGo = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "go runnable");
            if (mmyReader.getmMyReaderStatus() == MyReader.STATUS_NOT_SPEAKING) {
                mmyReader.read();
            }
        }
    };
    Runnable mRunnableStop = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "stop runnable");
            if (mmyReader.getmMyReaderStatus() == MyReader.STATUS_SPEAKING) {
                mmyReader.pauseReading();
            }
        }
    };

    Runnable mRunnableIng = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "runnableIng runnable");
            mmyReader.readButtonsChanged(true, false);
            if (mmyReader.getmMyReaderStatus() == MyReader.STATUS_NOT_SPEAKING) {
                mmyReader.read();
            }

        }
    };
    Runnable mRunnablePrep = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "runnablePrep runnable");
            mmyReader.readButtonsChanged(false, true);
            if (mmyReader.getmMyReaderStatus() == MyReader.STATUS_NOT_SPEAKING) {
                mmyReader.read();
            }
        }
    };
    Runnable mRunnableAll = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "runnableAll runnable");
            mmyReader.readButtonsChanged(true, true);
            if (mmyReader.getmMyReaderStatus() == MyReader.STATUS_NOT_SPEAKING) {
                mmyReader.read();
            }
        }
    };
}
