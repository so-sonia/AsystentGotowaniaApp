package com.example.sonia.asystentgotowania;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;

import com.example.sonia.asystentgotowania.Reading.MyReader;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_recipe);
        ButterKnife.bind(this);

        mmyReader = new MyReader(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        mmyReader.killReader();
        super.onDestroy();
    }


    @OnClick(R.id.btnPlayPause)
    public void readText(Button button) {
        if (mmyReader.getStatus() == MyReader.STATUS_NOT_SPEAKING) {
            mmyReader.read();
            button.setBackground(mpauseIcon);
        } else if (mmyReader.getStatus() == MyReader.STATUS_SPEAKING) {
            button.setBackground(mplayIcon);
            mmyReader.pauseReading();
        }
    }
}
