package com.example.sonia.asystentgotowania;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.example.sonia.asystentgotowania.reading.MyReader;
import com.example.sonia.asystentgotowania.recipefromlink.RecipeFromLink;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Observable;
import java.util.Observer;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RecipeActivity extends AppCompatActivity {
    private static final String TAG = RecipeActivity.class.getSimpleName();

    @BindView(R.id.btnIngredients)
    Button mbtnIngredients;
    @BindView(R.id.btnRecipe)
    Button mbtnRecipe;
    @BindView(R.id.etIngredients)
    EditText metIngredients;
    @BindView(R.id.etRecipe)
    EditText metRecipe;
    @BindView(R.id.btnPlayPause)
    Button mbtnPlayPause;

    @BindDrawable(R.drawable.ic_pause)
    Drawable mpauseIcon;
    @BindDrawable(R.drawable.ic_play)
    Drawable mplayIcon;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_recipe);
        ButterKnife.bind(this);

        mmyReader = new MyReader(getApplicationContext());
        mmyReader.getStatusObservable().addObserver(mStatusObserver);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null && "text/plain".equals(type)) {
            getRecipeFormLink(intent); // Handle text being sent
        }
    }

    private void getRecipeFormLink(Intent intent) {
        String link = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (link != null) {
            JSONObject recipeJson = RecipeFromLink.getRecipeInJSONFromLink(link);
            if (recipeJson != null) {
                String strIngreds = "";
                String strprepare = "";
                try {
                    strIngreds = recipeJson.getString(Constants.JSON_RECIPE_INGREDIENTS);
                } catch (JSONException e) {
                    Log.e(TAG, "JSON error, no ingreds in recipe:", e);
                }
                try {
                    strprepare = recipeJson.getString(Constants.JSON_RECIPE_PREPARATION);
                } catch (JSONException e) {
                    Log.e(TAG, "JSON error, no prepare in recipe:", e);
                }
                metIngredients.setText(strIngreds);
                metRecipe.setText(strprepare);
            }
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

}
