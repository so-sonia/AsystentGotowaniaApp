package com.example.sonia.asystentgotowania.databaseforrecipes;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class RecipeEntity {
    @PrimaryKey(autoGenerate = true)
    private long uid;
    private String title;
    private String ingredients;
    private String preparation;

    public RecipeEntity(String title, String ingredients, String preparation) {
        this.title = title;
        this.ingredients = ingredients;
        this.preparation = preparation;
    }


    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getPreparation() {
        return preparation;
    }

    public void setPreparation(String preparation) {
        this.preparation = preparation;
    }
}
