package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ListOfRecipes extends AppCompatActivity {

    TextView missingIngredients, desc, name;
    ImageView imageOfRecipe;
    Button returnToTitleScreen;
    Recipes recipe;
    Drawable recipeDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_recipes);
        recipe = (Recipes) getIntent().getSerializableExtra("RECIPE");
        desc = findViewById(R.id.id_description);
        name = findViewById(R.id.id_nameOfRecipe);
        missingIngredients = findViewById(R.id.id_missingIngredients);
        imageOfRecipe = findViewById(R.id.id_imageOfRecipe);
        returnToTitleScreen = findViewById(R.id.id_returnButton);

        desc.setText(recipe.getDescription());
        name.setText(recipe.getName());
        missingIngredients.setText(recipe.getUnusedIngredients());

        new getRecipeTask().execute();
        while(recipeDrawable==null){}
        imageOfRecipe.setImageDrawable(recipeDrawable);


        returnToTitleScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private class getRecipeTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            JSONObject recipes = new Spoonacular(recipe.getID()).getRecipeFromID();

            try {
                URL iconLink = new URL(recipes.getString("image"));
                HttpURLConnection connection = (HttpURLConnection) iconLink.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream is = connection.getInputStream();
                recipeDrawable = Drawable.createFromStream(is, null);
                is.close();

            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}