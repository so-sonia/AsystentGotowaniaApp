package com.example.sonia.asystentgotowania.reading;


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
}
