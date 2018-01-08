package com.example.sonia.asystentgotowania.allrecipeview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.widget.AdapterView.OnItemClickListener;

import com.example.sonia.asystentgotowania.R;


public class AllRecipesActivity extends AppCompatActivity {
    private static final String TAG = AllRecipesActivity.class.getSimpleName();

    @BindView(R.id.recipeShelf)
    GridView mRecipeShelf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_all_recipes);
        ButterKnife.bind(this);


        mRecipeShelf.setAdapter(new ImageAdapter(this));

        mRecipeShelf.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(AllRecipesActivity.this, "" + position,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

}
