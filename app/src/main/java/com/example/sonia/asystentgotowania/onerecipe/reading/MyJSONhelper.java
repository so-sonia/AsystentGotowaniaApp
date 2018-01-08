package com.example.sonia.asystentgotowania.onerecipe.reading;


import android.util.Log;

import com.example.sonia.asystentgotowania.Constants;

import org.json.JSONException;
import org.json.JSONObject;

public class MyJSONhelper {
    private static final String TAG = MyJSONhelper.class.getSimpleName();

    public static String getIngredientsFromJSON(JSONObject recipe) {
        String ingredients = "";
        try {
            String ingFromJson = recipe.getString(Constants.JSON_RECIPE_INGREDIENTS);
            ingredients = ingredients.concat(ingFromJson);
        } catch (JSONException e) {
            Log.e(TAG, "JSON error in getIngredientsToReadFromJSON", e);
        }
        Log.i(TAG, "from json ingredients: " + ingredients);
        return ingredients;
    }

    public static String getPreparationFromJSON(JSONObject recipe) {
        String preparation = "";
        try {
            String prepFromJson = recipe.getString(Constants.JSON_RECIPE_PREPARATION);
            preparation = preparation.concat(prepFromJson);
        } catch (JSONException e) {
            Log.e(TAG, "JSON error in getPreparationToReadFromJSON", e);
        }
        Log.i(TAG, "from json preparation: " + preparation);
        return preparation;
    }

    public static String getTitleFromJSON(JSONObject recipe) {
        String title = "";
        try {
            String prepFromJson = recipe.getString(Constants.JSON_RECIPE_TITLE);
            title = title.concat(prepFromJson);
        } catch (JSONException e) {
            Log.e(TAG, "JSON error in getTitleFromJSON", e);
        }
        Log.i(TAG, "from json title: " + title);
        return title;
    }

    public static String getPictureURLFromJSON(JSONObject recipe) {
        String pictureURL = "";
        try {
            String prepFromJson = recipe.getString(Constants.JSON_RECIPE_PICTUREURL);
            pictureURL = pictureURL.concat(prepFromJson);
        } catch (JSONException e) {
            Log.e(TAG, "JSON error in getPictureURLFromJSON", e);
        }
        Log.i(TAG, "from json pictureURL: " + pictureURL);
        return pictureURL;
    }
}
