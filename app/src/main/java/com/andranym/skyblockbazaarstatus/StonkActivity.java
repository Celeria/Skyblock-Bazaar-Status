package com.andranym.skyblockbazaarstatus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class StonkActivity extends AppCompatActivity {

    Button btnAddProduct;
    Button btnStonkHelp;
    public TextView txtStonkBalance;
    Spinner spinnerOwned;
    RecyclerView recStonks;
    StonkRecViewAdapter RecAdapter;
    TextView txtNoItems;

    public static double stonkBalance;
    public static double stonkBalanceCheat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stonk);

        setTitle("Stonks Simulator");

        //regionDeclare UI elements
        btnAddProduct = findViewById(R.id.btnAddProduct);
        btnStonkHelp = findViewById(R.id.btnStonkHelp);
        txtStonkBalance = findViewById(R.id.txtStonkBalance);
        spinnerOwned = findViewById(R.id.spinnerOwned);
        recStonks = findViewById(R.id.recStonks);
        txtNoItems = findViewById(R.id.txtNoItems);
        //endregion

        //region Import Settings
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final String priceDataString = settings.getString("currentData",null);
        stonkBalance = Double.parseDouble(settings.getString("stonkBalance","1000000"));
        stonkBalanceCheat = Double.parseDouble(settings.getString("stonkBalanceCheat","1000000"));
        //endregion

        //regionHelp button that goes to bazaar flip
        final Intent goFlip = new Intent(this,BazaarFlipActivity.class);
        btnStonkHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(goFlip);
            }
        });
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

        //region Fill spinner with each product
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),  android.R.layout.simple_spinner_dropdown_item, products);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOwned.setAdapter(adapter);
        //endregion

        //region Generate products to put in RecyclerView
            //List of items to display
            final ArrayList<Stonk> displayedStonks = new ArrayList<>();
            for(String product:productsAlphabetical){
                String possibleCorrection = new FixBadNames().unfix(product);
                String actualName;
                if(possibleCorrection!=null) {
                    actualName = possibleCorrection;
                } else {
                    actualName = product;
                }
                //Display all products the user had previously ordered, does so by checking how many owned.
                int currentlyOwned = settings.getInt("stonksOwned" + actualName,0);
                if (currentlyOwned != 0) {
                    String orderHistory = "Order History:\n" + settings.getString("orderHistory" + actualName,"");
                    Stonk currentStonk = new Stonk(product,currentlyOwned,orderHistory);
                    displayedStonks.add(currentStonk);
                    //There is a help text that tells you to add an item if you don't own items.
                    //If this piece of code runs, it is safe to remove that message
                    txtNoItems.setVisibility(View.GONE);
                }
            }
        //endregion

        //regionPut in RecyclerView
            RecAdapter = new StonkRecViewAdapter(this);
            recStonks.setAdapter(RecAdapter);
            recStonks.setLayoutManager(new LinearLayoutManager(this));
            RecAdapter.setData(displayedStonks,priceData);
        //endregion

        //region Add a product to the list
        final JSONObject finalPriceData = priceData;
        btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newProduct = spinnerOwned.getSelectedItem().toString();
                String possibleCorrection = new FixBadNames().unfix(newProduct);
                String actualName;
                if(possibleCorrection!=null) {
                    actualName = possibleCorrection;
                } else {
                    actualName = newProduct;
                }

                //make sure not to add repeats to the list
                boolean repeat = false;
                for(Stonk stonk:displayedStonks) {
                    if (stonk.getProductName().equals(newProduct)) {
                        repeat = true;
                    }
                }
                if (!repeat) {
                    int itemsOwned = settings.getInt("stonksOwned" + actualName,0);
                    String orderHistory = settings.getString("orderHistory" + actualName,"");
                    Stonk newStonk = new Stonk(newProduct,itemsOwned,orderHistory);
                    displayedStonks.add(0,newStonk);
                    RecAdapter = new StonkRecViewAdapter(StonkActivity.this);
                    recStonks.setAdapter(RecAdapter);
                    recStonks.setLayoutManager(new LinearLayoutManager(StonkActivity.this));
                    RecAdapter.setData(displayedStonks, finalPriceData);
                    //Remove the help message once an item is added
                    txtNoItems.setVisibility(View.GONE);
                }

            }
        });
        //endregion
    }
}