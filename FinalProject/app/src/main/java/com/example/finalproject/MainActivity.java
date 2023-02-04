package com.example.finalproject;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.finalproject.ml.MobilenetV110224Quant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    String ingredients = "";
    Bitmap imageOfIngredient;
    JSONArray receivedRecipes;
    EditText typedIngredients;

    public static int CAMERA_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button photoButton = findViewById(R.id.id_photoButton);
        Button typedIngredientsButton = findViewById(R.id.id_typedIngredientsButton);
        typedIngredients = findViewById(R.id.id_ingredientEditText);


        typedIngredients.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ingredients = charSequence.toString();
                Log.d("INGREDIENTS_STRING", ingredients);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Requesting access to the camera to take photos of ingredients
                if(ContextCompat.checkSelfPermission(MainActivity.this, "android.permission.CAMERA") != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.CAMERA"}, 1);
                }

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, MainActivity.CAMERA_REQUEST_CODE);

            }
        });

        typedIngredientsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ingredients.trim();
                ingredients.replace(" ", ",+");
                new getRecipeTask().execute();

                String missedIngredientsString = null;
                int ID = -1;
                try {
                    while(receivedRecipes == null){}

                    JSONObject r = (JSONObject) receivedRecipes.get(0);
                    ID = r.getInt("id");
                    if(r.getInt("missedIngredientCount") != 0) {
                        missedIngredientsString = "You are missing these ingredients: ";
                        JSONArray missedIngredients = (JSONArray) r.get("missedIngredients");
                        for (int i = 0; i < r.getInt("missedIngredientCount"); i++) {
                            if(i == r.getInt("missedIngredientCount")-1)
                                missedIngredientsString += ((JSONObject)missedIngredients.get(i)).getString("name");
                            else{
                                missedIngredientsString += ((JSONObject)missedIngredients.get(i)).getString("name") + ", ";
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Recipes recipes = new Recipes(ID, missedIngredientsString);

                Intent intent = new Intent(MainActivity.this, ListOfRecipes.class);
                intent.putExtra("RECIPE", recipes);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == CAMERA_REQUEST_CODE){
                imageOfIngredient = (Bitmap) data.getExtras().get("data");
                String predictedIngredient = "";
                try {
                    MobilenetV110224Quant model = MobilenetV110224Quant.newInstance(this);

                    Bitmap img = Bitmap.createScaledBitmap(imageOfIngredient, 224, 224, true);

                    TensorImage tensorImage = new TensorImage(DataType.UINT8);
                    tensorImage.load(img);
                    ByteBuffer byteBuffer = tensorImage.getBuffer();

                    TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.UINT8);

                    inputFeature0.loadBuffer(byteBuffer);

                    // Runs model inference and gets result.
                    MobilenetV110224Quant.Outputs outputs = model.process(inputFeature0);
                    TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                    Integer max = getMax(outputFeature0.getFloatArray());

                    Log.d("TESTTAG", "LINE 177");

                    String text = "";
                    try{
                        InputStream is = getAssets().open("labels_mobilenet_quant_v1_224.txt");
                        InputStreamReader inputStreamReader = new InputStreamReader(is);
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        String line = "";
                        while((line = bufferedReader.readLine()) != null) {
                            text += line + "\n";
                        }
                    } catch (IOException ex){
                        ex.printStackTrace();
                    }
                    Log.d("LABELS_FILE",text);
                    String str = text;
                    String[] splitStr = str.trim().split("\n");
                    String x = splitStr[max];
                    predictedIngredient = x;

                    // Releases model resources if no longer used.
                    model.close();
                } catch (IOException e) {
                    // TODO Handle the exception
                }

                ingredients = ingredients + predictedIngredient + " ";
                typedIngredients.setText(ingredients);
            }
        }
    }

    public int getMax(float[] a)
    {
        int index = 0;
        if (a.length <= 0)
            throw new IllegalArgumentException("The array is empty");
        float max = a[0];
        for (int i = 1; i < a.length; i++) {
            if (a[i] > max) {
                max = a[i];
                index = i;
            }
        }
        Log.d("TAG", String.valueOf(index));
        return index;
    }


    private class getRecipeTask extends AsyncTask<Void, Void, JSONArray>{

        @Override
        protected JSONArray doInBackground(Void... voids) {
            JSONArray recipes = new Spoonacular(ingredients).getRecipesSearchResult();
            receivedRecipes = recipes;
            return recipes;
        }

        @Override
        protected void onPostExecute(JSONArray JSONArray) {
            Log.d("RECIPES", JSONArray.toString());
        }
    }
}