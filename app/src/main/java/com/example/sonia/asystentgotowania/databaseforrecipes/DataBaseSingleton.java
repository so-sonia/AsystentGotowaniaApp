package com.example.sonia.asystentgotowania.databaseforrecipes;

import android.arch.persistence.room.Room;
import android.content.Context;

import java.util.List;


public class DataBaseSingleton {
    private static final String TAG = DataBaseSingleton.class.getSimpleName();
    private static DataBaseSingleton ourInstance;
    private static RecipeDataBase mRecipeDataBase;

    public static DataBaseSingleton getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new DataBaseSingleton(context);
        }
        return ourInstance;
    }

    private DataBaseSingleton(Context context) {
        mRecipeDataBase = Room.databaseBuilder(context,
                RecipeDataBase.class, "database-recipes").build();
    }

    //must be done NOT on UI thread
    public List<RecipeEntity> getAllRecipes() {
        return mRecipeDataBase.daoRecipe().getAll();
    }

    //must be done NOT on UI thread
    public void saveRecipe(RecipeEntity recipe) {
        mRecipeDataBase.daoRecipe().insertRecipe(recipe);
    }

    //must be done NOT on UI thread
    public void updateRecipe(RecipeEntity recipe) {
        mRecipeDataBase.daoRecipe().updateRecipe(recipe);
    }

    //must be done NOT on UI thread
    public void deleteRecipe(RecipeEntity recipe) {
        mRecipeDataBase.daoRecipe().deleteRecipe(recipe);
    }


}
