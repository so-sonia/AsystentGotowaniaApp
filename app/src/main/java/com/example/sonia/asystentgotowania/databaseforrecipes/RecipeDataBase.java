package com.example.sonia.asystentgotowania.databaseforrecipes;


import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;


@Database(entities = {RecipeEntity.class}, version = 1)
public abstract class RecipeDataBase extends RoomDatabase {
    public abstract DaoRecipe daoRecipe();
}