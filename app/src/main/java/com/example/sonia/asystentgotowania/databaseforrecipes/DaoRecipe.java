package com.example.sonia.asystentgotowania.databaseforrecipes;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface DaoRecipe {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRecipe(RecipeEntity recipe);

    @Update
    void updateRecipe(RecipeEntity recipe);

    @Delete
    void deleteRecipe(RecipeEntity recipe);

    @Query("SELECT * FROM recipeentity")
    List<RecipeEntity> getAll();
}
