package com.example.sonia.asystentgotowania.allrecipeview;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.example.sonia.asystentgotowania.Constants;
import com.example.sonia.asystentgotowania.R;
import com.example.sonia.asystentgotowania.databaseforrecipes.DataBaseSingleton;
import com.example.sonia.asystentgotowania.databaseforrecipes.RecipeEntity;
import com.example.sonia.asystentgotowania.databaseforrecipes.RecipeParser;
import com.example.sonia.asystentgotowania.onerecipe.RecipeActivity;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AllRecipesActivity extends AppCompatActivity {
    private static final String TAG = Constants.APP_TAG.concat(AllRecipesActivity.class.getSimpleName());

    @BindView(R.id.recipeShelf)
    GridView mRecipeShelf;
    final RecipeAdapter recipeAdapter = new RecipeAdapter(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "AllRecipesActivity create");
        setContentView(R.layout.view_all_recipes);
        ButterKnife.bind(this);

        mRecipeShelf.setAdapter(recipeAdapter);

        mRecipeShelf.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                Intent i = new Intent(getApplicationContext(), RecipeActivity.class);
                String recipeInJSONString = RecipeParser.recipeEntityToJSON(recipeAdapter.getRecipe(position)).toString();
                i.putExtra(Constants.INTENT_WITH_RECIPE_FROM_MAIN, recipeInJSONString);
                startActivity(i);
            }
        });

        mRecipeShelf.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view,
                                           final int position, long id) {
                Log.i(TAG, "delete item");
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Confirm");
                builder.setMessage("Do you want to delete recipe "
                        + recipeAdapter.getRecipe(position).getTitle() + "?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        recipeAdapter.removeRecipeAt(position);
                        RecipeEntity recipeEntity = recipeAdapter.getRecipe(position);
                        new RecipeDeleter().execute(String.valueOf(recipeEntity.getUid()),
                                recipeEntity.getTitle(), recipeEntity.getIngredients(),
                                recipeEntity.getPreparation());
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
                return true;
            }
        });
    }

    private class RecipeDeleter extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            Log.d(TAG, "zapisuję do Bazy Danych");
            long recipeID = Long.parseLong(params[0]);
            String recipeTitle = params[1];
            String ingredients = params[2];
            String preparation = params[3];

            Log.d(TAG, "RecipeDeleter: " + recipeID + "\n" + recipeTitle + "\n" + ingredients + "\n" + preparation);
            RecipeEntity recipe = new RecipeEntity(recipeID, recipeTitle, ingredients, preparation);
            DataBaseSingleton.getInstance(getApplicationContext()).deleteRecipe(recipe);
            Log.d(TAG, "usuniety z bazy: " + recipeID + "\n" + recipeTitle + "\n" + ingredients + "\n" + preparation);
            return null;
        }

    }
}
