package com.andranym.skyblockbazaarstatus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.games.Games;

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

//        //When I mess up and need to reset the balance
//        SharedPreferences mySPrefs = PreferenceManager.getDefaultSharedPreferences(this);
//        SharedPreferences.Editor editor1 = mySPrefs.edit();
//        editor1.remove("notificationThresholdCoins");
//        editor1.apply();

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
        double bazaarTax1 = 1-((double)(settings.getInt("personalBazaarTaxAmount",1250))/1000/100);
        stonkBalance = Double.parseDouble(settings.getString("stonkBalance","1000000"));
        stonkBalanceCheat = Double.parseDouble(settings.getString("stonkBalanceCheat","1000000"));
        //Add a few more coins because I think 1 million isn't a lot.
        if(settings.getBoolean("notGivenBoostOfCoins",true)){
            stonkBalance += 99000000;
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("notGivenBoostOfCoins",false);
            editor.putString("stonkBalance",Double.toString(stonkBalance));
            editor.commit();
        }
        //endregion

        //regionAward achievement if you found the secret trick
        if (bazaarTax1 > 1) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("solvedChallenge6", true);
            editor.putBoolean("solvedChallenge6display",true);
            Toast.makeText(getApplicationContext(),"I see you found the secret workaround. I'm not going to force you to actually" +
                    " get 1 billion coins, you already got the achievement. But I won't let you actually cheat so the leaderboard remains steady.",Toast.LENGTH_LONG).show();
            editor.commit();
        } else if (stonkBalance >= 1000000000) {
            //seriously
            Toast.makeText(getApplicationContext(),"Oh wow, you actually did it, you became a billionaire, you absolute madlad.",Toast.LENGTH_LONG).show();
        }
        //endregion

        //region Leader board for max coins
        try {
            Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                    .submitScore(getString(R.string.leaderboard_most_coins_earned), (long) (stonkBalance/100000));
        } catch(Exception e){
            //do nothing
        }
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(StonkActivity.this,  android.R.layout.simple_spinner_dropdown_item, products);
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
                boolean isPinned = settings.getBoolean("isPinned" + actualName,false);
                if (currentlyOwned > 0 || isPinned) {
                    //uses the "original name" provided by the API, this ensures it
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
                        break;
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

        //region Update amount of money
        new Thread() {
            @Override
            public void run() {
                boolean keepRunning = true;
                while (keepRunning) {
                    String money = settings.getString("stonkBalance","1000000");
                    String display = "Current Balance: " + addCommasAdjusted(money);
                    new update().execute(display);
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        //endregion
    }

    class update extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... strings) {
            return strings[0];
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            txtStonkBalance.setText(s);
        }
    }

    //Add commas method, adjusted to work with decimal places at the end
    public String addCommasAdjusted(String digits) {

        if(digits.equals("1.0E8")){
            return "100,000,000";
        }

        //Store the part with the decimal
        String[] digitsSplit = digits.split("\\.");
        String beforeDecimal = digitsSplit[0];
        String afterDecimal;
        try {
            afterDecimal = digitsSplit[1];
        } catch (Exception e){
            afterDecimal = "0";
        }

        String result = "";
        for (int i=1; i <= beforeDecimal.length(); ++i) {
            char ch = beforeDecimal.charAt(beforeDecimal.length() - i);
            if (i % 3 == 1 && i > 1) {
                result = "," + result;
            }
            result = ch + result;
        }

        //Put the decimals back on before returning
        if (afterDecimal.length() < 2) {
            result = result + "." + afterDecimal;
        } else {
            result = result + "." + afterDecimal.substring(0,1);
        }
        return result;
    }
}