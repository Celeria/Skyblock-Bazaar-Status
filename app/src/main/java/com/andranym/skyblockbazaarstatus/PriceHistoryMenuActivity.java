package com.andranym.skyblockbazaarstatus;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.room.Room;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.games.Games;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import static com.andranym.skyblockbazaarstatus.MainActivity.CHANNEL_ID;

public class PriceHistoryMenuActivity extends AppCompatActivity {

    static AppDatabase bazaarPriceHistory;
    Button btnScheduleData;
    RadioButton radioDropFirst, radioRiseFirst;
    Button btnNewDataNow;
    ListView listPriceChanges;
    EditText search;
    static int patienceCounter = 0;
    static boolean searchedItem = false;
    static boolean dropFirst;

    static boolean dataRetrievalFinished;

    static ArrayList<String> searchThis;
    static ArrayList<String> searchPlainNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_history_menu);

        setTitle("Price History");

        //regionDeclare UI elements
        //btnScheduleData = findViewById(R.id.btnScheduleData);
        radioDropFirst = findViewById(R.id.radioDropFirst);
        radioRiseFirst = findViewById(R.id.radioRiseFirst);
        listPriceChanges = findViewById(R.id.listPriceChanges);
        btnNewDataNow = findViewById(R.id.btnNewDataNow);
        search = findViewById(R.id.editFindPriceHistory);
        //endregion

        //App will crash if prices not loaded before you start searching
        search.setEnabled(false);
        search.setHint("Loading price histories...");

        //regionCode for the radio buttons deciding the order to display
        final SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (data.getBoolean("dropFirstIsChecked",true)){
            radioDropFirst.setChecked(true);
            dropFirst = true;
        } else {
            radioRiseFirst.setChecked(true);
            dropFirst = false;
        }

        radioDropFirst.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    SharedPreferences.Editor editor = data.edit();
                    editor.putBoolean("dropFirstIsChecked", true);
                    editor.commit();
                    dropFirst = true;
                    searchedItem = false;
                }
            }
        });

        radioRiseFirst.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    SharedPreferences.Editor editor = data.edit();
                    editor.putBoolean("dropFirstIsChecked", false);
                    editor.commit();
                    dropFirst = false;
                    searchedItem = false;
                }
            }
        });
        //endregion

        //regionSubmit high score for notifications
        try {
            int notificationsSent = data.getInt("numberOfNotificationsSent",0);
            Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                    .submitScore(getString(R.string.leaderboard_notifications_received), (long)notificationsSent);
        } catch(Exception e){
            //do nothing
        }
        //endregion

        //regionThread that sorts the data to the user's liking
        new Thread(){
            @Override
            public void run() {
                super.run();
                boolean stateChanged = true;
                boolean previousState = dropFirst;
                while (true) {
                    if (!searchedItem && stateChanged){
                        previousState = dropFirst;
                        stateChanged = false;

                        //regionLoad old data from database
                        bazaarPriceHistory = Room.databaseBuilder(getApplicationContext(),
                                AppDatabase.class, "BazaarPriceHistoryDB")
                                .build();
                        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                        List<BazaarItem> allItems = bazaarPriceHistory.BazaarDao().getAllItems();
                        final ArrayList<String> itemNameList = new ArrayList<>();
                        ArrayList<Double> buyPriceList = new ArrayList<>();
                        ArrayList<Double> sellPriceList = new ArrayList<>();
                        ArrayList<Double> avgBuyPriceList = new ArrayList<>();
                        ArrayList<Double> avgSellPriceList = new ArrayList<>();
                        for(BazaarItem currentItem:allItems) {
                            //Add the item name to the list
                            itemNameList.add(currentItem.getItemName());

                            //To store in database, everything was in strings, convert them back to their original types
                            ArrayList<String> buyPricesString = currentItem.getBuyPrices();
                            ArrayList<String> sellPricesString = currentItem.getSellPrices();
                            ArrayList<String> timesRetrievedString = currentItem.getTimesRetrieved();

                            ArrayList<Double> buyPrices = new ArrayList<>();
                            ArrayList<Double> sellPrices = new ArrayList<>();
                            ArrayList<Long> timesRetrieved = new ArrayList<>();
                            for (int i = 0; i < timesRetrievedString.size(); ++i) {
                                buyPrices.add(Double.parseDouble(buyPricesString.get(i)));
                                sellPrices.add(Double.parseDouble(sellPricesString.get(i)));
                                timesRetrieved.add(Long.parseLong(timesRetrievedString.get(i)));
                            }

                            long maxTimeBack = settings.getLong("calculationTimeHistory", 1000 * 60 * 60 * 24 * 7); //One week default
                            ArrayList<Integer> compatibleEntries = new ArrayList<>();
                            //Go backwards through the timesRetrieved Arraylist, and see which ones are within the calculation interval
                            for (int i = timesRetrieved.size() - 2; i >= 0; --i) {
                                if (System.currentTimeMillis() - timesRetrieved.get(i) < maxTimeBack) {
                                    compatibleEntries.add(i);
                                }
                            }

                            //Get the current buy and sell prices
                            double currentBuyPrice = buyPrices.get(timesRetrieved.size() - 1);
                            double currentSellPrice = sellPrices.get(timesRetrieved.size() - 1);

                            //Store them for later
                            buyPriceList.add(currentBuyPrice);
                            sellPriceList.add(currentSellPrice);

                            //Calculate historical average price, for comparison
                            double averageBuyPrice = 0;
                            double averageSellPrice = 0;

                            for (int i : compatibleEntries) {
                                averageBuyPrice += buyPrices.get(i);
                                averageSellPrice += sellPrices.get(i);
                            }

                            try {
                                averageBuyPrice /= compatibleEntries.size();
                                averageSellPrice /= compatibleEntries.size();
                            } catch (Exception e) {
                                averageBuyPrice = currentBuyPrice;
                                averageSellPrice = currentSellPrice;
                            }

                            //Outlier filtration
                            if (compatibleEntries.size() > 10) {
                                //Sort by price, then remove the tops and bottoms of the set
                                ArrayList<Double> compatibleBuyPrices = new ArrayList<>();
                                ArrayList<Double> compatibleSellPrices = new ArrayList<>();

                                for(int entry: compatibleEntries){
                                    compatibleBuyPrices.add(buyPrices.get(entry));
                                    compatibleSellPrices.add(sellPrices.get(entry));
                                }

                                Collections.sort(compatibleBuyPrices);
                                Collections.sort(compatibleSellPrices);

                                float cutoff = settings.getFloat("outlierCutoffRange",(float)0.15);
                                int lowerCutoff = (int) (compatibleEntries.size()  * cutoff);
                                int higherCutoff = compatibleEntries.size() - lowerCutoff;

                                double newAverageBuyPrice = 0;
                                double newAverageSellPrice = 0;
                                for(int i = lowerCutoff; i < higherCutoff; ++i) {
                                    newAverageBuyPrice += compatibleBuyPrices.get(i);
                                    newAverageSellPrice += compatibleSellPrices.get(i);
                                }
                                newAverageBuyPrice /= higherCutoff - lowerCutoff;
                                newAverageSellPrice /= higherCutoff - lowerCutoff;

                                averageBuyPrice = newAverageBuyPrice;
                                averageSellPrice = newAverageSellPrice;
                            }

                            //Store them for later
                            avgBuyPriceList.add(averageBuyPrice);
                            avgSellPriceList.add(averageSellPrice);
                        }
                        //endregion

                        //regionSort the lists by volatility (average buy price change + average sell price change)
                        Map<Double,String> volatilityMeasure = new HashMap<>();
                        for(int i = 0; i < itemNameList.size(); ++i) {
                            double currentBuyPrice = buyPriceList.get(i);
                            double currentSellPrice = sellPriceList.get(i);
                            double avgBuyPrice = avgBuyPriceList.get(i);
                            double avgSellPrice = avgSellPriceList.get(i);

                            double buyVolatility = (currentBuyPrice - avgBuyPrice)/avgBuyPrice;
                            double sellVolatility = (currentSellPrice - avgSellPrice)/avgSellPrice;
                            //This will not affect the ranking, but ensures that every single key is unique, otherwise
                            //if by sheer coincidence, two prices both change by exactly 10%, its good to be sure
                            double uniqueGuarantee = (double)(new Random().nextInt(100))/100000000;
                            //Add to the list the volatility and the name of the item, so they get sorted together
                            volatilityMeasure.put(buyVolatility + sellVolatility + uniqueGuarantee, itemNameList.get(i));
                        }

                        //why write your own sorting algorithm when treemap has one?
                        Map<Double, String> volatilityMeasureSorted = new TreeMap<>(volatilityMeasure);

                        //Store a sorted collection of strings containing all the necessary information to put in the list
                        final ArrayList<String> displayData = new ArrayList<>();

                        //Keep an ArrayList of the order, which might be useful later.
                        final ArrayList<Integer> storeOrder = new ArrayList<>();

                        //Add list of search terms
                        searchPlainNames = new ArrayList<>();

                        //This loop will run the previously sorted data
                        for(double volatility: volatilityMeasureSorted.keySet()){
                            String productName = volatilityMeasureSorted.get(volatility);
                            searchPlainNames.add(productName);
                            //All the previous lists are sorted the same way, so just use the position and we can get all the previous data needed
                            int position = itemNameList.indexOf(productName);
                            storeOrder.add(position);
                            double currentBuyPrice = buyPriceList.get(position);
                            double currentSellPrice = sellPriceList.get(position);
                            double avgBuyPrice = avgBuyPriceList.get(position);
                            double avgSellPrice = avgSellPriceList.get(position);
                            double buyVolatility = (currentBuyPrice - avgBuyPrice)/avgBuyPrice;
                            double sellVolatility = (currentSellPrice - avgSellPrice)/avgSellPrice;

                            //Put a + sign in front of positive percentages
                            String positiveBuy = "";
                            if (buyVolatility > 0) {
                                positiveBuy = "+";
                            }
                            String positiveSell = "";
                            if (sellVolatility > 0) {
                                positiveSell = "+";
                            }

                            //Get the final string to display, and add it to the list
                            String display = new FixBadNamesImproved().fix(productName) + ":\n" +
                                    "   Buy price: " + positiveBuy + Round(buyVolatility * 100,1) +
                                    "%\n     Was: " + addCommasAdjusted(Round(avgBuyPrice,1)) + "\n     Now: "
                                    + addCommasAdjusted(Round(currentBuyPrice,1)) +
                                    "\n   Sell price: " + positiveSell + Round(sellVolatility * 100,1) +
                                    "%\n     Was: " + addCommasAdjusted(Round(avgSellPrice,1)) + "\n     Now: "
                                    + addCommasAdjusted(Round(currentSellPrice,1));
                            displayData.add(display);
                        }
                        //endregion

                        //Sort according to user preference
                        if(!dropFirst) {
                            Collections.reverse(displayData);
                            Collections.reverse(storeOrder);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ArrayAdapter<String> ListAdapter = new ArrayAdapter<>(
                                        PriceHistoryMenuActivity.this,
                                        android.R.layout.simple_list_item_1,
                                        displayData
                                );
                                listPriceChanges.setAdapter(ListAdapter);
                                listPriceChanges.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                        final Intent viewGraphs = new Intent(getApplicationContext(), PriceHistoryOfItemActivity.class);
                                        //The extra is the item name, which I get from itemNameList. As for which one to get, that's
                                        //determined by storeOrder.
                                        viewGraphs.putExtra("itemNameAPI",itemNameList.get(storeOrder.get(i)));
                                        startActivity(viewGraphs);
                                    }
                                });
                            }
                        });

                        //Store just the strings to allow for faster searching
                        searchThis = new ArrayList<>(displayData);
                        searchPlainNames = new ArrayList<>();
                        for(int i = 0; i < storeOrder.size(); ++i){
                            searchPlainNames.add(itemNameList.get(storeOrder.get(i)));
                        }

                        //Allow user to search now that everything is loaded
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                search.setEnabled(true);
                                search.setHint("Search for an item");
                            }
                        });

                    } else {
                        //If the user changed the button, this will restart the loop
                        if (previousState != dropFirst){
                            stateChanged = true;
                        } else {
                            //Otherwise this thread takes a break and waits to see
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }.start();
        //endregion

        //regionThread that allows the user to search for an item they want
        new Thread(){
            @Override
            public void run() {
                super.run();
                String previousSearch = search.getText().toString();
                while(true) {
                    String currentSearch = search.getText().toString();
                    //Only perform work when the user has entered new text
                    if (currentSearch.equals(previousSearch) || currentSearch.equals("")) {
                        if(currentSearch.equals("")){
                            searchedItem = false;
                        }
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        searchedItem = true;
                        previousSearch = currentSearch;
                        //So there is consistency
                        currentSearch = currentSearch.toLowerCase();
                        //Store all of the things to search for
                        ArrayList<String> searchTerms = new ArrayList<>();
                        String currentSearchTerm = "";
                        //It makes sense that users will put spaces in between new terms, and the
                        //actual data does not use spaces, so I'll just run .contains on each term, separated
                        //by spaces
                        for (int i = 0; i < currentSearch.length(); ++i) {
                            if (currentSearch.substring(i, i + 1).equals(" ")) {
                                searchTerms.add(currentSearchTerm);
                                currentSearchTerm = "";
                            } else {
                                currentSearchTerm += (currentSearch.charAt(i));
                            }
                        }
                        searchTerms.add(currentSearchTerm);

                        //Get only the list of items the user wants
                        final ArrayList<String> fullDescriptionsWanted = new ArrayList<>();
                        final ArrayList<String> itemNamesWanted = new ArrayList<>();
                        for(int i = 0; i < searchPlainNames.size(); ++i){
                            String possibleProduct = searchPlainNames.get(i);
                            //Check all the search terms, if a product contains the term, add it to the list
                            for(String term : searchTerms) {
                                if(possibleProduct.toLowerCase().contains(term)){
                                    fullDescriptionsWanted.add(searchThis.get(i));
                                    itemNamesWanted.add(possibleProduct);
                                    break;
                                }
                            }
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ArrayAdapter<String> ListAdapter = new ArrayAdapter<>(
                                        PriceHistoryMenuActivity.this,
                                        android.R.layout.simple_list_item_1,
                                        fullDescriptionsWanted
                                );
                                listPriceChanges.setAdapter(ListAdapter);
                                //Make each one clickable so you can view expanded graphs
                                listPriceChanges.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                        final Intent viewGraphs = new Intent(getApplicationContext(), PriceHistoryOfItemActivity.class);
                                        //The extra is the item name, which I get from itemNameList. As for which one to get, that's
                                        //determined by storeOrder.
                                        viewGraphs.putExtra("itemNameAPI",itemNamesWanted.get(i));
                                        startActivity(viewGraphs);
                                    }
                                });
                            }
                        });

                    }
                }
            }
        }.start();
        //endregion

        //regionRunnable which can store data in the background
        final Runnable RetrieveAndStoreData = new Runnable() {
            @Override
            public void run() {
                Log.d("StartedDataStorage","started");
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                //Retrieve current data from Hypixel API
                JSONObject currentBazaarInformation = null;
                try {
                    currentBazaarInformation = new JSONObject(new RetrieveData().execute("https://api.hypixel.net/skyblock/bazaar").get());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //Load old data from database
                bazaarPriceHistory = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "BazaarPriceHistoryDB")
                        .build();

                //regionTake data retrieved, and update the stored database.
                try {
                    JSONObject productInformation = currentBazaarInformation.getJSONObject("products");
                    //Current time that the data has been retrieved
                    long timeRetrieved = currentBazaarInformation.getLong("lastUpdated");
                    Iterator<String> productIterator = productInformation.keys();
                    while (productIterator.hasNext()){
                        String current_product = productIterator.next();
                        //Get the lowest instant buy and instant sell prices for the current moment
                        double sellPrice;
                        try {
                            sellPrice = productInformation.getJSONObject(current_product).getJSONArray("sell_summary").getJSONObject(0).getDouble("pricePerUnit");
                        } catch (Exception e) {
                            //Nobody is selling this item, so set the buy and sell prices to zero, usually caused by
                            //admins adding an item with an incorrect name into the API, and not bothering to remove it
                            sellPrice = 0.0;
                        }
                        double buyPrice;
                        try {
                            buyPrice = productInformation.getJSONObject(current_product).getJSONArray("buy_summary").getJSONObject(0).getDouble("pricePerUnit");
                        } catch (Exception e) {
                            //Nobody is selling this item, so set the buy and sell prices to zero, usually caused by
                            //admins adding an item with an incorrect name into the API, and not bothering to remove it
                            buyPrice = 0.0;
                        }
                        BazaarItem previousData = bazaarPriceHistory.BazaarDao().getAnAuctionItem(current_product);
                        if(previousData != null){
                            //Retrieve the old data, append the new data on, and then put it back
                            ArrayList<String> storedBuyPrices = previousData.getBuyPrices();
                            storedBuyPrices.add(Double.toString(buyPrice));
                            ArrayList<String> storedSellPrices = previousData.getSellPrices();
                            storedSellPrices.add(Double.toString(sellPrice));
                            ArrayList<String> storedTimesRetrieved = previousData.getTimesRetrieved();
                            storedTimesRetrieved.add(Long.toString(timeRetrieved));
                            BazaarItem updatedEntry = new BazaarItem(current_product,storedBuyPrices,storedSellPrices,storedTimesRetrieved);
                            bazaarPriceHistory.BazaarDao().insertAll(updatedEntry);
                        } else {
                            //Create new arrays, and store those
                            ArrayList<String> storedBuyPrices = new ArrayList<>();
                            storedBuyPrices.add(Double.toString(buyPrice));
                            ArrayList<String> storedSellPrices = new ArrayList<>();
                            storedSellPrices.add(Double.toString(sellPrice));
                            ArrayList<String> storedTimesRetrieved = new ArrayList<>();
                            storedTimesRetrieved.add(Long.toString(timeRetrieved));
                            BazaarItem newEntry = new BazaarItem(current_product,storedBuyPrices,storedSellPrices,storedTimesRetrieved);
                            bazaarPriceHistory.BazaarDao().insertAll(newEntry);
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //endregion

                //regionSend notifications to the user, if they desire such a thing
                if(settings.getBoolean("sendNotifications",true)){
                    //This one is a percentage, the price has to change by a certain percent before user is notified
                    float notificationThreshold = settings.getFloat("notificationThreshold", (float) 0.3);
                    //This one is a flat number, the price has to change by at least a set value before user is notified
                    float notificationThresholdCoins = settings.getFloat("notificationThresholdCoins",(float) 0);
                    int notificationsSent = settings.getInt("numberOfNotificationsSent",0);
                    List<BazaarItem> allItems = bazaarPriceHistory.BazaarDao().getAllItems();
                    for(BazaarItem currentItem:allItems){
                        String itemName = currentItem.getItemName();
                        //To store in database, everything was in strings, convert them back to their original types
                        ArrayList<String> buyPricesString = currentItem.getBuyPrices();
                        ArrayList<String> sellPricesString = currentItem.getSellPrices();
                        ArrayList<String> timesRetrievedString = currentItem.getTimesRetrieved();

                        ArrayList<Double> buyPrices = new ArrayList<>();
                        ArrayList<Double> sellPrices = new ArrayList<>();
                        ArrayList<Long> timesRetrieved = new ArrayList<>();
                        for(int i = 0; i < timesRetrievedString.size(); ++i){
                            buyPrices.add(Double.parseDouble(buyPricesString.get(i)));
                            sellPrices.add(Double.parseDouble(sellPricesString.get(i)));
                            timesRetrieved.add(Long.parseLong(timesRetrievedString.get(i)));
                        }

                        long maxTimeBack = settings.getLong("calculationTimeHistory",1000*60*60*24*7); //One week default
                        ArrayList<Integer> compatibleEntries = new ArrayList<>();
                        //Go backwards through the timesRetrieved Arraylist, and see which ones are within the calculation interval
                        for (int i = timesRetrieved.size() - 2; i >= 0; --i) {
                            if (System.currentTimeMillis() - timesRetrieved.get(i) < maxTimeBack){
                                compatibleEntries.add(i);
                            }
                        }

                        //Get the current buy and sell prices
                        double currentBuyPrice = buyPrices.get(timesRetrieved.size()-1);
                        double currentSellPrice = sellPrices.get(timesRetrieved.size()-1);

                        //Calculate historical average price, for comparison
                        double averageBuyPrice = 0;
                        double averageSellPrice = 0;
                        for (int i:compatibleEntries){
                            averageBuyPrice += buyPrices.get(i);
                            averageSellPrice += sellPrices.get(i);
                        }

                        try {
                            averageBuyPrice /= compatibleEntries.size();
                            averageSellPrice /= compatibleEntries.size();
                        } catch (Exception e) {
                            averageBuyPrice = currentBuyPrice;
                            averageSellPrice = currentSellPrice;
                        }

                        //Outlier filtration
                        if (compatibleEntries.size() > 10) {
                            //Sort by price, then remove the tops and bottoms of the set
                            ArrayList<Double> compatibleBuyPrices = new ArrayList<>();
                            ArrayList<Double> compatibleSellPrices = new ArrayList<>();

                            for(int entry: compatibleEntries){
                                compatibleBuyPrices.add(buyPrices.get(entry));
                                compatibleSellPrices.add(sellPrices.get(entry));
                            }

                            Collections.sort(compatibleBuyPrices);
                            Collections.sort(compatibleSellPrices);

                            float cutoff = settings.getFloat("outlierCutoffRange",(float)0.15);
                            int lowerCutoff = (int) (compatibleEntries.size()  * cutoff);
                            int higherCutoff = compatibleEntries.size() - lowerCutoff;

                            double newAverageBuyPrice = 0;
                            double newAverageSellPrice = 0;
                            for(int i = lowerCutoff; i < higherCutoff; ++i) {
                                newAverageBuyPrice += compatibleBuyPrices.get(i);
                                newAverageSellPrice += compatibleSellPrices.get(i);
                            }
                            newAverageBuyPrice /= higherCutoff - lowerCutoff;
                            newAverageSellPrice /= higherCutoff - lowerCutoff;

                            averageBuyPrice = newAverageBuyPrice;
                            averageSellPrice = newAverageSellPrice;
                        }

                        //regionSend a notification if the threshold is met
                        if(Math.abs(averageBuyPrice - currentBuyPrice) / averageBuyPrice > notificationThreshold &&
                            Math.abs(averageBuyPrice - currentBuyPrice) > notificationThresholdCoins) {
                            //Check if the price is above or below

                            String movementType;
                            double priceChange = Round(-100 * (averageBuyPrice - currentBuyPrice) / averageBuyPrice,1);
                            if (averageBuyPrice - currentBuyPrice < 0) {
                                movementType = "risen!";
                            } else {
                                movementType = "dropped!";
                            }
                            String notificationTitle = new FixBadNamesImproved().fix(itemName) + " buy price has " + movementType;
                            String notificationContent = priceChange + "% | Was: " + addCommasAdjusted(Round(averageBuyPrice,1)) +
                                    " // Now: " + addCommasAdjusted(Round(currentBuyPrice,1));
                            ++notificationsSent;
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                    .setSmallIcon(R.drawable.app_logo)
                                    .setContentTitle(notificationTitle)
                                    .setContentText(notificationContent)
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    // Set the intent that will fire when the user taps the notification
                                    .setContentIntent(pendingIntent)
                                    .setAutoCancel(true);
                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(PriceHistoryMenuActivity.this);
                            notificationManager.notify(notificationsSent, builder.build());
                        }

                        if(Math.abs(averageSellPrice - currentSellPrice) / averageSellPrice > notificationThreshold &&
                            Math.abs(averageSellPrice - currentSellPrice) > notificationThresholdCoins) {
                            //Check if the price is above or below

                            String movementType;
                            double priceChange = Round(-100 * (averageSellPrice - currentSellPrice) / averageSellPrice,1);
                            if (averageSellPrice - currentSellPrice < 0) {
                                movementType = "risen!";
                            } else {
                                movementType = "dropped!";
                            }
                            String notificationTitle = new FixBadNamesImproved().fix(itemName) + " sell price has " + movementType;
                            String notificationContent = priceChange + "% | Was: " + addCommasAdjusted(Round(averageSellPrice,1)) + " //  Now: " +
                                    addCommasAdjusted(Round(currentSellPrice,1));
                            ++notificationsSent;
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                    .setSmallIcon(R.drawable.app_logo)
                                    .setContentTitle(notificationTitle)
                                    .setContentText(notificationContent)
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    // Set the intent that will fire when the user taps the notification
                                    .setContentIntent(pendingIntent)
                                    .setAutoCancel(true);
                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(PriceHistoryMenuActivity.this);
                            notificationManager.notify(notificationsSent, builder.build());
                        }

                        SharedPreferences.Editor editor = settings.edit();
                        editor.putInt("numberOfNotificationsSent",notificationsSent);
                        editor.commit();

                        //endregion
                    }
                }

                //Let the list updating thread know we're done here
                dataRetrievalFinished = true;
                Log.d("UpdatedList","runnable done");
                //endregion
            }
        };
        //endregion

        //regionButton that updates the data now
        btnNewDataNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (patienceCounter > 0){
                    //Prevent the user from spamming the button and getting duplicate results / crashing Hypixel
                    String waitMessage = "Slow down! Wait " + patienceCounter + " more seconds.";
                    Toast.makeText(getApplicationContext(),waitMessage,Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(),"Retrieving data",Toast.LENGTH_SHORT).show();
                    patienceCounter = 60;
                    new Thread(){
                        @Override
                        public void run() {
                            super.run();

                            dataRetrievalFinished = false;

                            RetrieveAndStoreData.run();

                            //region Redo the lists
                                boolean keepRunning = true;
                                while(keepRunning) {
                                    //Only update the view once new data is retrieved
                                    if (dataRetrievalFinished) {
                                        Log.d("UpdatedList","started");
                                        keepRunning = false;

                                        //regionLoad old data from database
                                        bazaarPriceHistory = Room.databaseBuilder(getApplicationContext(),
                                                AppDatabase.class, "BazaarPriceHistoryDB")
                                                .build();
                                        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                                        List<BazaarItem> allItems = bazaarPriceHistory.BazaarDao().getAllItems();
                                        final ArrayList<String> itemNameList = new ArrayList<>();
                                        ArrayList<Double> buyPriceList = new ArrayList<>();
                                        ArrayList<Double> sellPriceList = new ArrayList<>();
                                        ArrayList<Double> avgBuyPriceList = new ArrayList<>();
                                        ArrayList<Double> avgSellPriceList = new ArrayList<>();
                                        for (BazaarItem currentItem : allItems) {
                                            //Add the item name to the list
                                            itemNameList.add(currentItem.getItemName());

                                            //To store in database, everything was in strings, convert them back to their original types
                                            ArrayList<String> buyPricesString = currentItem.getBuyPrices();
                                            ArrayList<String> sellPricesString = currentItem.getSellPrices();
                                            ArrayList<String> timesRetrievedString = currentItem.getTimesRetrieved();

                                            ArrayList<Double> buyPrices = new ArrayList<>();
                                            ArrayList<Double> sellPrices = new ArrayList<>();
                                            ArrayList<Long> timesRetrieved = new ArrayList<>();
                                            for (int i = 0; i < timesRetrievedString.size(); ++i) {
                                                buyPrices.add(Double.parseDouble(buyPricesString.get(i)));
                                                sellPrices.add(Double.parseDouble(sellPricesString.get(i)));
                                                timesRetrieved.add(Long.parseLong(timesRetrievedString.get(i)));
                                            }

                                            long maxTimeBack = settings.getLong("calculationTimeHistory", 1000 * 60 * 60 * 24 * 7); //One week default
                                            ArrayList<Integer> compatibleEntries = new ArrayList<>();
                                            //Go backwards through the timesRetrieved Arraylist, and see which ones are within the calculation interval
                                            for (int i = timesRetrieved.size() - 2; i >= 0; --i) {
                                                if (System.currentTimeMillis() - timesRetrieved.get(i) < maxTimeBack) {
                                                    compatibleEntries.add(i);
                                                }
                                            }

                                            //Get the current buy and sell prices
                                            double currentBuyPrice = buyPrices.get(timesRetrieved.size() - 1);
                                            double currentSellPrice = sellPrices.get(timesRetrieved.size() - 1);

                                            //Store them for later
                                            buyPriceList.add(currentBuyPrice);
                                            sellPriceList.add(currentSellPrice);

                                            //Calculate historical average price, for comparison
                                            double averageBuyPrice = 0;
                                            double averageSellPrice = 0;
                                            for (int i : compatibleEntries) {
                                                averageBuyPrice += buyPrices.get(i);
                                                averageSellPrice += sellPrices.get(i);
                                            }

                                            try {
                                                averageBuyPrice /= compatibleEntries.size();
                                                averageSellPrice /= compatibleEntries.size();
                                            } catch (Exception e) {
                                                averageBuyPrice = currentBuyPrice;
                                                averageSellPrice = currentSellPrice;
                                            }

                                            //Outlier filtration
                                            if (compatibleEntries.size() > 10) {
                                                //Sort by price, then remove the tops and bottoms of the set
                                                ArrayList<Double> compatibleBuyPrices = new ArrayList<>();
                                                ArrayList<Double> compatibleSellPrices = new ArrayList<>();

                                                for(int entry: compatibleEntries){
                                                    compatibleBuyPrices.add(buyPrices.get(entry));
                                                    compatibleSellPrices.add(sellPrices.get(entry));
                                                }

                                                Collections.sort(compatibleBuyPrices);
                                                Collections.sort(compatibleSellPrices);

                                                float cutoff = settings.getFloat("outlierCutoffRange",(float)0.15);
                                                int lowerCutoff = (int) (compatibleEntries.size()  * cutoff);
                                                int higherCutoff = compatibleEntries.size() - lowerCutoff;

                                                double newAverageBuyPrice = 0;
                                                double newAverageSellPrice = 0;
                                                for(int i = lowerCutoff; i < higherCutoff; ++i) {
                                                    newAverageBuyPrice += compatibleBuyPrices.get(i);
                                                    newAverageSellPrice += compatibleSellPrices.get(i);
                                                }
                                                newAverageBuyPrice /= higherCutoff - lowerCutoff;
                                                newAverageSellPrice /= higherCutoff - lowerCutoff;

                                                averageBuyPrice = newAverageBuyPrice;
                                                averageSellPrice = newAverageSellPrice;
                                            }

                                            //Store them for later
                                            avgBuyPriceList.add(averageBuyPrice);
                                            avgSellPriceList.add(averageSellPrice);
                                        }
                                        //endregion

                                        //regionSort the lists by volatility (average buy price change + average sell price change)
                                        Map<Double, String> volatilityMeasure = new HashMap<>();
                                        for (int i = 0; i < itemNameList.size(); ++i) {
                                            double currentBuyPrice = buyPriceList.get(i);
                                            double currentSellPrice = sellPriceList.get(i);
                                            double avgBuyPrice = avgBuyPriceList.get(i);
                                            double avgSellPrice = avgSellPriceList.get(i);

                                            double buyVolatility = (currentBuyPrice - avgBuyPrice) / avgBuyPrice;
                                            double sellVolatility = (currentSellPrice - avgSellPrice) / avgSellPrice;
                                            //This will not affect the ranking, but ensures that every single key is unique, otherwise
                                            //if by sheer coincidence, two prices both change by exactly 10%, its good to be sure
                                            double uniqueGuarantee = (double) (new Random().nextInt(100)) / 100000000;
                                            //Add to the list the volatility and the name of the item, so they get sorted together
                                            volatilityMeasure.put(buyVolatility + sellVolatility + uniqueGuarantee, itemNameList.get(i));
                                        }

                                        //why write your own sorting algorithm when treemap has one?
                                        Map<Double, String> volatilityMeasureSorted = new TreeMap<>(volatilityMeasure);

                                        //Store a sorted collection of strings containing all the necessary information to put in the list
                                        final ArrayList<String> displayData = new ArrayList<>();

                                        //Keep an ArrayList of the order, which might be useful later.
                                        final ArrayList<Integer> storeOrder = new ArrayList<>();

                                        //Add list of search terms
                                        searchPlainNames = new ArrayList<>();

                                        //This loop will run the previously sorted data
                                        for (double volatility : volatilityMeasureSorted.keySet()) {
                                            String productName = volatilityMeasureSorted.get(volatility);
                                            searchPlainNames.add(productName);
                                            //All the previous lists are sorted the same way, so just use the position and we can get all the previous data needed
                                            int position = itemNameList.indexOf(productName);
                                            storeOrder.add(position);
                                            double currentBuyPrice = buyPriceList.get(position);
                                            double currentSellPrice = sellPriceList.get(position);
                                            double avgBuyPrice = avgBuyPriceList.get(position);
                                            double avgSellPrice = avgSellPriceList.get(position);
                                            double buyVolatility = (currentBuyPrice - avgBuyPrice) / avgBuyPrice;
                                            double sellVolatility = (currentSellPrice - avgSellPrice) / avgSellPrice;

                                            //Put a + sign in front of positive percentages
                                            String positiveBuy = "";
                                            if (buyVolatility > 0) {
                                                positiveBuy = "+";
                                            }
                                            String positiveSell = "";
                                            if (sellVolatility > 0) {
                                                positiveSell = "+";
                                            }

                                            //Get the final string to display, and add it to the list
                                            String display = new FixBadNamesImproved().fix(productName) + ":\n" +
                                                    "   Buy price: " + positiveBuy + Round(buyVolatility * 100,1) +
                                                    "%\n     Was: " + addCommasAdjusted(Round(avgBuyPrice,1)) + "\n     Now: "
                                                    + addCommasAdjusted(Round(currentBuyPrice,1)) +
                                                    "\n   Sell price: " + positiveSell + Round(sellVolatility * 100,1) +
                                                    "%\n     Was: " + addCommasAdjusted(Round(avgSellPrice,1)) + "\n     Now: "
                                                    + addCommasAdjusted(Round(currentSellPrice,1));
                                            displayData.add(display);
                                        }
                                        //endregion

                                        //Sort according to user preference
                                        if (!dropFirst) {
                                            Collections.reverse(displayData);
                                            Collections.reverse(storeOrder);
                                        }

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ArrayAdapter<String> ListAdapter = new ArrayAdapter<>(
                                                        PriceHistoryMenuActivity.this,
                                                        android.R.layout.simple_list_item_1,
                                                        displayData
                                                );
                                                listPriceChanges.setAdapter(ListAdapter);
                                                //Make each one clickable so you can view expanded graphs
                                                listPriceChanges.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                    @Override
                                                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                                        final Intent viewGraphs = new Intent(getApplicationContext(), PriceHistoryOfItemActivity.class);
                                                        //The extra is the item name, which I get from itemNameList. As for which one to get, that's
                                                        //determined by storeOrder.
                                                        viewGraphs.putExtra("itemNameAPI",itemNameList.get(storeOrder.get(i)));
                                                        startActivity(viewGraphs);
                                                    }
                                                });
                                                Toast.makeText(getApplicationContext(),"List updated",Toast.LENGTH_SHORT).show();
                                                Log.d("UpdatedList","yeah");
                                            }
                                        });

                                        //Store just the strings to allow for faster searching
                                        searchThis = new ArrayList<>(displayData);
                                        searchPlainNames = new ArrayList<>();
                                        for(int i = 0; i < storeOrder.size(); ++i){
                                            searchPlainNames.add(itemNameList.get(storeOrder.get(i)));
                                        }

                                    } else {
                                        try {
                                            Thread.sleep(10);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            //endregion
                        }
                    }.start();
                }
            }
        });
        //endregion

        //regionThread that ticks down the impatience timer
        new Thread(){
            @Override
            public void run() {
                super.run();
                while(true) {
                    if (patienceCounter == 0) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        patienceCounter -= 1;
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.start();
        //endregion

    }

    public double Round(double input,int scale) {
        try {
            if (scale > 30) {
                scale = 30;
            }
            BigDecimal bd = BigDecimal.valueOf(input);
            bd = bd.setScale(scale, RoundingMode.HALF_UP);
            return bd.doubleValue();
        } catch (Exception e) {
            return 0;
        }
    }

    //Add commas method, adjusted to work with decimal places at the end
    public String addCommasAdjusted(double number) {
        String digits = Double.toString(number);
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