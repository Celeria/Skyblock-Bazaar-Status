package com.andranym.skyblockbazaarstatus;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.andranym.skyblockbazaarstatus.MainActivity.CHANNEL_ID;

public class RetrieveAndStoreDataAndNotify extends Worker {

    public RetrieveAndStoreDataAndNotify(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
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
                .addMigrations(MIGRATION_1_2)
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

                long buyVolume = productInformation.getJSONObject(current_product).getJSONObject("quick_status").getLong("buyVolume");
                long sellVolume = productInformation.getJSONObject(current_product).getJSONObject("quick_status").getLong("sellVolume");
                long buyMovingWeek = productInformation.getJSONObject(current_product).getJSONObject("quick_status").getLong("buyMovingWeek");
                long sellMovingWeek = productInformation.getJSONObject(current_product).getJSONObject("quick_status").getLong("sellMovingWeek");

                BazaarItem previousData = bazaarPriceHistory.BazaarDao().getAnAuctionItem(current_product);
                if(previousData != null){
                    //Retrieve the old data, append the new data on, and then put it back
                    ArrayList<String> storedBuyPrices = previousData.getBuyPrices();
                    storedBuyPrices.add(Double.toString(buyPrice));
                    ArrayList<String> storedSellPrices = previousData.getSellPrices();
                    storedSellPrices.add(Double.toString(sellPrice));
                    ArrayList<String> storedTimesRetrieved = previousData.getTimesRetrieved();
                    storedTimesRetrieved.add(Long.toString(timeRetrieved));
                    try {
                        ArrayList<String> storedBuyVolume = previousData.getBuyVolume();
                        storedBuyVolume.add(Long.toString(buyVolume));
                        ArrayList<String> storedSellVolume = previousData.getSellVolume();
                        storedSellVolume.add(Long.toString(sellVolume));
                        ArrayList<String> storedBuyMovingWeek = previousData.getBuyMovingWeek();
                        storedBuyMovingWeek.add(Long.toString(buyMovingWeek));
                        ArrayList<String> storedSellMovingWeek = previousData.getSellMovingWeek();
                        storedSellMovingWeek.add(Long.toString(sellMovingWeek));

                        BazaarItem updatedEntry = new BazaarItem(current_product,storedBuyPrices,storedSellPrices,storedTimesRetrieved,
                                storedBuyMovingWeek,storedSellMovingWeek,storedBuyVolume,storedSellVolume);
                        bazaarPriceHistory.BazaarDao().insertAll(updatedEntry);
                    } catch (Exception e){
                        //This is here because if the user has a previous version stored, it won't break
                        ArrayList<String> storedBuyVolume = new ArrayList<>();
                        storedBuyVolume.add(Long.toString(buyVolume));
                        ArrayList<String> storedSellVolume = new ArrayList<>();
                        storedSellVolume.add(Long.toString(sellVolume));
                        ArrayList<String> storedBuyMovingWeek = new ArrayList<>();
                        storedBuyMovingWeek.add(Long.toString(buyMovingWeek));
                        ArrayList<String> storedSellMovingWeek = new ArrayList<>();
                        storedSellMovingWeek.add(Long.toString(sellMovingWeek));

                        BazaarItem updatedEntry = new BazaarItem(current_product,storedBuyPrices,storedSellPrices,storedTimesRetrieved,
                                storedBuyMovingWeek,storedSellMovingWeek,storedBuyVolume,storedSellVolume);
                        bazaarPriceHistory.BazaarDao().insertAll(updatedEntry);
                    }
                } else {
                    //Create new arrays, and store those
                    ArrayList<String> storedBuyPrices = new ArrayList<>();
                    storedBuyPrices.add(Double.toString(buyPrice));
                    ArrayList<String> storedSellPrices = new ArrayList<>();
                    storedSellPrices.add(Double.toString(sellPrice));
                    ArrayList<String> storedTimesRetrieved = new ArrayList<>();
                    storedTimesRetrieved.add(Long.toString(timeRetrieved));
                    ArrayList<String> storedBuyVolume = new ArrayList<>();
                    storedBuyVolume.add(Long.toString(buyVolume));
                    ArrayList<String> storedSellVolume = new ArrayList<>();
                    storedSellVolume.add(Long.toString(sellVolume));
                    ArrayList<String> storedBuyMovingWeek = new ArrayList<>();
                    storedBuyMovingWeek.add(Long.toString(buyMovingWeek));
                    ArrayList<String> storedSellMovingWeek = new ArrayList<>();
                    storedSellMovingWeek.add(Long.toString(sellMovingWeek));
                    BazaarItem newEntry = new BazaarItem(current_product,storedBuyPrices,storedSellPrices,storedTimesRetrieved,
                            storedBuyMovingWeek,storedSellMovingWeek,storedBuyVolume,storedSellVolume);
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
            List<BazaarItem> allItems = bazaarPriceHistory.BazaarDao().getAllItems();
            int notificationsSent = settings.getInt("numberOfNotificationsSent",0);
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
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
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
                    android.app.NotificationManager notificationManager = (android.app.NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(notificationsSent, builder.build());
                }

                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("numberOfNotificationsSent",notificationsSent);
                editor.commit();

                //endregion

            }
        }
        return Result.success();
    }

    static AppDatabase bazaarPriceHistory;

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

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE BazaarItem ADD COLUMN buyMovingWeek");
            database.execSQL("ALTER TABLE BazaarItem ADD COLUMN sellMovingWeek");
            database.execSQL("ALTER TABLE BazaarItem ADD COLUMN sellVolume");
            database.execSQL("ALTER TABLE BazaarItem ADD COLUMN buyVolume");
        }
    };
}
