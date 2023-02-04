package com.example.finalproject;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;


public class Recipes implements Serializable {

    int id;
    String unusedIngredients, desc, imageURL, name;

    public Recipes(int recipeID, String unusedIngredients){
        id = recipeID;
        this.unusedIngredients = unusedIngredients;
        new getRecipeTask().execute();
        while(getDescription() == null){}
    }

    public String getUnusedIngredients(){
        return unusedIngredients;
    }

    public int getID(){
        return id;
    }

    public String getDescription(){
        return desc;
    }

    public String getImageURL(){
        return imageURL;
    }

    public String getName(){
        return name;
    }

    private class getRecipeTask extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Void... voids) {
            JSONObject recipes = new Spoonacular(id).getRecipeFromID();
            try {
                imageURL = recipes.getString("image");
                desc = recipes.getString("summary");
                name = recipes.getString("title");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return recipes;
        }

        @Override
        protected void onPostExecute(JSONObject JSONObject) {

            Log.d("RECIPE_FROM_ID", JSONObject.toString());
        }
    }



}
