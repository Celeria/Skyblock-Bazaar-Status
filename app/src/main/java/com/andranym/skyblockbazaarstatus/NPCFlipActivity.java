package com.andranym.skyblockbazaarstatus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Pair;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

public class NPCFlipActivity extends AppCompatActivity {

    TextView txtTimeNPC;
    ListView NPCProfitList;
    TextView txtNPCProfitAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_n_p_c_flip);

        //region Load data for this activity
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final int discount = settings.getInt("shadyDiscount",0);
        final String priceDataString = settings.getString("currentData",null);
        double bazaarTax = 1-((double)settings.getInt("personalBazaarTaxAmount",125)/1000/100);
        //endregion

        //regionSet proper title
        switch (discount) {
            case 1:
                setTitle("NPC Flip With Shady Ring");
                break;
            case 2:
                setTitle("NPC Flip With Shady Artifact");
                break;
            case 3:
                setTitle("NPC Flip With Seal of the Family");
                break;
            default:
                setTitle("NPC Flip With No Discount");
        }
        //endregion

        //regionDeclare UI elements
        txtTimeNPC = findViewById(R.id.txtTimeNPC);
        NPCProfitList = findViewById((R.id.NPCProfitList));
        txtNPCProfitAmount = findViewById(R.id.txtNPCProfitAmount);
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

        //regionMap of NPC prices;
        //Create a map of the products and their popularity
        Map<String,Double> npcPrices = new HashMap<>();
        npcPrices.put("PACKED_ICE",9.0);
        npcPrices.put("PACKED_ICE (JERRY'S WORKSHOP)",9.0);
        npcPrices.put("ICE_BAIT (JERRY'S WORKSHOP)",12.0);
        npcPrices.put("RABBIT_FOOT",10.0);
        npcPrices.put("RED_MUSHROOM",12.0);
        npcPrices.put("SAND",4.0);
        npcPrices.put("FLINT",6.0);
        npcPrices.put("COAL",4.0);
        npcPrices.put("GOLD_INGOT",6.0);
        npcPrices.put("GOLD_INGOT (GOLD MINE)",5.5);
        npcPrices.put("GUNPOWDER",10.0);
        npcPrices.put("ICE",1.0);
        npcPrices.put("ICE (JERRY'S WORKSHOP)",1.0);
        npcPrices.put("BROWN_MUSHROOM",12.0);
        npcPrices.put("RAW_FISH",20.0);
        npcPrices.put("COBBLESTONE",3.0);
        npcPrices.put("WHEAT", 149.0/64);
        npcPrices.put("IRON_INGOT",5.5);
        npcPrices.put("IRON_INGOT (GOLD MINE)",5.0);
        npcPrices.put("CARROT",149.0/64);
        npcPrices.put("POTATO",149.0/64);
        npcPrices.put("JUNGLE_WOOD",5.0);
        npcPrices.put("BIRCH_WOOD",5.0);
        npcPrices.put("OAK_WOOD",5.0);
        npcPrices.put("COCOA_BEANS",5.0);
        npcPrices.put("BONE",8.0);
        npcPrices.put("SPRUCE_WOOD",5.0);
        npcPrices.put("MELON",2.0);
        npcPrices.put("ACACIA_WOOD",5.0);
        npcPrices.put("SUGAR_CANE",5.0);
        npcPrices.put("DARK_OAK_WOOD",5.0);
        npcPrices.put("ROTTEN_FLESH",8.0);
        npcPrices.put("GRAVEL",6.0);
        npcPrices.put("PUMPKIN",8.0);
        npcPrices.put("ENDSTONE",10.0);
        npcPrices.put("SPIDER_EYE",12.00);
        npcPrices.put("SLIME_BALL",14.0);
        npcPrices.put("NETHER_WART",10.0);
        npcPrices.put("MAGMA_CREAM",20.0);
        npcPrices.put("SALMON",30.0);
        npcPrices.put("PUFFERFISH",40.0);
        npcPrices.put("OBSIDIAN",50.0);
        npcPrices.put("CLOWNFISH",100.0);
        npcPrices.put("GHAST_TEAR",200.0);
        npcPrices.put("QUARTZ",32.0);
        //endregion

        //region Get JSON array of price data.
        JSONObject priceData = null;
        try {
            assert priceDataString != null;
            priceData = new JSONObject(priceDataString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //endregion

        //regionList of profits of the unenchanted versions
        ArrayList<Double> npcProfitNormal = new ArrayList<>();
        for (String product: npcPrices.keySet()){
            //Adjust any names so that it matches the data entry in the priceData
            String productKey;
            switch (product) {
                case "PACKED_ICE (JERRY'S WORKSHOP)":
                    productKey = "PACKED_ICE";
                    break;
                case "ICE_BAIT (JERRY'S WORKSHOP)":
                    productKey = "ICE_BAIT";
                    break;
                case "GOLD_INGOT (GOLD MINE)":
                    productKey = "GOLD_INGOT";
                    break;
                case "ICE (JERRY'S WORKSHOP)":
                    productKey = "ICE";
                    break;
                case "GUNPOWDER":
                    productKey = "SULPHUR";
                    break;
                case "IRON_INGOT (GOLD MINE)":
                    productKey = "IRON_INGOT";
                    break;
                case "POTATO":
                    productKey = "POTATO_ITEM";
                    break;
                case "CARROT":
                    productKey = "CARROT_ITEM";
                    break;
                case "JUNGLE_WOOD":
                case "OAK_WOOD":
                case "BIRCH_WOOD":
                case "DARK_OAK_WOOD":
                case "ACACIA_WOOD":
                case "SPRUCE_WOOD":
                case "SALMON":
                case "PUFFERFISH":
                case "CLOWNFISH":
                case "COCOA_BEANS":
                case "ENDSTONE":
                case "NETHER_WART":
                    productKey = new FixBadNames().unfix(product);
                    break;
                default:
                    productKey = product;
            }

            //Get the current pricing data using productKey
            double sellPrice = 0;
            try {
                assert priceData != null;
                sellPrice = priceData.getJSONObject("products").getJSONObject(productKey).getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //Find the profit by subtracting the npc price (accounting for user discount) from the sell price at bazaar
            double currentProfit;
            if (product.equals("ENCHANTED_QUARTZ")) {
                //Account for the fact that regular QUARTZ you can't buy from the market, so you can't sell it normally
                currentProfit = 0;
            } else {
                currentProfit = (sellPrice - npcPrices.get(product) * (1 - (double) (discount / 100)))*bazaarTax;
            }
            npcProfitNormal.add(currentProfit);
        }
        //endregion

        //regionList of profits of the enchanted versions
        ArrayList<Double> npcProfitEnchanted = new ArrayList<>();
        for(String product: npcPrices.keySet()) {
            String productKey;
            switch (product) {
                case "PACKED_ICE (JERRY'S WORKSHOP)":
                case "ICE (JERRY'S WORKSHOP)":
                case "ICE":
                case "PACKED_ICE":
                    productKey = "ENCHANTED_ICE";
                    break;
                case "ICE_BAIT (JERRY'S WORKSHOP)":
                    productKey = "ICE_BAIT";
                    break;
                case "RABBIT_FOOT":
                    productKey = "ENCHANTED_RABBIT_FOOT";
                    break;
                case "RED_MUSHROOM":
                    productKey = "ENCHANTED_RED_MUSHROOM";
                    break;
                case "BROWN_MUSHROOM":
                    productKey = "ENCHANTED_BROWN_MUSHROOM";
                    break;
                case "SAND":
                    productKey = "ENCHANTED_SAND";
                    break;
                case "FLINT":
                    productKey = "ENCHANTED_FLINT";
                    break;
                case "COAL":
                    productKey = "ENCHANTED_COAL";
                    break;
                case "GOLD_INGOT (GOLD MINE)":
                case "GOLD_INGOT":
                    productKey = "ENCHANTED_GOLD";
                    break;
                case "GUNPOWDER":
                    productKey = "ENCHANTED_GUNPOWDER";
                    break;
                case "RAW_FISH":
                    productKey = "ENCHANTED_RAW_FISH";
                    break;
                case "COBBLESTONE":
                    productKey = "ENCHANTED_COBBLESTONE";
                    break;
                case "IRON_INGOT":
                case "IRON_INGOT (GOLD MINE)":
                    productKey = "ENCHANTED_IRON";
                    break;
                case "WHEAT":
                    productKey = "HAY_BLOCK";
                    break;
                case "POTATO":
                    productKey = "ENCHANTED_POTATO";
                    break;
                case "CARROT":
                    productKey = "ENCHANTED_CARROT";
                    break;
                case "JUNGLE_WOOD":
                    productKey = "ENCHANTED_JUNGLE_LOG";
                    break;
                case "OAK_WOOD":
                    productKey = "ENCHANTED_OAK_LOG";
                    break;
                case "BIRCH_WOOD":
                    productKey = "ENCHANTED_BIRCH_LOG";
                    break;
                case "DARK_OAK_WOOD":
                    productKey = "ENCHANTED_DARK_OAK_LOG";
                    break;
                case "ACACIA_WOOD":
                    productKey = "ENCHANTED_ACACIA_LOG";
                    break;
                case "SPRUCE_WOOD":
                    productKey = "ENCHANTED_SPRUCE_LOG";
                    break;
                case "SALMON":
                    productKey = "ENCHANTED_RAW_SALMON";
                    break;
                case "PUFFERFISH":
                    productKey = "ENCHANTED_PUFFERFISH";
                    break;
                case "CLOWNFISH":
                    productKey = "ENCHANTED_CLOWNFISH";
                    break;
                case "COCOA_BEANS":
                    productKey = "ENCHANTED_COCOA";
                    break;
                case "ENDSTONE":
                    productKey = "ENCHANTED_ENDSTONE";
                    break;
                case "NETHER_WART":
                    productKey = "ENCHANTED_NETHER_STALK";
                    break;
                case "BONE":
                    productKey = "ENCHANTED_BONE";
                    break;
                case "MELON":
                    productKey = "ENCHANTED_MELON";
                    break;
                case "SUGAR_CANE":
                    productKey = "ENCHANTED_SUGAR";
                    break;
                case "ROTTEN_FLESH":
                    productKey = "ENCHANTED_ROTTEN_FLESH";
                    break;
                case "GRAVEL":
                    productKey = "GRAVEL";
                    break;
                case "PUMPKIN":
                    productKey = "ENCHANTED_PUMPKIN";
                    break;
                case "SPIDER_EYE":
                    productKey = "ENCHANTED_SPIDER_EYE";
                    break;
                case "STRING":
                    productKey = "ENCHANTED_STRING";
                    break;
                case "SLIME_BALL":
                    productKey = "ENCHANTED_SLIME_BALL";
                    break;
                case "MAGMA_CREAM":
                    productKey = "ENCHANTED_MAGMA_CREAM";
                    break;
                case "OBSIDIAN":
                    productKey = "ENCHANTED_OBSIDIAN";
                    break;
                case "GHAST_TEAR":
                    productKey = "ENCHANTED_GHAST_TEAR";
                    break;
                default:
                    //shouldn't happen
                    productKey = product;
            }

            //How many regular items form an enchanted version
            double multiply_by;
            switch (product) {
                case "PACKED_ICE":
                    multiply_by = (160/9);
                    break;
                case "PACKED_ICE (JERRY'S WORKSHOP)":
                    multiply_by = (160/9);
                    break;
                case "QUARTZ":
                    multiply_by = 40.0;
                    break;
                case "WHEAT":
                    multiply_by = 9;
                    break;
                case "STRING":
                    multiply_by = 192;
                    break;
                case "GHAST_TEAR":
                    multiply_by = 5;
                    break;
                default:
                    multiply_by = 160;
            }

            //get the proper sellPrice
            double sellPrice = 0;
            try {
                assert priceData != null;
                sellPrice = priceData.getJSONObject("products").getJSONObject(productKey).getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            double enchantedProfit;
            if (product.equals("GRAVEL")) {
                enchantedProfit = 0;
            } else{
                enchantedProfit = sellPrice - npcPrices.get(product) * (1 - (double) (discount / 100)) * multiply_by;
            }

            npcProfitEnchanted.add(enchantedProfit);
        }
        //endregion

        //regionStore a map of the names plus the total profits
        Map<Double,String> productsByProfit= new HashMap<>();
        int iterator = 0;
        Set NPCProductNamesSet = npcPrices.keySet();
        Iterator<String> NPCProductNames = NPCProductNamesSet.iterator();
        //Create a bit of randomness so the values aren't the same (so keys work)
        Random rand = new Random();
        for (double normalProfit: npcProfitNormal) {
            double totalProfit = normalProfit * 640 + npcProfitEnchanted.get(iterator) * 4;
            double fuzziness = rand.nextDouble() * 0.001;
            //Randomness here
            totalProfit = totalProfit + fuzziness;
            ++iterator;
            productsByProfit.put(totalProfit,NPCProductNames.next());
        }
        //endregion

        //regionUse TreeMap to sort by the column of integers
        Map<Double,String> sortedByNPCProfit = new TreeMap<>();
        sortedByNPCProfit.putAll(productsByProfit);
        //endregion

        //regionDisplay the profits in a listview
        //Variable used to keep track of total profits for the day
        double totalProfitToday = 0;
        //Array used for the listview later on
        ArrayList<String> printThese = new ArrayList<>();
        //Loop through each one and add them to the things to be printed
        for(double profit: sortedByNPCProfit.keySet()){
            String currentProduct = sortedByNPCProfit.get(profit);
            //regionSome products have weird edge cases, specify message for those products
            String specialNote = "";
            if (currentProduct.equals("QUARTZ")) {
                specialNote = "\n  *The shop only sells quartz blocks";
            }
            if (currentProduct.equals("WHEAT")) {
                specialNote = "\n  *You can only make hay bales out of wheat\n" +
                        "   There will be a bit of leftover";
            }
            if (currentProduct.equals("GRAVEL")){
                specialNote = "\n  *There is no enchanted gravel";
            }
            if (currentProduct.equals("ICE_BAIT (JERRY'S WORKSHOP)")){
                specialNote = "\n  *There is no enchanted ice bait";
            }
            if (currentProduct.equals("STRING")){
                specialNote = "\n  *Enchanted string requires 192 string to craft.\n" +
                        "   There will be a bit of leftover";
            }
            //endregion

            //regionCalculate profits again for unenchanted items
            String productKeyNormal;
            switch (currentProduct) {
                case "PACKED_ICE (JERRY'S WORKSHOP)":
                    productKeyNormal = "PACKED_ICE";
                    break;
                case "ICE_BAIT (JERRY'S WORKSHOP)":
                    productKeyNormal = "ICE_BAIT";
                    break;
                case "GOLD_INGOT (GOLD MINE)":
                    productKeyNormal = "GOLD_INGOT";
                    break;
                case "ICE (JERRY'S WORKSHOP)":
                    productKeyNormal = "ICE";
                    break;
                case "GUNPOWDER":
                    productKeyNormal = "SULPHUR";
                    break;
                case "IRON_INGOT (GOLD MINE)":
                    productKeyNormal = "IRON_INGOT";
                    break;
                case "POTATO":
                    productKeyNormal = "POTATO_ITEM";
                    break;
                case "CARROT":
                    productKeyNormal = "CARROT_ITEM";
                    break;
                case "JUNGLE_WOOD":
                case "OAK_WOOD":
                case "BIRCH_WOOD":
                case "DARK_OAK_WOOD":
                case "ACACIA_WOOD":
                case "SPRUCE_WOOD":
                case "SALMON":
                case "PUFFERFISH":
                case "CLOWNFISH":
                case "COCOA_BEANS":
                case "ENDSTONE":
                case "NETHER_WART":
                    productKeyNormal = new FixBadNames().unfix(currentProduct);
                    break;
                default:
                    productKeyNormal = currentProduct;
            }
            double normalProfit = 0;
            double normalSellPrice;
            if (currentProduct.equals("QUARTZ")){
                //Quartz does not have a normal version in shops
                normalProfit = 0;
            } else {
                try {
                    //Multiply by 640 because that's how many you can buy
                    normalSellPrice = 640 * priceData.getJSONObject("products").getJSONObject(productKeyNormal).getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit");
                    normalProfit = normalSellPrice - 640 * (npcPrices.get(currentProduct) * (1 - ((double) discount / 100)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //endregion

            //region Calculate profits again for enchanted items
            String productKeyEnchanted;
            switch (currentProduct) {
                case "PACKED_ICE":
                case "PACKED_ICE (JERRY'S WORKSHOP)":
                case "ICE (JERRY'S WORKSHOP)":
                case "ICE":
                    productKeyEnchanted = "ENCHANTED_ICE";
                    break;
                case "ICE_BAIT (JERRY'S WORKSHOP)":
                    productKeyEnchanted = "ICE_BAIT";
                    break;
                case "RABBIT_FOOT":
                    productKeyEnchanted = "ENCHANTED_RABBIT_FOOT";
                    break;
                case "RED_MUSHROOM":
                    productKeyEnchanted = "ENCHANTED_RED_MUSHROOM";
                    break;
                case "BROWN_MUSHROOM":
                    productKeyEnchanted = "ENCHANTED_BROWN_MUSHROOM";
                    break;
                case "SAND":
                    productKeyEnchanted = "ENCHANTED_SAND";
                    break;
                case "FLINT":
                    productKeyEnchanted = "ENCHANTED_FLINT";
                    break;
                case "COAL":
                    productKeyEnchanted = "ENCHANTED_COAL";
                    break;
                case "GOLD_INGOT (GOLD MINE)":
                case "GOLD_INGOT":
                    productKeyEnchanted = "ENCHANTED_GOLD";
                    break;
                case "GUNPOWDER":
                    productKeyEnchanted = "ENCHANTED_GUNPOWDER";
                    break;
                case "RAW_FISH":
                    productKeyEnchanted = "ENCHANTED_RAW_FISH";
                    break;
                case "COBBLESTONE":
                    productKeyEnchanted = "ENCHANTED_COBBLESTONE";
                    break;
                case "IRON_INGOT":
                case "IRON_INGOT (GOLD MINE)":
                    productKeyEnchanted = "ENCHANTED_IRON";
                    break;
                case "WHEAT":
                    productKeyEnchanted = "HAY_BLOCK";
                    break;
                case "POTATO":
                    productKeyEnchanted = "ENCHANTED_POTATO";
                    break;
                case "CARROT":
                    productKeyEnchanted = "ENCHANTED_CARROT";
                    break;
                case "JUNGLE_WOOD":
                    productKeyEnchanted = "ENCHANTED_JUNGLE_LOG";
                    break;
                case "OAK_WOOD":
                    productKeyEnchanted = "ENCHANTED_OAK_LOG";
                    break;
                case "BIRCH_WOOD":
                    productKeyEnchanted = "ENCHANTED_BIRCH_LOG";
                    break;
                case "DARK_OAK_WOOD":
                    productKeyEnchanted = "ENCHANTED_DARK_OAK_LOG";
                    break;
                case "ACACIA_WOOD":
                    productKeyEnchanted = "ENCHANTED_ACACIA_LOG";
                    break;
                case "SPRUCE_WOOD":
                    productKeyEnchanted = "ENCHANTED_SPRUCE_LOG";
                    break;
                case "SALMON":
                    productKeyEnchanted = "ENCHANTED_RAW_SALMON";
                    break;
                case "PUFFERFISH":
                    productKeyEnchanted = "ENCHANTED_PUFFERFISH";
                    break;
                case "CLOWNFISH":
                    productKeyEnchanted = "ENCHANTED_CLOWNFISH";
                    break;
                case "COCOA_BEANS":
                    productKeyEnchanted = "ENCHANTED_COCOA";
                    break;
                case "ENDSTONE":
                    productKeyEnchanted = "ENCHANTED_ENDSTONE";
                    break;
                case "NETHER_WART":
                    productKeyEnchanted = "ENCHANTED_NETHER_STALK";
                    break;
                case "BONE":
                    productKeyEnchanted = "ENCHANTED_BONE";
                    break;
                case "MELON":
                    productKeyEnchanted = "ENCHANTED_MELON";
                    break;
                case "SUGAR_CANE":
                    productKeyEnchanted = "ENCHANTED_SUGAR";
                    break;
                case "ROTTEN_FLESH":
                    productKeyEnchanted = "ENCHANTED_ROTTEN_FLESH";
                    break;
                case "GRAVEL":
                    productKeyEnchanted = "GRAVEL";
                    break;
                case "PUMPKIN":
                    productKeyEnchanted = "ENCHANTED_PUMPKIN";
                    break;
                case "SPIDER_EYE":
                    productKeyEnchanted = "ENCHANTED_SPIDER_EYE";
                    break;
                case "STRING":
                    productKeyEnchanted = "ENCHANTED_STRING";
                    break;
                case "SLIME_BALL":
                    productKeyEnchanted = "ENCHANTED_SLIME_BALL";
                    break;
                case "MAGMA_CREAM":
                    productKeyEnchanted = "ENCHANTED_MAGMA_CREAM";
                    break;
                case "OBSIDIAN":
                    productKeyEnchanted = "ENCHANTED_OBSIDIAN";
                    break;
                case "GHAST_TEAR":
                    productKeyEnchanted = "ENCHANTED_GHAST_TEAR";
                    break;
                case "QUARTZ":
                    productKeyEnchanted = "ENCHANTED_QUARTZ";
                    break;
                default:
                    //shouldn't happen
                    productKeyEnchanted = currentProduct;
            }
            double enchantedProfit = 0;
            double enchantedSellPrice;
            if (currentProduct.equals("ICE_BAIT (JERRY'S WORKSHOP)")) {
                enchantedProfit = 0;
            } else
            if (currentProduct.equals("WHEAT")){
                //Wheat only has hay bales, so a different situation slightly (9 per bale instead of the normal 160)
                try {
                    enchantedSellPrice = (640/9) * priceData.getJSONObject("products").getJSONObject(productKeyEnchanted).getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit");
                    enchantedProfit = (enchantedSellPrice - 640 * (npcPrices.get(currentProduct) * (1 - ((double) discount / 100))))*bazaarTax;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else
            if (currentProduct.equals("GRAVEL")) {
                enchantedProfit = 0;
            } else
            if (currentProduct.equals("STRING")) {
                try {
                    //You can technically only buy 3 enchanted string plus a third.
                    enchantedSellPrice = (10/3) * priceData.getJSONObject("products").getJSONObject(productKeyEnchanted).getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit");
                    enchantedProfit = (enchantedSellPrice - (640) * (npcPrices.get(currentProduct) * (1 - ((double) discount / 100))))*bazaarTax;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else
            if (currentProduct.equals("QUARTZ")){
                //You can buy 16 enchanted quartz since its in block form
                try {
                    enchantedSellPrice = 16 * priceData.getJSONObject("products").getJSONObject(productKeyEnchanted).getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit");
                    enchantedProfit = (enchantedSellPrice - (640) * (npcPrices.get(currentProduct) * (1 - ((double) discount / 100))))*bazaarTax;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else
            if (currentProduct.equals("GHAST_TEAR")) {
                //You can buy up to 128 ghast tears
                //Dunno why I included this, this will almost never be profitable
                try {
                    enchantedSellPrice = 128 * priceData.getJSONObject("products").getJSONObject(productKeyEnchanted).getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit");
                    enchantedProfit = (enchantedSellPrice - (640) * (npcPrices.get(currentProduct) * (1 - ((double) discount / 100))))*bazaarTax;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else
            if (currentProduct.equals("PACKED_ICE (JERRY'S WORKSHOP)") || currentProduct.equals("PACKED_ICE")) {
                //640 packed ice can be crafted into 36 enchanted ice
                try {
                    enchantedSellPrice = 36 * priceData.getJSONObject("products").getJSONObject(productKeyEnchanted).getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit");
                    enchantedProfit = (enchantedSellPrice - (640) * (npcPrices.get(currentProduct) * (1 - ((double) discount / 100))))*bazaarTax;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else {
                //For all the normal products where you can buy 640 items, and each enchanted item requires 160 items to craft, nice and easy.
                //Sometimes they just have to change it up to annoy us.
                try {
                    enchantedSellPrice = 4 * priceData.getJSONObject("products").getJSONObject(productKeyEnchanted).getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit");
                    enchantedProfit = (enchantedSellPrice - (640) * (npcPrices.get(currentProduct) * (1 - ((double) discount / 100))))*bazaarTax;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //endregion

            //regionPut the positive elements in the array that gets printed out
            //Put a plus sign by the one that is higher
            String enchantedMore = "";
            String normalMore = "";
            if (normalProfit > enchantedProfit) {
                normalMore = "  +";
            } else {
                enchantedMore = "  +";
            }
            //Message to be printed
            String listEntry = currentProduct +
                    "\n  Profit for unenchanted: " + addCommasAdjusted(Double.toString(Round2(normalProfit))) + normalMore +
                    "\n  Profit for enchanted: " + addCommasAdjusted(Double.toString(Round2(enchantedProfit))) + enchantedMore + specialNote;

            if (enchantedProfit > 1 || normalProfit > 1){
                if (normalProfit > enchantedProfit) {
                    totalProfitToday += normalProfit;
                } else {
                    totalProfitToday += enchantedProfit;
                }
                printThese.add(listEntry);
            }
            //endregion
        }

        //Reverse so most profitable is on top
        Collections.reverse(printThese);

        //region Display using adapter
        ArrayAdapter<String> profitAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                printThese
        );
        NPCProfitList.setAdapter(profitAdapter);
        //endregion
        //endregion

        //region Display total profits today
        String profitAmount = addCommasAdjusted(Double.toString(Round2(totalProfitToday)));
        txtNPCProfitAmount.setText(profitAmount);
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
                txtTimeNPC.setText(setTextTo);
            }
            if (minutesPassed > 1 && minutesPassed <= 5){
                String setTextTo = minutesPassed + " MINUTES AGO";
                txtTimeNPC.setText(setTextTo);
            }
            if (minutesPassed > 5 && minutesPassed <= 60) {
                String setTextTo = minutesPassed + " MINUTES AGO";
                txtTimeNPC.setText(setTextTo);
                //Set to Orange
                txtTimeNPC.setTextColor(Color.parseColor("#ff8519"));
            }
            if (minutesPassed > 60) {
                String setTextTo = minutesPassed + " MINUTES AGO";
                //Set to Red
                txtTimeNPC.setTextColor(Color.parseColor("#ed1818"));
                txtTimeNPC.setText(setTextTo);
            }
        }
    }

    //Quick method for removing pesky floating point imprecision decimals
    public double Round2(double input) {
        BigDecimal bd = BigDecimal.valueOf(input);
        bd = bd.setScale(1, RoundingMode.HALF_UP);
        return bd.doubleValue();
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
}