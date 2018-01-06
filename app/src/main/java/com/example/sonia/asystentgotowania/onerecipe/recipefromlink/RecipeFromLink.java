package com.example.sonia.asystentgotowania.onerecipe.recipefromlink;

import android.util.Log;

import com.example.sonia.asystentgotowania.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import org.jsoup.*;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Document;

public class RecipeFromLink {
    private boolean knownWebsite;
    private int instructionIndex;
    private String websiteName;
    private HashMap<String, WebsiteData> knownWebsites;
    private String instructions;
    private String ingredients;
    private String recipeName;
    private final String TAG = RecipeFromLink.class.getSimpleName();

    public RecipeFromLink(String link){
        HashMap<String, WebsiteData> knownWebsites = new HashMap<String, WebsiteData>();
        knownWebsites.put("kwestiasmaku", new WebsiteData("kwestiasmaku", ".group-skladniki",
                ".group-przepis", ".group-wskazowki"));
        knownWebsites.put("kotlet", new WebsiteData("kotlet", ".ingredients", ".steps-ul", ".lists"));
        knownWebsites.put("mojewypieki", new WebsiteData("mojewypieki", "[id~=post[0-9]+]"));
        knownWebsites.put("olgasmile", new WebsiteData("olgasmile", ".entry-content"));
        knownWebsites.put("jadlonomia", new WebsiteData("jadlonomia", "[id=RecipeCard]"));
        knownWebsites.put("lawendowydom.com", new WebsiteData("lawendowydom.com", "[id=entry]"));

        this.knownWebsites = knownWebsites;

        try {

            Document doc = getWebsiteContent(link);
            String websiteName = getWebsiteName(link);

            this.websiteName = websiteName;

            if (knownWebsites.get(websiteName) != null) {
                this.knownWebsite = true;
            }
            parseDocument(doc);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getRecipeInJSON() {
        //tak chwilowo, zeby dzialalo i pokazac o co mi chodzi:
//        String nazwa = "CIASTECZKA SEROWE";
//        String skladniki = "ser biały / twaróg – 125 g\n" +
//                "mąka pszenna – 125 g\n" +
//                "masło – 125 g\n" +
//                "cukier";
//        String przyrzadzenie = "Masło siekamy z mąką i serem białym, zagniatamy gładkie ciasto, odstawiamy na ok. 20 minut do lodówki.\n" +
//                "Po tym czasie rozwałkowujemy na ok. 4 mm, wycinamy okręgi ( lubi kwadraty za pomocą radełka ) i wkładamy na blachę wyłożoną papierem do pieczenia.\n" +
//                "Posypujemy cukrem i pieczemy ok. 20min/180C";

        JSONObject przepis = new JSONObject();
        try {
            przepis.put(Constants.JSON_RECIPE_TITLE, this.recipeName);
            przepis.put(Constants.JSON_RECIPE_INGREDIENTS, this.ingredients);
            przepis.put(Constants.JSON_RECIPE_PREPARATION, this.instructions);
        } catch (JSONException e) {
            Log.e(TAG, "JSON error:", e);
        }
        return przepis;
    }



    public Document getWebsiteContent(String link) throws IOException {
        Document doc = Jsoup.connect(link).get();
        return(doc);
    }

    public void parseDocument(Document doc){

        String title = getRecipeTitle(doc);
        this.recipeName = title;
        String ingredients;
        String instructions;

        if (this.knownWebsite) {

            WebsiteData webData = this.knownWebsites.get(this.websiteName);
            Elements ingre = doc.select(webData.getIngredientClass());
            String ingreCleaned = cleanParser(ingre);

            if (webData.hasRecipeClass()) {
                ingredients = getOnlyIngredients(ingreCleaned);

                Elements reci = doc.select(webData.getRecipeClass());
                instructions = getOnlyInstructions(cleanParser(reci));

            } else {
                ingredients = getIngredients(ingreCleaned);
                instructions = getInstructions(ingreCleaned);
            }


        } else {
            String content;

            Elements ingre = doc.select("[class~=print]");

            if ("".equals(ingre.text()) | ingre.text().length()< 100) {
                ingre = doc.select("[class~=post-?[0-9]+], [id~=post-?[0-9]+]");

                if ("".equals(ingre.text())) {

                    Element body = doc.body();
                    content = cleanParser(body);

                } else {
                    content = cleanParser(ingre);

                }

            } else {
                content = cleanParser(ingre);
                System.out.println(content);

            }

            ingredients = getIngredients(content);
            instructions = getInstructions(content);
        }

        this.ingredients = ingredients;
        this.instructions = instructions;
    }

    public String getRecipeTitle(Document doc) {
        Elements title = doc.select("title");
        String titleClean = cleanParser(title);
        int indexSlash;
        if ((indexSlash = titleClean.indexOf("|"))>0){
            titleClean = titleClean.substring(0, indexSlash);
        }
        return(titleClean);
    }

    /**cleanParser gets rid of the HTML tags preserving the new line characters. It also cleans extra new line characters to
     get pretty output (first making sure all multiple spaces are change to one space, so they are not considered
     as more than one whitespace character)*/

    public String cleanParser(Element dirty){
        dirty.select("br").append("\\n");
        dirty.select("p").prepend("\\n\\n");
        String cleaned = dirty.html().replaceAll("\\\\n", "\n");
        cleaned = Jsoup.clean(cleaned, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
        cleaned = cleaned.replaceAll(" {2,}", " ").replaceAll("(\\s){2,}", "\n").replaceAll("&nbsp;", " ");
        return cleaned;
    }

    public String cleanParser(Elements dirty){
        dirty.select("br").append("\\n");
        dirty.select("p").prepend("\\n\\n");
        String cleaned = dirty.html().replaceAll("\\\\n", "\n");
        cleaned = Jsoup.clean(cleaned, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
        cleaned = cleaned.replaceAll(" {2,}", " ").replaceAll("(\\s){2,}", "\n").replaceAll("&nbsp;", " ");
        return cleaned;
    }

    /**extracts the name of the website from the link*/
    public String getWebsiteName(String website){
        String name = website.replaceAll(".*(www\\.|http://)(.*)\\.(pl|com|tv|info|blogspot|blog).*", "$2");
        return name;
    }

    //TO DO: parser other powinien sprawdzać, czy nie ma tam też składników i/lub instrukcji

    private int makeInstructionIndex(String content, int startIndex){
        int indexInstructions;
        Pattern p = Pattern.compile(".*(wykonanie|przepis|przygotowani(a|e)|instrukcj(a|e))( |:|\n).*");  // insert your pattern here
        Matcher m = p.matcher(content.substring(startIndex));
        if (m.find()) {
            int position = m.start() + startIndex;
            indexInstructions = content.lastIndexOf("\n", position) + 1;
        } else {
            indexInstructions = -1;
        }
        int indexEnd = getRecipeEnd(content, startIndex);

        if (indexInstructions > indexEnd) {
            indexInstructions = indexEnd;
        }
        this.instructionIndex = indexInstructions;
        return(indexInstructions);
    }

    public String getIngredients(String content, String name){

        String contentLow = content.toLowerCase();
        int indexIngredients;
        int indexEnd;
        String ingredients;

        if ((indexIngredients = getIngredientsIndex(contentLow, 0)) >= 0){
            if (getIngredientsIndex(contentLow, indexIngredients+1) >= 0) {
                //TO DO: multiple składniki function
            }
            indexIngredients = contentLow.indexOf("\n", indexIngredients) + 1;
        } else if ("".equals(name)){
            //TO DO: no składniki no name function
            indexIngredients = 0;
        } else {
            indexIngredients = contentLow.indexOf(name);
            indexIngredients = contentLow.indexOf("\n", indexIngredients) + 1;
        }

        indexEnd =  makeInstructionIndex(contentLow, indexIngredients);

        if (indexEnd<0){
            indexEnd = getRecipeEnd(contentLow, indexIngredients);
        }

        if (indexEnd<indexIngredients + 10) {
            indexEnd = content.length();
        }

        ingredients = content.substring(indexIngredients, indexEnd);
        return(ingredients);
    }

    public String getIngredients(String content){
        return(getIngredients(content, ""));
    }

    public String getOnlyIngredients(String content) {
        int indexIngredients = getIngredientsIndex(content.toLowerCase(), 0);
        indexIngredients = content.indexOf("\n", indexIngredients) + 1;
        String ingredients = content.substring(indexIngredients, content.length());
        return(ingredients);
    }

    public int getIngredientsIndex(String content, int startIndex){
        int ingredientsIndex;
        Pattern p = Pattern.compile(".*(?<!(wszystkie|pozostałe|suche|mokre) )(składniki|skład)( |:|\n)(?!((z|wy)mieszać|zmiksować|powinny)).*");
        Matcher m = p.matcher(content.substring(startIndex));
        if (m.find()) {
            ingredientsIndex = m.start() + startIndex;
        } else {
            ingredientsIndex = -1;
        }
        return(ingredientsIndex);
    }

    public String getOnlyInstructions(String content){
        int startIndex = makeInstructionIndex(content.toLowerCase(), 0);
        startIndex = content.indexOf("\n", startIndex) + 1;
        String instruction = content.substring(startIndex, content.length());
        return(instruction);
    }

    public int getRecipeEnd(String content, int startIndex){
        int endIndex;
        Pattern p = Pattern.compile(".*(link|smacznego|komentarze?|następny|poprzedni|popularne|zobacz|dodaj do ulubionych|"
                + "podziel się|udostępnij|autor|etykiet(a|y)|kategori(a|e)|tagi?).*");
        Matcher m = p.matcher(content.substring(startIndex));
        if (m.find()) {
            endIndex = m.start() + startIndex;
            endIndex = content.lastIndexOf("\n", endIndex) + 1;
        } else {
            endIndex = content.length();
        }
        return(endIndex);
    }

    public String getInstructions(String content) {
        String instruction;
        if (this.instructionIndex<0){
            instruction = "";
        } else {
            int startIndex = content.indexOf("\n", this.instructionIndex) + 1;
            int endIndex = getRecipeEnd(content.toLowerCase(), this.instructionIndex-1);

            if (startIndex>=endIndex) {
                instruction = "";
            } else {
                instruction = content.substring(startIndex, endIndex);
            }
        }
        return(instruction);
    }
}
