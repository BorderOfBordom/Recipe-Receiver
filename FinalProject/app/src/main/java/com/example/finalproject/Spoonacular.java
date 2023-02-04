package com.example.finalproject;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class Spoonacular {
    String ingredients;
    URL url;
    URLConnection connection;
    InputStream stream;
    int id;
    final private String API_KEY = "c0c00cea6c604b278e0ccccadab47b55";

    public Spoonacular(String ingredients){
        this.ingredients = ingredients;
    }
    public Spoonacular(int id){
        this.id = id;
    }

    public JSONArray getRecipesSearchResult(){
        JSONArray j = null;
        try {
            url = new URL("https://api.spoonacular.com/recipes/findByIngredients?ingredients=" + ingredients + "&apiKey=" + API_KEY + "&ranking=2&number=1");
            Log.d("LINK", "https://api.spoonacular.com/recipes/findByIngredients?ingredients=" + ingredients + "&apiKey=" + API_KEY + "&ranking=2&number=1");
            connection = url.openConnection();
            stream = connection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(stream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String a = bufferedReader.readLine();
            j = new JSONArray(a);
            bufferedReader.close();
            inputStreamReader.close();
        } catch (IOException | JSONException e){
            Log.d("ERROR", "Error in getRecipesSearchResult() method in Spoonacular class");
            e.printStackTrace();
        }
        return j;
    }

    public JSONObject getRecipeFromID() {
        JSONObject j = null;
        try {
            url = new URL("https://api.spoonacular.com/recipes/"+id+"/information?includeNutrition=false&apiKey="+API_KEY);
            Log.d("LINK", "https://api.spoonacular.com/recipes/"+id+"/information?includeNutrition=false&apiKey="+API_KEY);
            connection = url.openConnection();
            stream = connection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(stream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String a = bufferedReader.readLine();
            j = new JSONObject(a);
        } catch (IOException | JSONException e){
            Log.d("ERROR", "Error in getRecipesSearchResult() method in Spoonacular class");
            e.printStackTrace();
        }
        return j;
    }
}
