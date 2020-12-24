package com.andranym.skyblockbazaarstatus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class FavoriteActivity extends AppCompatActivity {

    TextView txtTimeFavorite;
    Spinner spinnerFavorites;
    RecyclerView recFavorites;
    Button btnAddTop;
    Button btnAddBottom;
    FavoriteRecViewAdapter RecAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        setTitle("Favorites");

        //region Load data for this activity
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final String priceDataString = settings.getString("currentData",null);
        final String favoritesString = settings.getString("favoritesList","[\"DIAMOND\",\"CATALYST\",\"STOCK_OF_STONKS\"]");
        final boolean solved5 = settings.getBoolean("solvedChallenge5",false);
        //endregion

        //regionCreate UI elements
        txtTimeFavorite = (TextView)findViewById(R.id.txtTimeFavorite);
        spinnerFavorites = (Spinner)findViewById(R.id.spinnerFavorites);
        recFavorites = (RecyclerView)findViewById(R.id.recFavorites);
        btnAddBottom = (Button)findViewById(R.id.btnAddBottom);
        btnAddTop = (Button)findViewById(R.id.btnAddTop);
        //endregion

        //regionUpdate time
        new Thread(){
            @Override
            public void run() {
                Boolean checkTime = true; //always true
                while(checkTime){
                    //Get time updated from the string from earlier
                    JSONObject priceDataJSON = null;
                    try {
                        assert priceDataString != null;
                        priceDataJSON = new JSONObject(priceDataString);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    long unixTimeDataUpdated = 0;
                    try {
                        unixTimeDataUpdated = priceDataJSON.getLong("lastUpdated");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //Call the task that can actually access UI elements
                    new checkTime().execute(unixTimeDataUpdated);
                    //Take a break so this thread isn't too resource intensive
                    SystemClock.sleep(30000);
                }
            }
        }.start();
        //endregion

        //region Get JSON array of all the products
        JSONObject priceData = null;
        try {
            priceData = new JSONObject(priceDataString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayList<String> productsAlphabetical = new ArrayList<>();
        JSONObject productsList = null;
        try {
            assert priceData != null;
            productsList = priceData.getJSONObject("products");
            Iterator<String> productIterator = productsList.keys();
            while (productIterator.hasNext()){
                //Multiple products have an "enchanted" version. I figured it makes the most sense
                //for ENCHANTED_PORK to appear next to PORK if you were sorting alphabetically
                //Therefore I move enchanted to the back
                String current_product = productIterator.next();

                String possible_correction = new FixBadNames().fix(current_product);
                if (possible_correction != null) {
                    current_product = possible_correction;
                }

                if (current_product.length() > 9 && current_product.substring(0,9).equals("ENCHANTED")){
                    current_product = current_product.substring(10) + "_ENCHANTED";
                }
                productsAlphabetical.add(current_product);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Actually put in alphabetical order
        Collections.sort(productsAlphabetical);
        //Put Enchanted back to the beginning
        ArrayList<String> products = new ArrayList<>();
        for(String product :productsAlphabetical) {
            if ((product.length() > 10) && product.substring(product.length() - 10).equals("_ENCHANTED")) {
                product = "ENCHANTED_" + product.substring(0, product.length() - 10);
            }
            products.add(product);
        }
        //endregion

        //region Put the list of items in the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(FavoriteActivity.this,  android.R.layout.simple_spinner_dropdown_item, products);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFavorites.setAdapter(adapter);
        //endregion

        //regionLoad the favorites, and populate an ArrayList with them.
        JSONArray listOfFavorites = null;
        try {
            listOfFavorites = new JSONArray(favoritesString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Place to store all the Favorite objects
        final ArrayList<Favorite> favorites = new ArrayList<>();
        //Store just the names so that actual favorites can be restored
        final ArrayList<String> favoriteNames = new ArrayList<>();
        for (int i = 0; i < listOfFavorites.length(); ++i) {
            String currentProduct = null;
            try {
                currentProduct = listOfFavorites.getString(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String possibleCorrection = new FixBadNames().unfix(currentProduct);
            String itemName = currentProduct;
            if (possibleCorrection != null) {
                currentProduct = possibleCorrection;
            }
            //Get the current prices
            double buyPrice = 0;
            double sellPrice = 0;
            try {
                int buyOrders = productsList.getJSONObject(currentProduct).getJSONObject("quick_status").getInt("buyOrders");
                int sellOrders = productsList.getJSONObject(currentProduct).getJSONObject("quick_status").getInt("buyOrders");
                if (buyOrders != 0) {
                    buyPrice = productsList.getJSONObject(currentProduct).getJSONArray("buy_summary").getJSONObject(0).getDouble("pricePerUnit");
                } else {
                    buyPrice = 0;
                }
                if (sellOrders != 0) {
                    sellPrice = productsList.getJSONObject(currentProduct).getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit");
                } else {
                    sellPrice = 0;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String itemDesc = "Buy Price: " + addCommasAdjusted(Double.toString(buyPrice)) +
                            "\nSell Price: " + addCommasAdjusted(Double.toString(sellPrice));
            Favorite currentFavorite = new Favorite(itemName,itemDesc);
            favoriteNames.add(itemName);
            favorites.add(currentFavorite);
        }
        //endregion

        //region RecyclerView thing
        RecAdapter = new FavoriteRecViewAdapter(this);
        recFavorites.setAdapter(RecAdapter);
        recFavorites.setLayoutManager(new LinearLayoutManager(this));
        RecAdapter.setFavorites(favorites);
        RecAdapter.setSaveData(favoriteNames);
        //endregion

        //region Buttons to add to favorites
        final JSONObject finalProductsList = productsList;
        //Allow changes to the actual list
        final SharedPreferences.Editor editor = settings.edit();
        final Toast fail = Toast.makeText(this,"You must complete challenge 5 to use this button.\nFor now, use the add to bottom button.",Toast.LENGTH_LONG);
        final Toast added = Toast.makeText(this,"Item added to favorites",Toast.LENGTH_SHORT);
        //Add product to top
        btnAddTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (solved5) {
                    String newItem = spinnerFavorites.getSelectedItem().toString();
                    String possibleCorrection = new FixBadNames().unfix(newItem);
                    String itemName = newItem;
                    if (possibleCorrection != null) {
                        newItem = possibleCorrection;
                    }
                    //Get the current prices
                    double buyPrice = 0;
                    double sellPrice = 0;
                    try {
                        int buyOrders = finalProductsList.getJSONObject(newItem).getJSONObject("quick_status").getInt("buyOrders");
                        int sellOrders = finalProductsList.getJSONObject(newItem).getJSONObject("quick_status").getInt("buyOrders");
                        if (buyOrders != 0) {
                            buyPrice = finalProductsList.getJSONObject(newItem).getJSONArray("buy_summary").getJSONObject(0).getDouble("pricePerUnit");
                        } else {
                            buyPrice = 0;
                        }
                        if (sellOrders != 0) {
                            sellPrice = finalProductsList.getJSONObject(newItem).getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit");
                        } else {
                            sellPrice = 0;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String itemDesc = "Buy Price: " + addCommasAdjusted(Double.toString(Round2(buyPrice))) +
                            "\nSell Price: " + addCommasAdjusted(Double.toString(Round2(sellPrice)));
                    Favorite currentFavorite = new Favorite(itemName, itemDesc);

                    favorites.add(0, currentFavorite);
                    favoriteNames.add(0, itemName);
                    RecAdapter = new FavoriteRecViewAdapter(FavoriteActivity.this);
                    recFavorites.setAdapter(RecAdapter);
                    recFavorites.setLayoutManager(new LinearLayoutManager(FavoriteActivity.this));
                    RecAdapter.setFavorites(favorites);
                    RecAdapter.setSaveData(favoriteNames);
                    String favoritesList = new Gson().toJson(favoriteNames);
                    editor.putString("favoritesList", favoritesList);
                    editor.commit();
                    added.show();
                } else {
                    fail.show();
                }
            }
        });

        btnAddBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newItem = spinnerFavorites.getSelectedItem().toString();
                String possibleCorrection = new FixBadNames().unfix(newItem);
                String itemName = newItem;
                if (possibleCorrection != null) {
                    newItem = possibleCorrection;
                }
                //Get the current prices
                double buyPrice = 0;
                double sellPrice = 0;
                try {
                    int buyOrders = finalProductsList.getJSONObject(newItem).getJSONObject("quick_status").getInt("buyOrders");
                    int sellOrders = finalProductsList.getJSONObject(newItem).getJSONObject("quick_status").getInt("buyOrders");
                    if (buyOrders != 0) {
                        buyPrice = finalProductsList.getJSONObject(newItem).getJSONArray("buy_summary").getJSONObject(0).getDouble("pricePerUnit");
                    } else {
                        buyPrice = 0;
                    }
                    if (sellOrders != 0) {
                        sellPrice = finalProductsList.getJSONObject(newItem).getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit");
                    } else {
                        sellPrice = 0;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String itemDesc = "Buy Price: " + addCommasAdjusted(Double.toString(Round2(buyPrice))) +
                        "\nSell Price: " + addCommasAdjusted(Double.toString(Round2(sellPrice)));
                Favorite currentFavorite = new Favorite(itemName,itemDesc);
                favorites.add(currentFavorite);
                favoriteNames.add(itemName);
                RecAdapter = new FavoriteRecViewAdapter(FavoriteActivity.this);
                recFavorites.setAdapter(RecAdapter);
                recFavorites.setLayoutManager(new LinearLayoutManager(FavoriteActivity.this));
                RecAdapter.setFavorites(favorites);
                RecAdapter.setSaveData(favoriteNames);
                String favoritesList = new Gson().toJson(favoriteNames);
                editor.putString("favoritesList",favoritesList);
                editor.commit();
                added.show();
            }
        });
        //endregion

    }

    //Check time class recycled from the MainActivity
    private class checkTime extends AsyncTask<Long,Void,Integer> {

        @Override
        protected Integer doInBackground(Long... longs) {
            //Extract the UNIX timestamp from the JSON object
            long unixTimeDataUpdated = longs[0];
            long currentUnixTime = System.currentTimeMillis();
            //This gives the minutes passed
            return (int)(currentUnixTime - unixTimeDataUpdated)/1000/60;
        }

        @Override
        protected void onPostExecute(Integer minutesPassed) {
            super.onPostExecute(minutesPassed);
            if (minutesPassed == 1) {
                String setTextTo = "1 MINUTE AGO";
                txtTimeFavorite.setText(setTextTo);
            }
            if (minutesPassed > 1 && minutesPassed <= 5){
                String setTextTo = minutesPassed + " MINUTES AGO";
                txtTimeFavorite.setText(setTextTo);
            }
            if (minutesPassed > 5 && minutesPassed <= 60) {
                String setTextTo = minutesPassed + " MINUTES AGO";
                txtTimeFavorite.setText(setTextTo);
                //Set to Orange
                txtTimeFavorite.setTextColor(Color.parseColor("#ff8519"));
            }
            if (minutesPassed > 60) {
                String setTextTo = minutesPassed + " MINUTES AGO";
                //Set to Red
                txtTimeFavorite.setTextColor(Color.parseColor("#ed1818"));
                txtTimeFavorite.setText(setTextTo);
            }
        }
    }

    //Add commas method, adjusted to work with decimal places at the end
    public String addCommasAdjusted(String digits) {
        //Store the part with the decimal
        String afterDecimal = digits.substring(digits.length()-2);
        //Run original code on the raw string with the decimal part cut off
        String beforeDecimal = digits.substring(0,digits.length()-2);

        String result = "";
        for (int i=1; i <= beforeDecimal.length(); ++i) {
            char ch = beforeDecimal.charAt(beforeDecimal.length() - i);
            if (i % 3 == 1 && i > 1) {
                result = "," + result;
            }
            result = ch + result;
        }

        //Put the decimals back on before returning
        result = result + afterDecimal;
        return result;
    }

    //Quick method for removing pesky floating point imprecision decimals
    public double Round2(double input) {
        BigDecimal bd = BigDecimal.valueOf(input);
        bd = bd.setScale(1, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}