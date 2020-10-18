package com.andranym.skyblockbazaarstatus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

public class BazaarFlipActivity extends AppCompatActivity {

    TextView txtInvestmentAmount;
    TextView txtInvestmentSass;
    TextView txtNoSafeFlips;
    ListView productProfitList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bazaar_flip);

        setTitle("Most Profitable Bazaar Flips");

        //regionDeclare UI elements
        txtInvestmentAmount = (TextView)findViewById(R.id.txtInvestmentAmount);
        txtInvestmentSass = (TextView)findViewById(R.id.txtInvestmentSass);
        txtNoSafeFlips = (TextView)findViewById(R.id.txtNoSafeFlips);
        productProfitList = (ListView)findViewById(R.id.productProfitList);
        //endregion

        //region Get settings data from sharedPreferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        long investmentAmount = settings.getLong("BazaarInvestmentAmount",100000);
        final String priceDataString = settings.getString("currentData",null);
        int hideBad = settings.getInt("hideBadOrders",0);
        double bazaarTax = 1-((double)(settings.getInt("personalBazaarTaxAmount",1250))/1000/100);
        //endregion

        //region Set up the coin amount in the textview
        //Add commas to number for readability
        String amount = Long.toString(investmentAmount);
        amount = addCommas(amount);

        //Display a message mocking the user if they have too little or too many coins
        String sass = "";
        if(investmentAmount < 10000) {
            sass = "\nSo little... how does it feel to be an ender non? Don't tell me, I don't actually care, your poverty disgusts me.";
        }
        if((investmentAmount >= 10000) & (investmentAmount < 1000000)) {
            sass = "\nGood luck with your flip! Remember, listed profit is just an estimate!";
        }
        if((investmentAmount >= 1000000) & (investmentAmount < 1000000000)) {
            sass = "\nThat is an impressive amount of coins. But that's also a lot of risk, remember the listed profit is an estimation.";
        }
        if(investmentAmount >= 1000000000) {
            sass = "\nYou have that many coins? You do realize that there's more to life than skyblock, right? Ever drive out to the middle " +
                    "of nowhere, on a clear night and watched the Milky Way shimmer into existence as the soft light of twilight fades into " +
                    "an inky blackness so dark you can barely see your hand in front of your face? At the same time, did you look into the eyes " +
                    "of your lover and see the same cosmic infinity reflected in back, and bask in the unrivaled beauty of both? Probably not, " +
                    "because you're wasting your life making a meaningless number go up by far less than the total number of stars you could have " +
                    "seen otherwise.";
        }

        String displayString = "Profits calculated using a buy order of up to " + amount + " coins.";

        txtInvestmentAmount.setText(displayString);
        txtInvestmentSass.setText(sass);


        //endregion

        //region Display the list of profits
        //A lot of code here was copied from the sort by popularity, and some adjustments made
        JSONObject priceData = null;
        try {
            assert priceDataString != null;
            priceData = new JSONObject(priceDataString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayList<String> productsBeforeSort = new ArrayList<>();
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

        ArrayList<BigDecimal> productProfits = new ArrayList<>();
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

                //Acquire the difference between the buy and sell price

                int buyOrders = productsList.getJSONObject(product).getJSONObject("quick_status").getInt("buyOrders");
                int sellOrders = productsList.getJSONObject(product).getJSONObject("quick_status").getInt("buyOrders");

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

                long items_bought = 0;
                if (sellPrice != 0) {
                    items_bought = (long) (investmentAmount / sellPrice);
                }

                //Yeah this line is a bit of a mess.
                //Round2Big takes a BigDecimal and rounds it to 1 place.
                //Because it takes BigDemical, I had to create 2 new BigDecimal values that are the items_bought, and the profit, to get the total profit
                //In the middle, I subtract 0.2, to account for buying for 0.1 more than the previous player, and selling for 0.1 less than the previous player
                //Finally, multiply by the proper amount to account for bazaar tax
                BigDecimal profitMeasure = Round2Big((new BigDecimal(items_bought)).multiply(new BigDecimal(((buyPrice - sellPrice)-0.2)*bazaarTax)));

                productProfits.add(profitMeasure);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Create a map of the products and their profits
        Map<BigDecimal, String> profitSort = new HashMap<>();
        try {
            productsList = priceData.getJSONObject("products");
            Iterator<String> productIterator = productsList.keys();
            int popIndex = 0;
            for (BigDecimal prices: productProfits) {
                profitSort.put(productProfits.get(popIndex),productIterator.next());
                ++popIndex;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Initialize a list of all the products sorted by profit
        final ArrayList<String> productsByProfit = new ArrayList<>();

        //Use TreeMap to sort by the column of integers
        Map<BigDecimal, String> sortedByProfit = new TreeMap<>();
        sortedByProfit.putAll(profitSort);

        //See if we need to display a message that there are no safe flips
        boolean noSafeFlips = true;

        //Populate productsByProfit with the proper entries (code recycled from alphabetical)
        for (BigDecimal key:sortedByProfit.keySet()) {
            String product = sortedByProfit.get(key);
            try {
                productsList = priceData.getJSONObject("products");

                //Acquire the number of buy and sell orders
                int buyOrders = productsList.getJSONObject(product).getJSONObject("quick_status").getInt("buyOrders");
                int sellOrders = productsList.getJSONObject(product).getJSONObject("quick_status").getInt("sellOrders");

                //Get buy and sell prices
                double buyPrice;
                if (buyOrders != 0) {
                    buyPrice = productsList.getJSONObject(product).getJSONArray("buy_summary").getJSONObject(0).getDouble("pricePerUnit");
                }else{
                    buyPrice = 0;
                }

                double sellPrice;
                if (sellOrders != 0) {
                    sellPrice = productsList.getJSONObject(product).getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit");
                }else{
                    sellPrice = 0;
                }

                //Figure out how many items you can buy with your investment
                long items_bought = 0;
                if(buyPrice!=0) {
                    items_bought = (long) (investmentAmount / sellPrice);
                }
                //Acquire profit for this particular item
                //Account for 1% tax as well as the 0.2 more coins you need to spend to secure an order
                String profitPerItem = Double.toString(Round2((buyPrice-sellPrice-0.2)*bazaarTax));
                //BigDecimal userProfitPre = new BigDecimal(profitPerItem);
                BigDecimal items_bought_big = new BigDecimal(items_bought);
                //BigDecimal userProfit = userProfitPre.multiply(items_bought_big);
                BigDecimal userProfit = key;

                //Acquire total amount of product on the market
                long buyVolume = productsList.getJSONObject(product).getJSONObject("quick_status").getLong("buyVolume");
                long sellVolume = productsList.getJSONObject(product).getJSONObject("quick_status").getLong("sellVolume");

                //Get market supply and demand historical data
                long buyMovingWeek = productsList.getJSONObject(product).getJSONObject("quick_status").getLong("buyMovingWeek");
                long sellMovingWeek = productsList.getJSONObject(product).getJSONObject("quick_status").getLong("sellMovingWeek");

                //Warn the user if they crash the market
                boolean hide = false;
                String warning = "";
                if (items_bought > (buyMovingWeek/7/24/2) || items_bought > (sellMovingWeek/7/24/2)) {
                    warning = "\n\n  !!WARNING!! \n" +
                            "  THIS ORDER IS TOO LARGE\n" +
                            "  PRICES ARE LIKELY TO CHANGE BEFORE\n" +
                            "  BUY AND SELL ORDERS ARE FILLED.\n" +
                            "  PROFIT ESTIMATE IS LIKELY INACCURATE\n";
                    hide = true;
                }

                //Before displaying, fix any bad names
                String possible_correction = new FixBadNames().fix(product);
                if (possible_correction != null) {
                    product = possible_correction;
                }

                //Add commas to numbers for maximum readability
                String buyVolumeCommas = addCommas(Long.toString(buyVolume));
                String sellVolumeCommas = addCommas(Long.toString(sellVolume));
                String userProfitCommas = null;
                if (items_bought == 0 && buyPrice != 0) {
                    userProfitCommas = "\n  Estimated profit: " + "\n  You can't even afford one of these items.";
                } else if(items_bought != 0) {
                    userProfitCommas = warning + "\n  Estimated profit: " + addCommasAdjusted(userProfit.toString());
                } else if(items_bought == 0 && buyPrice == 0) {
                    userProfitCommas = "\n  Estimated profit: No price data available";
                }

                String profitPerItemCommas = addCommasAdjusted(profitPerItem);

                String itemsBoughtCommas = addCommas(Long.toString(items_bought));

                String listEntry = product +
                        "\n  Profit for 1 item: " + profitPerItemCommas + userProfitCommas +
                        "\n  Number of items you can buy: " + itemsBoughtCommas +
                        "\n  Current supply in sell offers: " + sellVolumeCommas +
                        "\n  Current demand in buy offers:" + buyVolumeCommas;

                //See if the user wants entries flagged with warnings to be hidden
                if (hideBad == 0) {
                    //User does not want entries hidden
                    productsByProfit.add(listEntry);
                    //Remove no safe flips message
                    noSafeFlips = false;
                } else {
                    //User does want bad entries hidden, so only add if hide is not true
                    //hide from earlier is set to true if the warning is triggered.
                    if (!hide) {
                        productsByProfit.add(listEntry);
                        //remove no safe flips message
                        noSafeFlips = false;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //It is currently sorted from lowest to highest. I want highest to lowest, so order needs to be reversed
        Collections.reverse(productsByProfit);

        ArrayAdapter<String> productAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                productsByProfit
        );
        productProfitList.setAdapter(productAdapter);
        productProfitList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Intent viewDetails = new Intent(getApplicationContext(), ExtraDetailViewPricesActivity.class);
                String fullDescription = productsByProfit.get(position);
                int nameEnds = fullDescription.indexOf("\n");
                String itemName = fullDescription.substring(0,nameEnds);
                viewDetails.putExtra("itemToExpand",itemName);
                startActivity(viewDetails);
            }
        });

        //Check to see if we need to display the no safe flips text
        if(noSafeFlips){
            txtNoSafeFlips.setVisibility(View.VISIBLE);
        }
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

    public BigDecimal Round2Big(BigDecimal bd) {
        return bd.setScale(1, RoundingMode.HALF_UP);
    }
}