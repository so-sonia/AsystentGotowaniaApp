package com.example.sonia.asystentgotowania.onerecipe;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.example.sonia.asystentgotowania.R;
import com.example.sonia.asystentgotowania.onerecipe.reading.MyJSONhelper;
import com.example.sonia.asystentgotowania.onerecipe.reading.MyReader;
import com.example.sonia.asystentgotowania.onerecipe.recipefromlink.RecipeFromLink;

import org.json.JSONObject;

import java.util.Observable;
import java.util.Observer;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

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

    @BindDrawable(R.drawable.ic_pause)
    Drawable mpauseIcon;
    @BindDrawable(R.drawable.ic_play)
    Drawable mplayIcon;


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

        final Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        //recipe from internet
        if (Intent.ACTION_SEND.equals(action) && type != null && "text/plain".equals(type)) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try  {
                        getRecipeFormLink(intent); // Handle text being sent
                    } catch (Exception e) {
                        e.printStackTrace();
                     }
                }
            });
            thread.start();
        } else {
            mIngredientsText = "mąka\n sól\n woda\n wino\n";
//            mPreparationText = "zmieszaj mąkę i wodę. \nNalej sobie kieliszek wina. \n";
            mPreparationText = "Dynię obrać ze skórki, usunąć nasiona, miąższ pokroić w kostkę. Ziemniaki obrać i też pokroić w kostkę. \n" +
                    "W większym garnku na maśle zeszklić pokrojoną w kosteczkę cebulę oraz obrany i pokrojony na plasterki czosnek. Dodać dynię i ziemniaki, doprawić solą, wsypać kurkumę i dodać imbir. Smażyć co chwilę mieszając przez ok. 5 minut.\n" +
                    "Wlać gorący bulion, przykryć i zagotować. Zmniejszyć ogień do średniego i gotować przez ok. 10 minut. \n" +
                    "Świeżego pomidora sparzyć, obrać, pokroić na ćwiartki, usunąć szypułki oraz nasiona z komór. Miąższ pokroić w kosteczkę i dodać do zupy. Pomidory z puszki są już gotowe do użycia, wystarczy dodać do potrawy.\n" +
                    "Wymieszać i gotować przez 5 minut, do miękkości warzyw. Zmiksować w blenderze z dodatkiem mleka.\n";
        }
        setRecipeTextsInViews();

        mmyReader = new MyReader(getApplicationContext(), "składniki:\n" + mIngredientsText,
                "przygotowanie:\n" + mPreparationText);
        mmyReader.getStatusObservable().addObserver(mStatusObserver);
        mmyReader.mShouldReadPreparation.addObserver(mButtonsObserver);
        mmyReader.mShouldReadIngredients.addObserver(mButtonsObserver);
        setButtonsColors();
    }

    private void getRecipeFormLink(Intent intent) {
        String link = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (link != null) {
            RecipeFromLink newRecipe = new RecipeFromLink(link);
            JSONObject recipeJson = newRecipe.getRecipeInJSON();
            mIngredientsText = MyJSONhelper.getIngredientsFromJSON(recipeJson);
            mPreparationText = MyJSONhelper.getPreparationFromJSON(recipeJson);
        }
    }

    private void setRecipeTextsInViews() {
        metIngredients.setText(mIngredientsText);
        metRecipe.setText(mPreparationText);
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

//    @OnLongClick(R.id.etIngredients)


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
            mbtnIngredients.setBackgroundColor(Color.GREEN);
        } else {
            mbtnIngredients.setBackgroundColor(Color.GRAY);
        }
        if (mmyReader.mShouldReadPreparation.getValue()) {
            mbtnPreparation.setBackgroundColor(Color.GREEN);
        } else {
            mbtnPreparation.setBackgroundColor(Color.GRAY);
        }
    }
}
