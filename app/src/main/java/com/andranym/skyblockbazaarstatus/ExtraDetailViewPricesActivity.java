package com.andranym.skyblockbazaarstatus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class ExtraDetailViewPricesActivity extends AppCompatActivity {

    TextView txtBuyInfo;
    TextView txtSellInfo;
    ListView listBuyInfo;
    ListView listSellInfo;
    TextView txtTimeDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra_detail);

        //Get information about what we are displaying
        String itemToView = getIntent().getStringExtra("itemToExpand");
        setTitle(itemToView);

        //region Declare UI elements
        txtBuyInfo = findViewById(R.id.txtBuyInfo);
        txtSellInfo = findViewById(R.id.txtSellInfo);
        listBuyInfo = findViewById(R.id.listBuyInfo);
        listSellInfo = findViewById(R.id.listSellInfo);
        txtTimeDetail = findViewById(R.id.txtTimeDetail);
        //endregion

        //region Load data for this activity
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final String priceDataString = settings.getString("currentData",null);
        //endregion

        //region Get JSON array of all the products
        JSONObject priceData = null;
        try {
            priceData = new JSONObject(priceDataString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONObject productsNoOrder = null;
        try {
            assert priceData != null;
            productsNoOrder = priceData.getJSONObject("products");
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

        //region Get all data for our product
        String possibleCorrection = new FixBadNames().unfix(itemToView);
        if (possibleCorrection != null) {
            itemToView = possibleCorrection;
        }

        long sellMovingWeek = 0;
        long buyMovingWeek = 0;
        int buyOrders = 0;
        int sellOrders = 0;
        long currentDemand = 0;
        long currentSupply = 0;
        try {
            buyMovingWeek = productsNoOrder.getJSONObject(itemToView).getJSONObject("quick_status").getLong("buyMovingWeek");
            sellMovingWeek = productsNoOrder.getJSONObject(itemToView).getJSONObject("quick_status").getLong("sellMovingWeek");
            buyOrders = productsNoOrder.getJSONObject(itemToView).getJSONObject("quick_status").getInt("buyOrders");
            sellOrders = productsNoOrder.getJSONObject(itemToView).getJSONObject("quick_status").getInt("sellOrders");
            currentDemand = productsNoOrder.getJSONObject(itemToView).getJSONObject("quick_status").getLong("buyVolume");
            currentSupply = productsNoOrder.getJSONObject(itemToView).getJSONObject("quick_status").getLong("sellVolume");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        boolean buyDone = false;
        ArrayList<Integer> buyInfoAmount = new ArrayList<>();
        ArrayList<Double> buyInfoPrice = new ArrayList<>();
        ArrayList<Integer> buyInfoOrders = new ArrayList<>();
        try {
            for(int i = 0; !buyDone; i++) {
                buyInfoAmount.add(productsNoOrder.getJSONObject(itemToView).getJSONArray("buy_summary").getJSONObject(i).getInt("amount"));
                buyInfoPrice.add(productsNoOrder.getJSONObject(itemToView).getJSONArray("buy_summary").getJSONObject(i).getDouble("pricePerUnit"));
                buyInfoOrders.add(productsNoOrder.getJSONObject(itemToView).getJSONArray("buy_summary").getJSONObject(i).getInt("orders"));
            }
        } catch(JSONException e){
            e.printStackTrace();
            buyDone = true;
        }
        boolean sellDone = false;
        ArrayList<Integer> sellInfoAmount = new ArrayList<>();
        ArrayList<Double> sellInfoPrice = new ArrayList<>();
        ArrayList<Integer> sellInfoOrders = new ArrayList<>();
        try {
            for(int i = 0; !sellDone; i++) {
                sellInfoAmount.add(productsNoOrder.getJSONObject(itemToView).getJSONArray("sell_summary").getJSONObject(i).getInt("amount"));
                sellInfoPrice.add(productsNoOrder.getJSONObject(itemToView).getJSONArray("sell_summary").getJSONObject(i).getDouble("pricePerUnit"));
                sellInfoOrders.add(productsNoOrder.getJSONObject(itemToView).getJSONArray("sell_summary").getJSONObject(i).getInt("orders"));
            }
        } catch(JSONException e){
            e.printStackTrace();
            sellDone = true;
        }
        //endregion

        //regionDisplay Data
        String buyInfoString = addCommas(Long.toString(currentDemand)) + " available in " +
                addCommas(Integer.toString(buyOrders)) + " buy orders.\n" +
                addCommas(Long.toString(buyMovingWeek)) + " instant buys in the last 7 days.";
        txtBuyInfo.setText(buyInfoString);
        String sellInfoString = addCommas(Long.toString(currentSupply)) + " wanted in " +
                addCommas(Integer.toString(sellOrders)) + " sell orders.\n" +
                addCommas(Long.toString(sellMovingWeek)) + " instant sells in the last 7 days.";
        txtSellInfo.setText(sellInfoString);

        ArrayList<String> buyInfoAll = new ArrayList<>();
        for (int i = 0; i < buyInfoAmount.size(); ++i) {
            String info = addCommasAdjusted(Double.toString(Round2(buyInfoPrice.get(i)))) + "  |  " +
                    addCommas(Integer.toString(buyInfoAmount.get(i))) + "x" + " in " +
                    addCommas(Integer.toString(buyInfoOrders.get(i))) + " orders.";
            buyInfoAll.add(info);
        }

        ArrayAdapter<String> buyAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                buyInfoAll
        );

        listBuyInfo.setAdapter(buyAdapter);

        ArrayList<String> sellInfoAll = new ArrayList<>();
        for (int i = 0; i < sellInfoAmount.size(); ++i) {
            String info = addCommasAdjusted(Double.toString(Round2(sellInfoPrice.get(i)))) + "  |  " +
                    addCommas(Integer.toString(sellInfoAmount.get(i))) + "x" + " in " +
                    addCommas(Integer.toString(sellInfoOrders.get(i))) + " orders.";
            sellInfoAll.add(info);
        }

        ArrayAdapter<String> sellAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                sellInfoAll
        );

        listSellInfo.setAdapter(sellAdapter);
        //endregion


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
                txtTimeDetail.setText(setTextTo);
            }
            if (minutesPassed > 1 && minutesPassed <= 5){
                String setTextTo = minutesPassed + " MINUTES AGO";
                txtTimeDetail.setText(setTextTo);
            }
            if (minutesPassed > 5 && minutesPassed <= 60) {
                String setTextTo = minutesPassed + " MINUTES AGO";
                txtTimeDetail.setText(setTextTo);
                //Set to Orange
                txtTimeDetail.setTextColor(Color.parseColor("#ff8519"));
            }
            if (minutesPassed > 60) {
                String setTextTo = minutesPassed + " MINUTES AGO";
                //Set to Red
                txtTimeDetail.setTextColor(Color.parseColor("#ed1818"));
                txtTimeDetail.setText(setTextTo);
            }
        }
    }
}