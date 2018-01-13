package com.example.sonia.asystentgotowania.databaseforrecipes;

import org.json.JSONException;
import org.json.JSONObject;


public class RecipeParser {
    public static final String JSON_RECIPE_ID = "recipe_id";
    public static final String JSON_RECIPE_TITLE = "recipe_title";
    public static final String JSON_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String JSON_RECIPE_PREPARATION = "recipe_preparation";
    public static final String JSON_RECIPE_PICTURETITLE= "picture_title";

    public static JSONObject recipeEntityToJSON(RecipeEntity recipeEntity) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(JSON_RECIPE_ID, recipeEntity.getUid());
            jsonObject.put(JSON_RECIPE_TITLE, recipeEntity.getTitle());
            jsonObject.put(JSON_RECIPE_INGREDIENTS, recipeEntity.getIngredients());
            jsonObject.put(JSON_RECIPE_PREPARATION, recipeEntity.getPreparation());
            jsonObject.put(JSON_RECIPE_PICTURETITLE, recipeEntity.getPictureTitle());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static RecipeEntity JSONToRecipeEntity(JSONObject jsonObject) {
        RecipeEntity recipeEntity = null;
        if (jsonObject != null) {
            try {
                long uid;
                try {
                    uid = jsonObject.getLong(JSON_RECIPE_ID);
                } catch (JSONException e) {
                    uid = -1;
                }
                String title = jsonObject.getString(JSON_RECIPE_TITLE);
                String ingredients = jsonObject.getString(JSON_RECIPE_INGREDIENTS);
                String preparation = jsonObject.getString(JSON_RECIPE_PREPARATION);
                String pictureTitle = jsonObject.getString(JSON_RECIPE_PICTURETITLE);
                if (uid < 0) {
                    recipeEntity = new RecipeEntity(title, ingredients, preparation, pictureTitle);
                } else {
                    recipeEntity = new RecipeEntity(uid, title, ingredients, preparation, pictureTitle);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return recipeEntity;
    }
}
