package com.example.sonia.asystentgotowania.onerecipe;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.example.sonia.asystentgotowania.R;
import com.example.sonia.asystentgotowania.onerecipe.reading.MyJSONhelper;
import com.example.sonia.asystentgotowania.onerecipe.reading.MyReader;
import com.example.sonia.asystentgotowania.onerecipe.recipefromlink.RecipeFromLink;

import org.json.JSONObject;

import java.net.URL;
import java.util.Observable;
import java.util.Observer;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

import static android.R.attr.action;

public class RecipeActivity extends AppCompatActivity {
    private static final String TAG = RecipeActivity.class.getSimpleName();

    @BindView(R.id.btnIngredients)
    Button mbtnIngredients;
    @BindView(R.id.btnPreparation)
    Button mbtnPreparation;
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


    @BindDrawable(R.drawable.ic_pause_r)
    Drawable mpauseIcon;
    @BindDrawable(R.drawable.ic_play_r)
    Drawable mplayIcon;
    @BindDrawable(R.drawable.editing_pencil_f)
    Drawable editingIcon;
    @BindDrawable(R.drawable.check_mark_f)
    Drawable checkmarkIcon;

    boolean editable;
    String mIngredientsText;
    String mPreparationText;
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
        setContentView(R.layout.view_recipe);
        ButterKnife.bind(this);
        editable = false;

        new putRecipeInView().execute();
    }

    private class putRecipeInView extends AsyncTask<Void, Void, Void> {

        String ingredients;
        String instructions;

        private void getRecipeFormLink(Intent intent) {
            String link = intent.getStringExtra(Intent.EXTRA_TEXT);
            Log.d(TAG, link);
//            link = link.replace("https", "http");
//            Log.d(TAG, link);
            if (link != null) {
                RecipeFromLink newRecipe = new RecipeFromLink(link);
                JSONObject recipeJson = newRecipe.getRecipeInJSON();
                ingredients = MyJSONhelper.getIngredientsFromJSON(recipeJson);
                instructions = MyJSONhelper.getPreparationFromJSON(recipeJson);
            }
        }

        private void setRecipeTextsInViews() {
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
            } else {
                ingredients = "mąka\n sól\n woda\n wino\n";
                instructions = "Dynię obrać ze skórki, usunąć nasiona, miąższ pokroić w kostkę. Ziemniaki obrać i też pokroić w kostkę. \n" +
                        "W większym garnku na maśle zeszklić pokrojoną w kosteczkę cebulę oraz obrany i pokrojony na plasterki czosnek. Dodać dynię i ziemniaki, doprawić solą, wsypać kurkumę i dodać imbir. Smażyć co chwilę mieszając przez ok. 5 minut.\n" +
                        "Wlać gorący bulion, przykryć i zagotować. Zmniejszyć ogień do średniego i gotować przez ok. 10 minut. \n" +
                        "Świeżego pomidora sparzyć, obrać, pokroić na ćwiartki, usunąć szypułki oraz nasiona z komór. Miąższ pokroić w kosteczkę i dodać do zupy. Pomidory z puszki są już gotowe do użycia, wystarczy dodać do potrawy.\n" +
                        "Wymieszać i gotować przez 5 minut, do miękkości warzyw. Zmiksować w blenderze z dodatkiem mleka.\n";
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mIngredientsText = ingredients;
            mPreparationText = instructions;
            setRecipeTextsInViews();
            mmyReader = new MyReader(getApplicationContext(), "składniki:\n" + mIngredientsText,
                    "przygotowanie:\n" + mPreparationText);
            mmyReader.getStatusObservable().addObserver(mStatusObserver);
            mmyReader.mShouldReadPreparation.addObserver(mButtonsObserver);
            mmyReader.mShouldReadIngredients.addObserver(mButtonsObserver);
            setButtonsColors();
        }
    }


    @Override
    protected void onDestroy() {
        mmyReader.killReader();
        super.onDestroy();
    }

    @OnClick(R.id.btnPlayPause)
    public void readText() {
        if (mmyReader.getmMyReaderStatus() == MyReader.STATUS_NOT_SPEAKING) {
            mmyReader.read();
        } else if (mmyReader.getmMyReaderStatus() == MyReader.STATUS_SPEAKING) {
            mmyReader.pauseReading();
        }
    }

    @OnClick(R.id.btnEdit)
    public void editRecipe(){
        if (editable) {
            if (getCurrentFocus() != null) {
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
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

            String ingredients = metIngredients.getText().toString();
            String preparation = metRecipe.getText().toString();
            mmyReader.compareContent("składniki:\n" + ingredients,
                    "przygotowanie:\n" + preparation);
        } else {
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
//            mbtnIngredients.setBackgroundColor(Color.GREEN);
        } else {
            mbtnIngredients.setBackgroundColor(Color.GRAY);
        }
        if (mmyReader.mShouldReadPreparation.getValue()) {
//            mbtnPreparation.setBackgroundColor(Color.GREEN);
            int myColor = ContextCompat.getColor(getApplicationContext(), R.color.colorAccent);
            mbtnPreparation.setBackgroundColor(myColor);
        } else {
            mbtnPreparation.setBackgroundColor(Color.GRAY);
        }
    }
}
