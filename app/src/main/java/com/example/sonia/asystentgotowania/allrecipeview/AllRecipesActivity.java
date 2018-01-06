package com.example.sonia.asystentgotowania.allrecipeview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.sonia.asystentgotowania.R;


public class AllRecipesActivity extends AppCompatActivity {
    private static final String TAG = AllRecipesActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_all_recipes);
    }

}
