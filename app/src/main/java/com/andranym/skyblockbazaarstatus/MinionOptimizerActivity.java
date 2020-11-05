package com.andranym.skyblockbazaarstatus;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;

public class MinionOptimizerActivity extends AppCompatActivity {

    RecyclerView RecMinions;
    MinionRecViewAdapter RecAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minion_optimizer);

        setTitle("Minion Optimizer");

        //region Initialize UI elements
        RecMinions = findViewById(R.id.RecMinions);
        //endregion

        //region Load data for this activity
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final String priceDataString = settings.getString("currentData",null);
        final String defaultUpgrade1 = settings.getString("defaultUpgrade1","Compactor");
        final String defaultUpgrade2 = settings.getString("defaultUpgrade2","Diamond Spreading");
        final int customBoostNormal = settings.getInt("customBoostNormal",0);
        final int customBoostFly = settings.getInt("customBoostFly",0);
        final int catalystFuelNumber = settings.getInt("catalystFuelNumber",3);
        final int normalFuelNumber = settings.getInt("normalFuelNumber",0);
        final int bazaarTax = settings.getInt("personalBazaarTaxAmount",1250);
        double bazaarTaxMultiplier = (100 - ((double)bazaarTax / 1000))/100;
        //Load the Minion Data that I stored in the strings.xml file
        String minionInfoString = getString(R.string.minionJSONData);
        //endregion

        //regionGet the proper JSON objects needed
        JSONObject priceData = null;
        JSONObject minionDataJSON = null;
        try {
            JSONObject priceDataBefore = new JSONObject(priceDataString);
            priceData = priceDataBefore.getJSONObject("products");
            minionDataJSON = new JSONObject(minionInfoString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONArray minionData = null;
        try {
            minionData = minionDataJSON.getJSONArray("minions");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //endregion

        //regionGet usable lists of minion data
        int minionNumber = minionData.length();
        ArrayList<String> minionNames = new ArrayList<>();
        ArrayList<String[]> minionProducts = new ArrayList<>();
        ArrayList<Double[]> minionItemsPerAction = new ArrayList<>();
        ArrayList<Double[]> minionNPCValues = new ArrayList<>();
        ArrayList<Double[]> minionSpeeds = new ArrayList<>();
        //Loop through the minionData JSON array and retrieve all the data we need.
        for(int i = 0;i < minionNumber;++i){
            try {
                //Save lists of all the items, npc values, items per action, and speeds of all the minions
                minionNames.add(minionData.getJSONObject(i).getString("name"));
                JSONArray currentProducts = minionData.getJSONObject(i).getJSONArray("items");
                JSONArray currentNPCValues = minionData.getJSONObject(i).getJSONArray("npc_val");
                JSONArray currentItemsPerAction = minionData.getJSONObject(i).getJSONArray("items_per_action");
                //These are all the same length
                int numberOfItems = currentProducts.length();
                String[] items = new String[numberOfItems];
                Double[] NPCValues = new Double[numberOfItems];
                Double[] itemsPerAction = new Double[numberOfItems];
                for (int j = 0; j < numberOfItems;++j) {
                    items[j] = currentProducts.getString(j);
                    NPCValues[j] = currentNPCValues.getDouble(j);
                    itemsPerAction[j] = currentItemsPerAction.getDouble(j);
                }
                minionProducts.add(items);
                minionItemsPerAction.add(itemsPerAction);
                minionNPCValues.add(NPCValues);
                //Minion Speeds is different so new loop is needed, its always 11, because there's 11 tiers.
                JSONObject currentMinionSpeeds = minionData.getJSONObject(i).getJSONObject("tier");
                int speeds = 11;
                Double[] currentSpeeds = new Double[speeds];
                for (int j = 1; j <= speeds;++j) {
                    String index = Integer.toString(j);
                    currentSpeeds[j-1] = currentMinionSpeeds.getDouble(index);
                }
                minionSpeeds.add(currentSpeeds);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //Make a place to store all this aforementioned minion data
        ArrayList<Minion> minions = new ArrayList<>();
        //endregion

        //regionUse those lists to calculate profits for each minion and store all information about them in the minions array
        for(int i = 0; i < minionNames.size(); ++i){

            //region Display warnings if the user performs does risky stuff
            //New warnings get appended as the user does more and more stupid things for one minion in particular
            String warnings = "Extra Info:";
            //endregion

            //Figure out which upgrades we are dealing with for this Minion
            String name = minionNames.get(i);
            //Since we have a lot of minions, this creates and retrieves keys programmatically
            String upgrade1 = name + "upgrade1";
            String upgrade2 = name + "upgrade2";
            String thisUpgrade1 = settings.getString(upgrade1,defaultUpgrade1);
            String thisUpgrade2 = settings.getString(upgrade2,defaultUpgrade2);
            //regionDetermine the speed of the minion
                //Place to store the speeds
                Double[] timeBetweenActions = new Double[6];

                //region Initialize all multipliers
                int fuelType = settings.getInt(name + "fuelType",1);
                double fuelNumber = 0;
                int multiplierNumberInt = 1;
                if (fuelType == 1) {
                    fuelNumber = (double)settings.getInt(name + "normalFuelNumber",normalFuelNumber);
                } else {
                    multiplierNumberInt = settings.getInt(name + "catalystFuelNumber",catalystFuelNumber);
                }
                String additionalMultiplier = settings.getString(name + "additionalMultiplier","1");

                double multiplierNumber = multiplierNumberInt * Double.parseDouble(additionalMultiplier);
                if (Double.parseDouble(additionalMultiplier) > 1) {
                    warnings = warnings + "\nNote: You are using an extra multiplier for some reason. Congrats on completing the challenge, and for finding whatever online trick you did that makes this necessary.";
                }
                int woodChecked = settings.getInt("woodChecked",0);
                int farmChecked = settings.getInt("farmChecked",0);
                double petBoost = (double)settings.getInt(name+"petBoost",0)/10;
                double miscBoost1 = Double.parseDouble(settings.getString(name + "miscBoost1","0.0"));
                double miscBoost2 = Double.parseDouble(settings.getString(name + "miscBoost2","0.0"));

                miscBoost1 = miscBoost1 / 100;
                miscBoost2 = miscBoost2 / 100;

                if (petBoost > 0) {
                    warnings = warnings + "\nNote: You are using a pet boost of " + petBoost + "%. This probably only works if you are on your island." +
                            "\nAlso, make sure your pet actually affects this minion.";
                }

                petBoost = petBoost / 100;

                double boost1 = 0;
                if (thisUpgrade1.equals("Minion Expander 5%")) {
                    boost1 = 0.05;
                } else if (thisUpgrade1.equals("Custom Speed Boost " + customBoostNormal +"%")) {
                    boost1 = (double)customBoostNormal / 100;
                }

                double boost2 = 0;
                if (thisUpgrade2.equals("Minion Expander 5%")) {
                    boost2 = 0.05;
                } else if (thisUpgrade2.equals("Custom Speed Boost " + customBoostNormal +"%")) {
                    boost2 = (double)customBoostNormal / 100;
                }

                double fuelNumberForCalculations = fuelNumber;

                if (thisUpgrade1.equals("Flycatcher") || thisUpgrade2.equals("Flycatcher")) {
                    fuelNumberForCalculations = fuelNumberForCalculations + 20;
                } else if (thisUpgrade1.equals("Custom Fuel Boost " + customBoostFly + "%") || thisUpgrade2.equals("Custom Fuel Boost " + customBoostFly + "%")) {
                    fuelNumberForCalculations = fuelNumberForCalculations + customBoostFly;
                }

                fuelNumberForCalculations = fuelNumberForCalculations / 100;
                fuelNumber = fuelNumber / 100;

                double woodBoost = 0;
                if (woodChecked == 1) {
                    switch (name) {
                        case "Oak Minion":
                        case "Birch Minion":
                        case "Jungle Minion":
                        case "Dark Oak Minion":
                        case "Acacia Minion":
                        case "Spruce Minion":
                            woodBoost = 0.1;
                            warnings = warnings + "\nNote: Using Woodcutting Crystal.";
                    }
                }
                double farmBoost = 0;
                if (farmChecked == 1) {
                    switch (name) {
                        case "Wheat Minion":
                        case "Carrot Minion":
                        case "Potato Minion":
                        case "Pumpkin Minion":
                        case "Melon Minion":
                        case "Mushroom Minion":
                        case "Cocoa Beans Minion":
                        case "Cactus Minion":
                        case "Sugar Cane Minion":
                        case "Nether Wart Minion":
                            farmBoost = 0.1;
                            warnings = warnings + "\nNote: Using Farm Crystal.";
                    }
                }
                //endregion

                //region Apply Multipliers to each relevant tier:
                    int counterTBA = 0;
                    for (int j = 0; j < minionSpeeds.get(i).length; j+=2) {
                        Double initialTime = minionSpeeds.get(i)[j];
                        //Account for fuel
                        initialTime = RoundDownTwentieth(initialTime / (1 + fuelNumberForCalculations));
                        //Account for crystals
                        initialTime = RoundDownTwentieth(initialTime/ (1 + woodBoost));
                        initialTime = RoundDownTwentieth(initialTime/ (1 + farmBoost));
                        //Account for Upgrades
                        initialTime = RoundDownTwentieth(initialTime / (1 + boost1));
                        initialTime = RoundDownTwentieth(initialTime / (1 + boost2));
                        //Account for Pets
                        initialTime = RoundDownTwentieth(initialTime / (1 + petBoost));
                        //Account for misc boosts
                        initialTime = RoundDownTwentieth(initialTime/ (1 + miscBoost1));
                        initialTime = RoundDownTwentieth(initialTime/ (1 + miscBoost2));

                        timeBetweenActions[counterTBA] = initialTime;
                        counterTBA += 1;
                    }
                //endregion

            //endregion

            //regionFigure out which products this minion produces
                ArrayList<String> products = new ArrayList<>();
                ArrayList<Double> npcPrices = new ArrayList<>();
                ArrayList<Double> itemsPerAction = new ArrayList<>();
                for (int j = 0; j < minionProducts.get(i).length; ++j) {
                    //Account for all the different products that can exist if certain upgrades are used
                    //Change should only happen on the first element
                    if (j == 0) {
                        switch (name) {
                            case "Cactus Minion":
                                if (thisUpgrade1.equals("Auto Smelter") || thisUpgrade2.equals("Auto Smelter")) {
                                    products.add("CACTUS_GREEN");
                                    npcPrices.add(1.0);
                                    itemsPerAction.add(3.0);
                                } else {
                                    products.add("CACTUS");
                                    npcPrices.add(1.0);
                                    itemsPerAction.add(1.0);
                                    warnings = warnings + "\nWarning: No Auto Smelter used; cactus is not compactable.";
                                }
                                break;
                            case "Gravel Minion":
                                if (thisUpgrade1.equals("Flint Shovel") || thisUpgrade2.equals("Flint Shovel")) {
                                    products.add("FLINT");
                                    npcPrices.add(4.0);
                                    itemsPerAction.add(1.0);
                                } else {
                                    products.add("GRAVEL");
                                    npcPrices.add(3.0);
                                    warnings = warnings + "\nWwarning: No Flint Shovel used; gravel is not compactable";
                                    itemsPerAction.add(1.0);
                                }
                                break;
                            case "Iron Minion":
                                if (thisUpgrade1.equals("Auto Smelter") || thisUpgrade2.equals("Auto Smelter")) {
                                    products.add("IRON_INGOT");
                                    npcPrices.add(3.0);
                                    itemsPerAction.add(1.0);
                                } else {
                                    products.add("IRON_ORE");
                                    npcPrices.add(3.0);
                                    itemsPerAction.add(1.0);
                                    warnings = warnings + "\nWarning: No Auto Smelter used; iron ore is not compactable";
                                }
                                break;
                            case "Gold Minion":
                                if (thisUpgrade1.equals("Auto Smelter") || thisUpgrade2.equals("Auto Smelter")) {
                                    products.add("GOLD_INGOT");
                                    npcPrices.add(4.0);
                                    itemsPerAction.add(1.0);
                                } else {
                                    products.add("GOLD_ORE");
                                    npcPrices.add(3.0);
                                    itemsPerAction.add(1.0);
                                    warnings = warnings + "\nWarning: No Auto Smelter used; gold ore is not compactable";
                                }
                                break;
                            default:
                                //Rest of the minions are pretty straightforward
                                products.add(minionProducts.get(i)[j]);
                                npcPrices.add(minionNPCValues.get(i)[j]);
                                itemsPerAction.add(minionItemsPerAction.get(i)[j]);
                                break;
                        }
                    } else {
                        //The weird product is the first one, the rest should be fine, for those that even produce anything
                        products.add(minionProducts.get(i)[j]);
                        npcPrices.add(minionNPCValues.get(i)[j]);
                        itemsPerAction.add(minionItemsPerAction.get(i)[j]);
                    }
                }

                //Fishing minions have a different multiplier than everything else due to items per action
                int timeMultiplier = 2;
                if (name.equals("Fishing Minion")) {
                    timeMultiplier = 1;
                }

                //Add additional items because of diamond spreading or enchanted egg
                if (thisUpgrade1.equals("Diamond Spreading") || thisUpgrade2.equals("Diamond Spreading")) {
                    products.add("DIAMOND");
                    npcPrices.add(8.0);
                    //Diamond spreading produces 1 extra diamond for every 10 items produced by the minion
                    itemsPerAction.add(0.1*ArraySum(minionItemsPerAction.get(i)));
                    if (thisUpgrade1.equals("Diamond Spreading") && thisUpgrade2.equals("Diamond Spreading")) {
                        warnings = warnings + "WARNING: YOU CAN'T ACTUALLY USE 2 DIAMOND SPREADINGS (the code runs assuming you picked just one, please select another upgrade)";
                    }
                } else if (name.equals("Chicken Minion") && (thisUpgrade1.equals("Enchanted Egg") || thisUpgrade2.equals("Enchanted Egg"))) {
                    products.add("EGG");
                    npcPrices.add(3.0);
                    itemsPerAction.add(1.0);
                }
            //endregion

            //region Get Enchanted Prices / Bazaar Prices
            //Get enchanted prices for NPC
                ArrayList<String> possibleNormalBazaarWarning = new ArrayList<>();
                ArrayList<String> possibleEnchantedBazaarWarning = new ArrayList<>();
                ArrayList<Double> enchantedNPCPrices = new ArrayList();
                ArrayList<Double> bazaarNormalPrices = new ArrayList<>();
                ArrayList<Double> bazaarEnchantedPrices = new ArrayList<>();
                ArrayList<String> enchantedNames = new ArrayList<>();
                Double[] enchantedDivider = new Double[npcPrices.size()];
                for (int j = 0; j < npcPrices.size(); ++j) {
                    //Deal with enchanted prices
                    double currentPrice = npcPrices.get(j);
                    if (j == 0) {
                        switch (name) {
                            case "Spider Minion":
                            case "Tarantula Minion":
                                currentPrice *= 196;
                                enchantedDivider[j] = 196.0;
                                break;
                            case "Ghast Minion":
                                currentPrice *= 5;
                                enchantedDivider[j] = 5.0;
                                break;
                            case "Enderman Minion":
                                currentPrice *= 20;
                                enchantedDivider[j] = 20.0;
                                break;
                            case "Snow Minion":
                                currentPrice *= 640;
                                enchantedDivider[j] = 640.0;
                                break;
                            case "Wheat Minion":
                                currentPrice *= 1296.0;
                                enchantedDivider[j] = 1296.0;
                                break;
                            default:
                                currentPrice *= 160;
                                enchantedDivider[j] = 160.0;
                        }
                    } else if (j == 6) {
                        switch (name) {
                            case "Fishing Minion":
                                //Sponges, if you were curious
                                currentPrice *= 40;
                                enchantedDivider[j] = 40.0;
                                break;
                            default:
                                currentPrice *= 160;
                                enchantedDivider[j] = 160.0;
                        }
                    } else if (j == 1) {
                        switch (name) {
                            case "Cow Minion":
                                currentPrice *= 576;
                                enchantedDivider[j] = 576.0;
                                break;
                            default:
                                currentPrice *= 160;
                                enchantedDivider[j] = 160.0;
                        }
                    } else if (j == 2) {
                        switch (name) {
                            case "Rabbit Minion":
                                currentPrice *= 576;
                                enchantedDivider[j] = 576.0;
                                break;
                            default:
                                currentPrice *= 160;
                                enchantedDivider[j] = 160.0;
                        }
                    } else {
                        currentPrice *= 160;
                        enchantedDivider[j] = 160.0;
                    }

                    enchantedNPCPrices.add(currentPrice);
                    //Deal with normal bazaar prices
                    double bazaarNormalPrice;
                    try {
                        bazaarNormalPrice = priceData.getJSONObject(products.get(j)).getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        possibleNormalBazaarWarning.add("\n" + products.get(j) + " cannot be sold on the bazaar.");
                        bazaarNormalPrice = 0;
                    }
                    bazaarNormalPrices.add(bazaarNormalPrice * bazaarTaxMultiplier);

                    //Deal with enchanted bazaar prices
                    //First get the name of the enchanted item
                    String enchantedProduct = new GetEnchantedName().codeName(products.get(j));
                    enchantedNames.add(enchantedProduct);
                    double bazaarEnchantedPrice;
                    try {
                        bazaarEnchantedPrice = priceData.getJSONObject(enchantedProduct).getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit");
                    } catch (JSONException e) {
                        bazaarEnchantedPrice = 0;
                        //While debugging I can find out which enchanted products I need to write exceptions for over in the GetEnchantedName class
                        Log.d("FIND_ENCHANTED",enchantedProduct);
                        possibleEnchantedBazaarWarning.add("\n" + enchantedProduct + " cannot be sold on the bazaar.");
                    }
                    bazaarEnchantedPrices.add(bazaarEnchantedPrice * bazaarTaxMultiplier);

                }
            //endregion

            //regionDetermine if the items produced will be enchanted or not
                //Determine if we need to use any of the warnings for "missing items in bazaar" from earlier
                boolean useNormalWarning = true;
                boolean useEnchantedWarning = true;
                //0 for normal, 1 for enchanted, 2 for either
                int productType = 0;
                ArrayList<String> npcPricesString = new ArrayList<>();
                ArrayList<String> bazaarPricesString = new ArrayList<>();
                if(thisUpgrade1.equals("Compactor") || thisUpgrade2.equals("Compactor")) {
                    switch(name) {
                        case "Coal Minion":
                        case "Diamond Minion":
                        case "Lapis Minion":
                        case "Emerald Minion":
                        case "Gold Minion":
                        case "Iron Minion":
                        case "Redstone Minion":
                            warnings = warnings + "\nNote: Using compactor, the blocks produced by this minion can be turned into either enchanted or normal items.";
                            for (int j = 0; j < enchantedNames.size();++j) {
                                products.set(j,products.get(j) + " OR " +enchantedNames.get(j));
                                bazaarPricesString.add(addCommasAdjusted(bazaarNormalPrices.get(j)) + " OR " + addCommasAdjusted(bazaarEnchantedPrices.get(j)));
                                npcPricesString.add(addCommasAdjusted(npcPrices.get(j)) + " OR " + addCommasAdjusted(npcPrices.get(j) * enchantedDivider[j]));
                            }
                            productType = 2;
                            break;
                        case "Glowstone Minion":
                        case "Snow Minion":
                        case "Quartz Minion":
                            warnings = warnings + "\nNote: Using compactor, the blocks produced by this minion can only be crafted into enchanted items.";
                            for (int j = 0; j < enchantedNames.size();++j) {
                                products.set(j,enchantedNames.get(j));
                                bazaarPricesString.add(addCommasAdjusted(Double.toString(bazaarEnchantedPrices.get(j))));
                                npcPricesString.add(addCommasAdjusted(Double.toString(npcPrices.get(j)*enchantedDivider[j])));
                            }
                            productType = 1;
                            break;
                        case "Clay Minion":
                            warnings = warnings + "\nNote: Using compactor, the blocks produced by this minion can only be crafted into enchanted items.";
                            for (int j = 0; j < enchantedNames.size();++j) {
                                if (j == 0) {
                                    products.set(0, "CLAY_BLOCK");
                                } else {
                                    products.set(1, products.get(j) + "_BLOCK");
                                }
                                bazaarPricesString.add(addCommasAdjusted(Double.toString(bazaarEnchantedPrices.get(j))));
                                npcPricesString.add(Double.toString(npcPrices.get(j)*enchantedDivider[j]));
                            }
                            productType = 1;
                            break;
                    }
                }

                if(thisUpgrade1.equals("Super Compactor") || thisUpgrade2.equals("Super Compactor")) {
                    for (int j = 0; j < enchantedNames.size();++j) {
                        products.set(j,enchantedNames.get(j));
                        bazaarPricesString.add(addCommasAdjusted(Double.toString(bazaarEnchantedPrices.get(j))));
                        npcPricesString.add(addCommasAdjusted(Double.toString(npcPrices.get(j)*enchantedDivider[j])));
                        useNormalWarning = false;
                    }
                    productType = 1;
                } else {
                    useEnchantedWarning = false;
                }

                //Catch some leakage
                if (npcPricesString.size() == 0) {
                    for (int j = 0; j < enchantedNames.size(); ++j) {
                        bazaarPricesString.add(addCommasAdjusted(Double.toString(bazaarNormalPrices.get(j))));
                        npcPricesString.add(addCommasAdjusted(Double.toString(npcPrices.get(j))));
                    }
                    productType = 0;
                }

                //regionDeal with the mess that are wheat minions
                if (name.equals("Wheat Minion")) {
                    if ((thisUpgrade1.equals("Super Compactor") && thisUpgrade2.equals("Compactor")) ||
                            (thisUpgrade1.equals("Compactor") && thisUpgrade2.equals("Super Compactor"))) {
                        enchantedDivider[0] = 1296.0;
                        for (int j = 0; j < enchantedNames.size(); ++j) {
                            products.set(j, enchantedNames.get(j));
                            bazaarPricesString.add(addCommasAdjusted(Double.toString(bazaarEnchantedPrices.get(j))));
                            npcPricesString.add(addCommasAdjusted(Double.toString(npcPrices.get(j))));
                        }
                        productType = 1;
                    } else {
                        warnings = warnings + "\nWARNING: ONLY VIABLE UPGRADE COMBINATION IS SUPER COMPACTOR + COMPACTOR. ANY OTHER COMBINATION AND YOUR MINION EITHER MAKES ENCHANTED BREAD (WHICH IS TRASH) OR IT FILLS UP WITH SEEDS WITHOUT MAKING ANY OTHER PRODUCTS, ITS JUST A MESS, TRUST ME OK. ALSO I TRIED TO WRITE THE CODE FOR THE REST OF THE EDGE CASES BUT IT JUST TOOK TOO LONG AND I GAVE UP. SO EITHER USE WHEAT MINION PROPERLY OR NOT AT ALL.";
                    }
                    //region Attempt to fix the rest before giving up.
//                    else if (thisUpgrade1.equals("Compactor") || thisUpgrade2.equals("Compactor")) {
//                        warnings = warnings + "\nUSE COMPACTOR + SUPER COMPACTOR, DO NOT SMALL BRAIN THIS, YOUR MINION WILL FILL WITH SEEDS (seriously screw you, I had to write 100 lines of code so you could have the chance to screw this up and still have it work)";
//                        products.set(0, "HAY_BLOCK");
//                        products.set(1, "SEEDS");
//                        enchantedDivider[0] = 9.0;
//                        enchantedDivider[1] = 1.0;
//                        if (enchantedDivider.length > 2) {
//                            enchantedDivider[2] = 9.0;
//                        }
//                        if (products.size() > 2) {
//                            products.set(2, "DIAMOND_BLOCK");
//                        }
//                        for (int j = 0; j < enchantedNames.size(); ++j) {
//                            if (j == 0) {
//                                npcPricesString.add("9.0");
//                                try {
//                                    bazaarPricesString.add(addCommasAdjusted(Double.toString(priceData.getJSONObject("HAY_BLOCK").getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit")*bazaarTaxMultiplier)));
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//                                npcPrices.set(0, 1.0);
//                            } else if (j == 1) {
//                                npcPricesString.add("0.5");
//                                try {
//                                    bazaarPricesString.add(addCommasAdjusted(Double.toString(priceData.getJSONObject("SEEDS").getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit")*bazaarTaxMultiplier)));
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//                                npcPrices.set(1, 0.5);
//                            } else {
//                                npcPricesString.add("8.0 OR 1,280.0");
//                                try {
//                                    bazaarPricesString.add(addCommasAdjusted(priceData.getJSONObject("DIAMOND").getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit")*bazaarTaxMultiplier) + " OR " +
//                                            addCommasAdjusted(priceData.getJSONObject("ENCHANTED_DIAMOND").getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit")*bazaarTaxMultiplier));
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//                                npcPrices.set(2, 8.0);
//                            }
//                        }
//                        productType = 1;
//                    } else if (thisUpgrade1.equals("Super Compactor") || thisUpgrade2.equals("Super Compactor")) {
//                        warnings = warnings + "\nUSE COMPACTOR + SUPER COMPACTOR, DO NOT SMALL BRAIN THIS, YOUR MINION WILL MAKE USELESS ENCHANTED BREAD (seriously screw you, I had to write 100 lines of code so you could have the chance to screw this up and still have it work)";
//                        products.set(0, "ENCHANTED_BREAD");
//                        products.set(1, "ENCHANTED_SEEDS");
//                        enchantedDivider[0] = 60.0;
//                        if (products.size() > 2) {
//                            products.set(2, "ENCHANTED_DIAMOND");
//                        }
//                        for (int j = 0; j < enchantedNames.size(); ++j) {
//                            if (j == 0) {
//                                npcPricesString.add("60.0");
//                                try {
//                                    bazaarPricesString.add(addCommasAdjusted(Double.toString(priceData.getJSONObject("ENCHANTED_BREAD").getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit")*bazaarTaxMultiplier)));
//                                    bazaarEnchantedPrices.set(0,priceData.getJSONObject("ENCHANTED_BREAD").getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit"));
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//                                npcPrices.set(0, 1.0);
//                            } else if (j == 1) {
//                                npcPricesString.add("80.0");
//                                try {
//                                    bazaarPricesString.add(addCommasAdjusted(Double.toString(priceData.getJSONObject("ENCHANTED_SEEDS").getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit")*bazaarTaxMultiplier)));
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//                                npcPrices.set(1, 0.5);
//                            } else {
//                                npcPricesString.add("1,280.0");
//                                try {
//                                    bazaarPricesString.add(addCommasAdjusted(Double.toString(priceData.getJSONObject("ENCHANTED_DIAMOND").getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit")*bazaarTaxMultiplier)));
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//                                npcPrices.set(2, 8.0);
//                            }
//                        }
//                        productType = 1;
//                    }
                    //endregion
                }
                //endregion

                //determine if the warnings need to be used
                if (useNormalWarning) {
                    for (int j = 0; j < possibleNormalBazaarWarning.size(); ++j) {
                        warnings = warnings + possibleNormalBazaarWarning.get(j);
                    }
                }

                if (useEnchantedWarning) {
                    for (int j = 0; j< possibleEnchantedBazaarWarning.size(); ++j) {
                        warnings = warnings + possibleEnchantedBazaarWarning.get(j);
                    }
                }
            //endregion

            //regionAdd all the information about this minion to the minions ArrayList
                String tierProfits = "Profits by Tier:";
                String productsString = "Products:";
                String NPCPrices = "NPC Prices:";
                String bazaarPrices = "Bazaar Prices:";
                String itemsPerActionString = "Items Produced per Action";
                double ranking = 0;

                int current_tier = 1;

                //Warn if no compactors of any kind found
                if (productType == 0) {
                    warnings = warnings + "\nWarning: No compactors found. Minion will fill quickly, remember to empty.";
                }

                //region Fill out Tier info
                for (int j = 0; j < timeBetweenActions.length;++j) {
                    String currentTierInfo = "\n" + current_tier + ". Coins Per Day: ";
                    double profit = 0;
                    if (productType == 0) {
                        //For normal ones
                        for (int eachProduct = 0; eachProduct < npcPrices.size(); ++eachProduct) {
                            double currentNPCProfit = 86400 / (timeBetweenActions[j] * timeMultiplier)
                                    * npcPrices.get(eachProduct) * itemsPerAction.get(eachProduct) * multiplierNumber;
                            double currentBazaarProfit = 86400 / (timeBetweenActions[j] * timeMultiplier)
                                    * bazaarNormalPrices.get(eachProduct) * itemsPerAction.get(eachProduct) * multiplierNumber;
                            if (currentBazaarProfit < currentNPCProfit) {
                                profit += currentNPCProfit;
                                if (j == 0) {
                                    String possibleCorrection = new FixBadNames().fix(products.get(eachProduct));
                                    if (possibleCorrection != null) {
                                        warnings = warnings + "\nWarning: Better to sell " + possibleCorrection + " to the NPC.";
                                    } else {
                                        warnings = warnings + "\nWarning: Better to sell " + products.get(eachProduct) + " to the NPC.";
                                    }
                                }
                            } else {
                                profit += currentBazaarProfit;
                            }
                        }
                    } else if (productType == 1) {
                        //For enchanted
                        for (int eachProduct = 0; eachProduct < npcPrices.size(); ++eachProduct) {
                            double currentNPCProfit = 86400 /  (timeBetweenActions[j] * enchantedDivider[eachProduct] *
                                    timeMultiplier) * npcPrices.get(eachProduct) * enchantedDivider[eachProduct] * itemsPerAction.get(eachProduct) * multiplierNumber;
                            double currentBazaarProfit = 86400 / (timeBetweenActions[j] * enchantedDivider[eachProduct] * timeMultiplier)
                                    * bazaarEnchantedPrices.get(eachProduct) * itemsPerAction.get(eachProduct) * multiplierNumber;
                            if (currentBazaarProfit < currentNPCProfit) {
                                profit += currentNPCProfit;
                                if (j == 0) {
                                    String possibleCorrection = new FixBadNames().fix(products.get(eachProduct));
                                    if (possibleCorrection != null) {
                                        warnings = warnings + "\nWarning: Better to sell " + possibleCorrection + " to the NPC.";
                                    } else {
                                        warnings = warnings + "\nWarning: Better to sell " + products.get(eachProduct) + " to the NPC.";
                                    }
                                }
                            } else {
                                profit += currentBazaarProfit;
                            }
                        }
                    } else {
                        //Deal with if there's a regular compactor

                        double eProfit = 0;

                        //For enchanted
                        for (int eachProduct = 0; eachProduct < npcPrices.size(); ++eachProduct) {
                            double currentNPCProfit = 86400 /  (timeBetweenActions[j] * enchantedDivider[eachProduct] *
                                    timeMultiplier) * npcPrices.get(eachProduct) * enchantedDivider[eachProduct] * itemsPerAction.get(eachProduct) * multiplierNumber;
                            double currentBazaarProfit = 86400 / (timeBetweenActions[j] * enchantedDivider[eachProduct] * timeMultiplier)
                                    * bazaarEnchantedPrices.get(eachProduct) * itemsPerAction.get(eachProduct) * multiplierNumber;
                            if (currentBazaarProfit < currentNPCProfit) {
                                eProfit += currentNPCProfit;
                                if (j == 0) {
                                    String possibleCorrection = new FixBadNames().fix(products.get(eachProduct));
                                    if (possibleCorrection != null) {
                                        warnings = warnings + "\nWarning: Better to sell " + possibleCorrection + " to the NPC.";
                                    } else {
                                        warnings = warnings + "\nWarning: Better to sell " + products.get(eachProduct) + " to the NPC.";
                                    }
                                }
                            } else {
                                eProfit += currentBazaarProfit;
                            }
                        }

                        double nProfit = 0;
                        //For normal ones
                        for (int eachProduct = 0; eachProduct < npcPrices.size(); ++eachProduct) {
                            double currentNPCProfit = 86400 / (timeBetweenActions[j] * timeMultiplier)
                                    * npcPrices.get(eachProduct) * itemsPerAction.get(eachProduct) * multiplierNumber;
                            double currentBazaarProfit = 86400 / (timeBetweenActions[j] * timeMultiplier)
                                    * bazaarNormalPrices.get(eachProduct) * itemsPerAction.get(eachProduct) * multiplierNumber;
                            if (currentBazaarProfit < currentNPCProfit) {
                                nProfit += currentNPCProfit;
                                if (j == 0) {
                                    String possibleCorrection = new FixBadNames().fix(products.get(eachProduct));
                                    if (possibleCorrection != null) {
                                        warnings = warnings + "\nWarning: Better to sell " + possibleCorrection + " to the NPC.";
                                    } else {
                                        warnings = warnings + "\nWarning: Better to sell " + products.get(eachProduct) + " to the NPC.";
                                    }
                                }
                            } else {
                                nProfit += currentBazaarProfit;
                            }
                        }

                        //Determine which is better
                        if (nProfit > eProfit) {
                            profit = nProfit;
                            if (j == 0) {
                                warnings = warnings + "\nNote: More profit by crafting blocks into normal items and selling versus enchanted.";
                            }
                        } else {
                            profit = eProfit;
                            if (j == 0) {
                                warnings = warnings + "\nNote: More profit by crafting blocks into enchanted items and selling versus normal.";
                            }
                        }
                    }
                    currentTierInfo = currentTierInfo + addCommasAdjusted(Double.toString(Round1(profit)));
                    currentTierInfo = currentTierInfo + "\n     Time Between Actions: " + Round2(timeBetweenActions[j]);
                    tierProfits = tierProfits + currentTierInfo;
                    current_tier = current_tier + 2;
                    ranking += profit;
                }
                //endregion

                //regionCalculate items per action
                for (int j = 0; j < products.size(); ++j) {
                    String possibleCorrection = new FixBadNames().fix(products.get(j));
                    if (possibleCorrection != null) {
                        productsString = productsString + "\n" + possibleCorrection;
                    } else {
                        productsString = productsString + "\n" + products.get(j);
                    }
                    NPCPrices = NPCPrices + "\n" + npcPricesString.get(j);
                    bazaarPrices = bazaarPrices + "\n" + bazaarPricesString.get(j);

                    String productsMade;
                    String productsMade2 = null;
                    double productsNumber;
                    double productsNumber2 = 0;
                    if (productType == 1) {
                        productsNumber = (itemsPerAction.get(j) / timeMultiplier * multiplierNumber / enchantedDivider[j]);
                    } else if (productType == 0) {
                        productsNumber = (itemsPerAction.get(j) / timeMultiplier * multiplierNumber);
                    } else {
                        productsNumber = (itemsPerAction.get(j) / timeMultiplier * multiplierNumber);
                        productsNumber2 = (itemsPerAction.get(j) / timeMultiplier * multiplierNumber / enchantedDivider[j]);
                    }

                    if (productType == 2) {
                        NumberFormat format = new DecimalFormat("#.########");
                        productsMade = format.format(productsNumber);
                        productsMade2 = format.format(productsNumber2);
                        
                    } else {
                        NumberFormat format = new DecimalFormat("#.########");
                        productsMade = format.format(productsNumber);
                    }

                    if (productType == 2) {
                        itemsPerActionString = itemsPerActionString + "\n" + productsMade + " OR " + productsMade2;
                    } else {
                        itemsPerActionString = itemsPerActionString + "\n" + productsMade;
                    }
                }
                //endregion

                //regionFuel information
                boolean typeOfFuel;
                if (fuelType == 1) {
                    typeOfFuel = true;
                    fuelNumber = fuelNumber * 100;
                } else {
                    typeOfFuel = false;
                    fuelNumber = multiplierNumberInt;
                }
                //endregion

                Minion thisMinion = new Minion(name,tierProfits,productsString,itemsPerActionString,NPCPrices,
                        bazaarPrices,typeOfFuel, (int) fuelNumber,Double.parseDouble(additionalMultiplier),thisUpgrade1,thisUpgrade2,petBoost,miscBoost1,miscBoost2,warnings,ranking);
                minions.add(thisMinion);
            //endregion
        }

            //regionSort the minion list
                //List is pretty short, doesn't really matter how efficient, so I'm just going to use a bubble sort
                for (int a = 0; a < minions.size() - 1; ++a) {
                    for (int b = 0; b < minions.size() - a - 1; ++b) {
                        if (minions.get(b).getRankings() < minions.get(b+1).getRankings()) {
                            Collections.swap(minions,b,b+1);
                        }
                    }
                }
            //endregion

        //endregion

        //region Setup the MinionRecViewAdapter
            RecAdapter = new MinionRecViewAdapter(this);
            RecMinions.setAdapter(RecAdapter);
            RecMinions.setLayoutManager(new LinearLayoutManager(this));
            RecAdapter.setMinions(minions);
        //endregion
    }

    //Round to 1 decimal place
    public double Round1(double input) {
        BigDecimal bd = BigDecimal.valueOf(input);
        bd = bd.setScale(1, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    //Round to 2 decimal places
    public double Round2(double input) {
        BigDecimal bd = BigDecimal.valueOf(input);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    //Add commas to longer numbers
    public String addCommasAdjusted(String digits) {

        double placeholder = Double.parseDouble(digits);
        placeholder = Round1(placeholder);
        digits = Double.toString(placeholder);

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

    //Save a step of converting, this takes a double, converts it, then uses the original method
    public String addCommasAdjusted(double number) {
        number = Round1(number);
        return addCommasAdjusted(Double.toString(number));
    }

    //Find sum of array
    public double ArraySum(Double[] array) {
        double sum = 0;
        for(double numbers:array) {
            sum += numbers;
        }
        return sum;
    }

    //Rounds down to the nearest 0.05
    public double RoundDownTwentieth(double number) {
        return Math.floor(number * 20) / 20;
    }
}