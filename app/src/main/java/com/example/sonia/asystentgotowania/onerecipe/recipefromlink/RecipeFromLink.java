package com.example.sonia.asystentgotowania.onerecipe.recipefromlink;

import android.util.Log;

import com.example.sonia.asystentgotowania.Constants;

import org.json.JSONException;
import org.json.JSONObject;

public class RecipeFromLink {
    private static final String TAG = RecipeFromLink.class.getSimpleName();

    public static JSONObject getRecipeInJSONFromLink(String link) {
        //todo check if link is valid
        //tak chwilowo, zeby dzialalo i pokazac o co mi chodzi:
        String nazwa = "CIASTECZKA SEROWE";
        String skladniki = "ser biały / twaróg – 125 g\n" +
                "mąka pszenna – 125 g\n" +
                "masło – 125 g\n" +
                "cukier";
        String przyrzadzenie = "Masło siekamy z mąką i serem białym, zagniatamy gładkie ciasto, odstawiamy na ok. 20 minut do lodówki.\n" +
                "Po tym czasie rozwałkowujemy na ok. 4 mm, wycinamy okręgi ( lubi kwadraty za pomocą radełka ) i wkładamy na blachę wyłożoną papierem do pieczenia.\n" +
                "Posypujemy cukrem i pieczemy ok. 20min/180C";

        JSONObject przepis = new JSONObject();
        try {
            przepis.put(Constants.JSON_RECIPE_TITLE, nazwa);
            przepis.put(Constants.JSON_RECIPE_INGREDIENTS, skladniki);
            przepis.put(Constants.JSON_RECIPE_PREPARATION, przyrzadzenie);
        } catch (JSONException e) {
            Log.e(TAG, "JSON error:", e);
        }
        return przepis;
    }
}
