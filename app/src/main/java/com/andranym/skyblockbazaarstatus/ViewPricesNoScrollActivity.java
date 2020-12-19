package com.andranym.skyblockbazaarstatus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class ViewPricesNoScrollActivity extends AppCompatActivity {

    TextView txtTime;
    EditText search;
    ListView productsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_prices_no_scroll);

        //region Declare UI elements
        txtTime = (TextView)findViewById(R.id.txtTime);
        productsListView = findViewById(R.id.productsList);
        search = findViewById(R.id.editTxtSearchPrices);
        //endregion

        //regionGet the data we need for this activity
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //endregion

        //region Get the string version of the JSON data acquired in main activity
        final String priceDataString = settings.getString("currentData",null);
        //endregion

        //region Convert it into an actual JSON object for future use
        JSONObject priceData = null;
        try {
            assert priceDataString != null;
            priceData = new JSONObject(priceDataString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //endregion

        //region Display Current Prices in Listview

            //region Get list of all products
            final ArrayList<String> productsBeforeSort = new ArrayList<>();
            JSONObject productsList;
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
                    productsBeforeSort.add(current_product);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //endregion

            //region Sort products in the desired way
            //Get user preference on how data should be sorted
            int orderSettings = settings.getInt("viewPricesOrder",0);
                //region Order Alphabetically
                if (orderSettings == 1) {
                        setTitle("Bazaar Prices Sorted Alphabetically");
                        Collections.sort(productsBeforeSort);
                        final ArrayList<String> productsAlphabetical = new ArrayList<>();
                        try {
                            productsList = priceData.getJSONObject("products");
                            for (String product : productsBeforeSort) {

                                //In order to prevent problems later I then put it back the way it was before
                                if ((product.length() > 10) && product.substring(product.length() - 10).equals("_ENCHANTED")) {
                                    product = "ENCHANTED_" + product.substring(0, product.length() - 10);
                                }

                                String possible_correction = new FixBadNames().unfix(product);
                                if (possible_correction != null) {
                                    product = possible_correction;
                                }

                                //Acquire the number of buy and sell orders
                                int buyOrders = productsList.getJSONObject(product).getJSONObject("quick_status").getInt("buyOrders");
                                int sellOrders = productsList.getJSONObject(product).getJSONObject("quick_status").getInt("sellOrders");

                                double buyPrice;
                                if (buyOrders != 0) {
                                    buyPrice = productsList.getJSONObject(product).getJSONArray("buy_summary").getJSONObject(0).getDouble("pricePerUnit");
                                } else {
                                    buyPrice = 0;
                                }

                                double sellPrice;
                                if (buyOrders != 0) {
                                    sellPrice = productsList.getJSONObject(product).getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit");
                                } else {
                                    sellPrice = 0;
                                }

                                buyPrice = Round2(buyPrice);
                                sellPrice = Round2(sellPrice);
                                String buyMessage = Double.toString(buyPrice);
                                String sellMessage = Double.toString(sellPrice);

                                //Add commas to numbers for maximum readability
                                buyMessage = addCommasAdjusted(buyMessage);
                                sellMessage = addCommasAdjusted(sellMessage);
                                String buyOrdersCommas = addCommas(Integer.toString(buyOrders));
                                String sellOrdersCommas = addCommas(Integer.toString(sellOrders));

                                //Sad message for very unpopular items
                                if (buyPrice == 0.0) {
                                    buyMessage = "Nobody is selling this item ¯\\_(ツ)_/¯";
                                }
                                if (sellPrice == 0.0) {
                                    sellMessage = "Nobody is buying this item ¯\\_(ツ)_/¯";
                                }

                                possible_correction = new FixBadNames().fix(product);
                                if (possible_correction != null) {
                                    product = possible_correction;
                                }

                                String listEntry = product + "\n    Buy for: " + buyMessage + "\n    Sell for: " + sellMessage +
                                        "\n    Buy Orders: " + buyOrdersCommas + "\n    Sell Orders: " + sellOrdersCommas;
                                productsAlphabetical.add(listEntry);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        ArrayAdapter<String> productAdapter = new ArrayAdapter<>(
                                this,
                                android.R.layout.simple_list_item_1,
                                productsAlphabetical
                        );
                        productsListView.setAdapter(productAdapter);
                        //Make each item clickable, and view extra details for any item clicked.
                        productsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                final Intent viewDetails = new Intent(getApplicationContext(), ExtraDetailViewPricesActivity.class);
                                String itemName = productsBeforeSort.get(position);
                                if ((itemName.length() > 10) && itemName.substring(itemName.length() - 10).equals("_ENCHANTED")) {
                                    itemName = "ENCHANTED_" + itemName.substring(0, itemName.length() - 10);
                                }
                                viewDetails.putExtra("itemToExpand",itemName);
                                startActivity(viewDetails);
                            }
                        });
                    }
                //endregion

                //regionOrder by Popularity
                if (orderSettings == 0) {
                    setTitle("Bazaar Prices Sorted By Popularity");
                    //In order to "order by popularity" the total number of buy orders and sell orders for each item are summed up, and ordered by most
                    //popular at the top
                    ArrayList<Integer> productPopularity = new ArrayList<>();
                    try {
                        productsList = priceData.getJSONObject("products");
                        for (String product : productsBeforeSort) {
                            //In order to prevent problems later I then put it back the way it was before
                            if ((product.length() > 10) && product.substring(product.length() - 10).equals("_ENCHANTED")) {
                                product = "ENCHANTED_" + product.substring(0, product.length() - 10);
                            }

                            //Unfix any title corrections I made earlier
                            String possible_correction = new FixBadNames().unfix(product);
                            if (possible_correction != null) {
                                product = possible_correction;
                            }

                            //Acquire the number of buy and sell orders
                            int buyOrders = productsList.getJSONObject(product).getJSONObject("quick_status").getInt("buyOrders");
                            int sellOrders = productsList.getJSONObject(product).getJSONObject("quick_status").getInt("sellOrders");
                            int popularityMeasure = buyOrders + sellOrders;
                            productPopularity.add(popularityMeasure);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //Create a map of the products and their popularity
                    Map<Integer,String> popularitySort = new HashMap<>();
                    try {
                        productsList = priceData.getJSONObject("products");
                        Iterator<String> productIterator = productsList.keys();
                        int popIndex = 0;
                        for (int prices:productPopularity) {
                            popularitySort.put(productPopularity.get(popIndex),productIterator.next());
                            ++popIndex;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //Initialize a list of all the products sorted by popularity
                    final ArrayList<String> productsByPopularity = new ArrayList<>();

                    //Use TreeMap to sort by the column of integers
                    final Map<Integer, String> sortedByPopularity = new TreeMap<>();
                    sortedByPopularity.putAll(popularitySort);

                    //Populate productsByPopularity with the proper entries, (code recycled from sort Alphabetical)
                    for (int key:sortedByPopularity.keySet()) {
                        String product = sortedByPopularity.get(key);
                        try {
                            productsList = priceData.getJSONObject("products");
                            //Acquire the number of buy and sell orders
                            int buyOrders = productsList.getJSONObject(product).getJSONObject("quick_status").getInt("buyOrders");
                            int sellOrders = productsList.getJSONObject(product).getJSONObject("quick_status").getInt("sellOrders");

                            double buyPrice;
                            if (buyOrders != 0) {
                                buyPrice = productsList.getJSONObject(product).getJSONArray("buy_summary").getJSONObject(0).getDouble("pricePerUnit");
                            } else {
                                buyPrice = 0;
                            }

                            double sellPrice;
                            if (sellOrders != 0) {
                                sellPrice = productsList.getJSONObject(product).getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit");
                            } else {
                                sellPrice = 0;
                            }

                            buyPrice = Round2(buyPrice);
                            sellPrice = Round2(sellPrice);
                            String buyMessage = Double.toString(buyPrice);
                            String sellMessage = Double.toString(sellPrice);

                            //Before displaying, fix any bad names
                            String possible_correction = new FixBadNames().fix(product);
                            if (possible_correction != null) {
                                product = possible_correction;
                            }

                            //Add commas to numbers for maximum readability
                            buyMessage = addCommasAdjusted(buyMessage);
                            sellMessage = addCommasAdjusted(sellMessage);
                            String buyOrdersCommas = addCommas(Integer.toString(buyOrders));
                            String sellOrdersCommas = addCommas(Integer.toString(sellOrders));

                            //Sad message for very unpopular items
                            if (buyPrice == 0.0) {
                                buyMessage = "Nobody is selling this item ¯\\_(ツ)_/¯";
                            }
                            if (sellPrice == 0.0) {
                                sellMessage = "Nobody is buying this item ¯\\_(ツ)_/¯";
                            }

                            String listEntry = product + "\n    Buy for: " + buyMessage + "\n    Sell for: " + sellMessage +
                                    "\n    Buy Orders: " + buyOrdersCommas + "\n    Sell Orders: " + sellOrdersCommas;
                            productsByPopularity.add(listEntry);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    //It is currently sorted from lowest to highest. I want highest to lowest, so order needs to be reversed
                    Collections.reverse(productsByPopularity);

                    ArrayAdapter<String> productAdapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_list_item_1,
                            productsByPopularity
                    );
                    productsListView.setAdapter(productAdapter);
                    //View details of each item if desired
                    productsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            final Intent viewDetails = new Intent(getApplicationContext(), ExtraDetailViewPricesActivity.class);
                            //What is this abomination you may ask?
                            //In order to get the name of the item, I had sortedByPopularity, which is a treemap, which doesn't have a
                            //get index option. Therefore, in order to get the index I had to find the value, which I could do by getting all of the
                            //keys, then converting it to an array, then retrieving the key at the proper position.
                            //Oh, and the array is backwards as well, so hence why the productsByPopularity.size, both should be the same size, I just knew
                            //that one had a size, and when I click on it, it instantly works, so no, this isn't the best possible way to do it, but it does work,
                            //(oh and there's a -1 to fix the off by one error as well)
                            String itemName = sortedByPopularity.get(sortedByPopularity.keySet().toArray()[productsByPopularity.size()-position-1]);
                            String possibleCorrection = new FixBadNames().fix(itemName);
                            if (possibleCorrection != null) {
                                itemName = possibleCorrection;
                            }
                            viewDetails.putExtra("itemToExpand",itemName);
                            startActivity(viewDetails);
                        }
                    });
                }
                //endregion
            //endregion

        //endregion

        //region Search for an item
        final JSONObject finalPriceData = priceData;
        new Thread(){
            @Override
            public void run() {
                super.run();
                String previousSearch = search.getText().toString();
                while(true){
                    String currentSearch = search.getText().toString();
                    //Only perform work when the user has entered new text
                    if (currentSearch.equals(previousSearch)){
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        previousSearch = currentSearch;
                        //So there is consistency
                        currentSearch = currentSearch.toLowerCase();
                        //Store all of the things to search for
                        ArrayList<String> searchTerms = new ArrayList<>();
                        String currentSearchTerm = "";
                        //It makes sense that users will put spaces in between new terms, and the
                        //actual data does not use spaces, so I'll just run .contains on each term, separated
                        //by spaces
                        for (int i = 0; i < currentSearch.length(); ++i){
                            if (currentSearch.substring(i,i+1).equals(" ")){
                                searchTerms.add(currentSearchTerm);
                                currentSearchTerm = "";
                            } else {
                                currentSearchTerm += (currentSearch.charAt(i));
                            }
                        }
                        searchTerms.add(currentSearchTerm);

                        //Get list of all possible items
                        final ArrayList<String> products = new ArrayList<>();
                        JSONObject productsList = null;
                        try {
                            assert finalPriceData != null;
                            productsList = finalPriceData.getJSONObject("products");
                            Iterator<String> productIterator = productsList.keys();
                            while (productIterator.hasNext()){
                                String current_product = productIterator.next();
                                String possible_correction = new FixBadNames().fix(current_product);
                                if (possible_correction != null) {
                                    current_product = possible_correction;
                                }
                                products.add(current_product);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //Get only the list of items the user wants
                        final ArrayList<String> productsWanted = new ArrayList<>();
                        for(String possibleProduct:products){
                            //Check all the search terms, if a product contains the term, add it to the list
                            for(String term : searchTerms) {
                                if(possibleProduct.toLowerCase().contains(term)){
                                    String possibleCorrection = new FixBadNames().unfix(possibleProduct);
                                    if(possibleCorrection != null) {
                                        possibleProduct = possibleCorrection;
                                    }
                                    productsWanted.add(possibleProduct);
                                    break;
                                }
                            }
                        }
                        //Use the same stupid method from earlier to actually display the products
                        final ArrayList<String> productsWantedInformation = new ArrayList<>();
                        //Acquire the number of buy and sell orders
                        for (String product : productsWanted) {
                            try {
                                int buyOrders = productsList.getJSONObject(product).getJSONObject("quick_status").getInt("buyOrders");
                                int sellOrders = productsList.getJSONObject(product).getJSONObject("quick_status").getInt("sellOrders");

                                double buyPrice;
                                if (buyOrders != 0) {
                                    buyPrice = productsList.getJSONObject(product).getJSONArray("buy_summary").getJSONObject(0).getDouble("pricePerUnit");
                                } else {
                                    buyPrice = 0;
                                }

                                double sellPrice;
                                if (buyOrders != 0) {
                                    sellPrice = productsList.getJSONObject(product).getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit");
                                } else {
                                    sellPrice = 0;
                                }

                                buyPrice = Round2(buyPrice);
                                sellPrice = Round2(sellPrice);
                                String buyMessage = Double.toString(buyPrice);
                                String sellMessage = Double.toString(sellPrice);

                                //Add commas to numbers for maximum readability
                                buyMessage = addCommasAdjusted(buyMessage);
                                sellMessage = addCommasAdjusted(sellMessage);
                                String buyOrdersCommas = addCommas(Integer.toString(buyOrders));
                                String sellOrdersCommas = addCommas(Integer.toString(sellOrders));

                                //Sad message for very unpopular items
                                if (buyPrice == 0.0) {
                                    buyMessage = "Nobody is selling this item ¯\\_(ツ)_/¯";
                                }
                                if (sellPrice == 0.0) {
                                    sellMessage = "Nobody is buying this item ¯\\_(ツ)_/¯";
                                }

                                String possible_correction = new FixBadNames().fix(product);
                                if (possible_correction != null) {
                                    product = possible_correction;
                                }

                                String listEntry = product + "\n    Buy for: " + buyMessage + "\n    Sell for: " + sellMessage +
                                        "\n    Buy Orders: " + buyOrdersCommas + "\n    Sell Orders: " + sellOrdersCommas;
                                productsWantedInformation.add(listEntry);
                            } catch (Exception e){
                                //Nothing to do here
                            }
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Put it all in the listview
                                ArrayAdapter<String> productAdapter = new ArrayAdapter<>(
                                        ViewPricesNoScrollActivity.this,
                                        android.R.layout.simple_list_item_1,
                                        productsWantedInformation
                                );

                                productsListView.setAdapter(productAdapter);
                                //View details of each item if desired
                                productsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        final Intent viewDetails = new Intent(getApplicationContext(), ExtraDetailViewPricesActivity.class);
                                        String itemName = productsWanted.get(position);
                                        String possibleCorrection = new FixBadNames().fix(itemName);
                                        if (possibleCorrection != null) {
                                            itemName = possibleCorrection;
                                        }
                                        viewDetails.putExtra("itemToExpand",itemName);
                                        startActivity(viewDetails);
                                    }
                                });
                            }
                        });
                    }
                }
            }
        }.start();
        //endregion

        //region Displays how long its been since the data was last updated
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
    }

    //Quick method for removing pesky floating point imprecision decimals
    public double Round2(double input) {
        BigDecimal bd = BigDecimal.valueOf(input);
        bd = bd.setScale(1, RoundingMode.HALF_UP);
        return bd.doubleValue();
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
                txtTime.setText(setTextTo);
            }
            if (minutesPassed > 1 && minutesPassed <= 5){
                String setTextTo = minutesPassed + " MINUTES AGO";
                txtTime.setText(setTextTo);
            }
            if (minutesPassed > 5 && minutesPassed <= 60) {
                String setTextTo = minutesPassed + " MINUTES AGO";
                txtTime.setText(setTextTo);
                //Set to Orange
                txtTime.setTextColor(Color.parseColor("#ff8519"));
            }
            if (minutesPassed > 60) {
                String setTextTo = minutesPassed + " MINUTES AGO";
                //Set to Red
                txtTime.setTextColor(Color.parseColor("#ed1818"));
                txtTime.setText(setTextTo);
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

    //Add commas method for integers
    public String addCommas(String digits) {
        String result = "";
        for (int i=1; i <= digits.length(); ++i) {
            char ch = digits.charAt(digits.length() - i);
            if (i % 3 == 1 && i > 1) {
                result = "," + result;
            }
            result = ch + result;
        }

        return result;
    }
}