package com.andranym.skyblockbazaarstatus;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

public class PriceHistoryOfItemActivity extends AppCompatActivity {

    static AppDatabase bazaarPriceHistory;
    LineChart chartBuyPrice;
    LineChart chartSellPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_history_of_item);

        //Get information about what we are displaying
        final String itemToView = getIntent().getStringExtra("itemNameAPI");
        //Set the title so the user knows
        setTitle(new FixBadNamesImproved().fix(itemToView));

        //Initialize the charts
        chartBuyPrice = findViewById(R.id.chartBuyPrice);
        chartSellPrice = findViewById(R.id.chartSellPrice);

        // create marker to display box when values are selected
        MyMarkerView mv = new MyMarkerView(PriceHistoryOfItemActivity.this,R.layout.markerview);

        // Set the marker to the chart
        mv.setChartView(chartBuyPrice);
        chartBuyPrice.setMarker(mv);
        chartSellPrice.setMarker(mv);

        //Enable touch gestures on graph
        chartBuyPrice.setTouchEnabled(true);
        chartSellPrice.setTouchEnabled(true);

        chartBuyPrice.setDragEnabled(true);
        chartBuyPrice.setScaleEnabled(true);
        chartSellPrice.setDragEnabled(true);
        chartSellPrice.setScaleEnabled(true);

        //Hide x labels, since they aren't helpful
        chartBuyPrice.getXAxis().setDrawLabels(false);
        chartSellPrice.getXAxis().setDrawLabels(false);

        //Set background color
        chartBuyPrice.setBackgroundColor(Color.LTGRAY);
        chartSellPrice.setBackgroundColor(Color.LTGRAY);

        chartBuyPrice.getDescription().setEnabled(false);
        chartSellPrice.getDescription().setEnabled(false);

        //regionLoad old data from database
        new Thread() {
            @Override
            public void run() {
                super.run();
                bazaarPriceHistory = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "BazaarPriceHistoryDB")
                        .build();
                BazaarItem desiredItem = bazaarPriceHistory.BazaarDao().getAnAuctionItem(itemToView);

                //Load up the information about that item from the database, convert into usable form
                ArrayList<String> buyPricesString = desiredItem.getBuyPrices();
                ArrayList<String> sellPricesString = desiredItem.getSellPrices();
                ArrayList<String> timesRetrievedString = desiredItem.getTimesRetrieved();

                ArrayList<Float> buyPrices = new ArrayList<>();
                ArrayList<Float> sellPrices = new ArrayList<>();
                ArrayList<Long> timesRetrieved = new ArrayList<>();

                for(int i = 0; i < timesRetrievedString.size(); ++i){
                    buyPrices.add(Float.parseFloat(buyPricesString.get(i)));
                    sellPrices.add(Float.parseFloat(sellPricesString.get(i)));
                    timesRetrieved.add(Long.parseLong(timesRetrievedString.get(i)));
                }

                //This is for the actual demand, this might be different in size, so its done here
                ArrayList<String> buyVolumeString = desiredItem.getBuyVolume();
                ArrayList<String> sellVolumeString = desiredItem.getSellVolume();

                ArrayList<Float> buyVolume = new ArrayList<>();
                ArrayList<Float> sellVolume = new ArrayList<>();
                for(int i = 0; i < buyVolumeString.size(); ++i) {
                    buyVolume.add(Float.parseFloat(buyVolumeString.get(i)));
                    sellVolume.add(Float.parseFloat(sellVolumeString.get(i)));
                }

                ArrayList<Float> buyVolumeDifference = new ArrayList<>();
                ArrayList<Float> sellVolumeDifference = new ArrayList<>();

                //Might be a size difference if you are using an older dataset
                int sizeDifference = timesRetrieved.size() - buyVolume.size();

                for(int i = 0; i < buyVolume.size(); ++ i){
                    buyVolumeDifference.add(buyVolume.get(i));
                    sellVolumeDifference.add(sellVolume.get(i));
                }

//                //Make it based on difference from previous time
//                for(int i = 1; i < buyVolume.size(); ++ i){
//                    buyVolumeDifference.add((buyVolume.get(i) - buyVolume.get(i-1)) / (timesRetrieved.get(i + sizeDifference) - timesRetrieved.get(i + sizeDifference - 1)));
//                    sellVolumeDifference.add((sellVolume.get(i) - sellVolume.get(i-1)) / (timesRetrieved.get(i + sizeDifference) - timesRetrieved.get(i + sizeDifference - 1)));
//                }
//
//                //Have something in the first slot just in case
//                buyVolumeDifference.add(0,buyVolumeDifference.get(0));
//                sellVolumeDifference.add(0,sellVolumeDifference.get(0));

                ArrayList<Entry> demandValues = new ArrayList<>();
                ArrayList<Entry> supplyValues = new ArrayList<>();

                //Create a scaled version so that the user can see what's going on
                float maxChangeBuy = buyVolumeDifference.get(0);
                float maxChangeSell = sellVolumeDifference.get(0);

                for(int i = 1; i < buyVolumeDifference.size(); ++i){
                    float currentChange =  buyVolumeDifference.get(i);
                    if (currentChange > maxChangeBuy) {
                        maxChangeBuy = currentChange;
                    }

                    currentChange = sellVolumeDifference.get(i);
                    if(currentChange > maxChangeSell){
                        maxChangeSell = currentChange;
                    }
                }

                float minPriceBuy = buyPrices.get(0);
                float minPriceSell = sellPrices.get(0);

                for(int i = 1; i < sellVolume.size(); ++i){
                    float currentPrice = buyPrices.get(i);
                    if(currentPrice < minPriceBuy) {
                        minPriceBuy = currentPrice;
                    }

                    currentPrice = sellPrices.get(i);
                    if(currentPrice < minPriceSell) {
                        minPriceSell = currentPrice;
                    }
                }

                float buyScalingFactor = minPriceBuy / maxChangeBuy;
                float sellScalingFactor = minPriceSell / maxChangeSell;

                ArrayList<Float> buyVolumeDifferenceScaled = new ArrayList<>();
                ArrayList<Float> sellVolumeDifferenceScaled = new ArrayList<>();

                for(int i = 0; i < buyVolumeDifference.size(); ++i){
                    buyVolumeDifferenceScaled.add(buyScalingFactor * buyVolumeDifference.get(i));
                    sellVolumeDifferenceScaled.add(sellScalingFactor * sellVolumeDifference.get(i));
                }

                for (int i = 0; i < buyVolume.size(); ++i) {
                    long time = timesRetrieved.get(i + sizeDifference);
                    float volume = buyVolumeDifferenceScaled.get(i);
                    //Add the buy price
                    demandValues.add(new Entry(time, volume));
                    //Add the sell price
                    volume = sellVolumeDifferenceScaled.get(i);
                    supplyValues.add(new Entry(time, volume));
                }


                ArrayList<Entry> buyValues = new ArrayList<>();
                ArrayList<Entry> sellValues = new ArrayList<>();

                for (int i = 0; i < timesRetrieved.size(); ++i) {
                    long time = timesRetrieved.get(i);
                    float price = buyPrices.get(i);
                    //Add the buy price
                    buyValues.add(new Entry(time, price));
                    //Add the sell price
                    price = sellPrices.get(i);
                    sellValues.add(new Entry(time,price));
                }

                LineDataSet buySet;
                LineDataSet sellSet;

                LineDataSet buyVolumeSet;
                LineDataSet sellVolumeSet;

                // create a dataset and give it a type
                buySet = new LineDataSet(buyValues,"Buy Price");
                sellSet = new LineDataSet(sellValues,"Sell Price");

                buyVolumeSet = new LineDataSet(demandValues,"Instant Buy Amount");
                sellVolumeSet = new LineDataSet(supplyValues,"Instant Sell Amount");

                buySet.setDrawIcons(false);
                sellSet.setDrawIcons(false);

                buyVolumeSet.setDrawIcons(false);
                sellVolumeSet.setDrawIcons(false);

                // black lines and points
                buySet.setColor(Color.BLUE);
                buySet.setCircleColor(Color.GRAY);

                sellSet.setColor(Color.RED);
                sellSet.setCircleColor(Color.GRAY);

                buyVolumeSet.setColor(Color.BLACK);
                sellVolumeSet.setColor(Color.BLACK);

                //For the buy and sell volume information, remove the circles and the labels
                buyVolumeSet.setDrawCircles(false);
                sellVolumeSet.setDrawCircles(false);

                buyVolumeSet.setDrawValues(false);
                sellVolumeSet.setDrawValues(false);

                // line thickness and point size
                buySet.setLineWidth(1f);
                buySet.setCircleRadius(3f);

                sellSet.setLineWidth(1f);
                sellSet.setCircleRadius(3f);

                sellVolumeSet.setLineWidth(1f);
                buyVolumeSet.setLineWidth(1f);

                // draw points as solid circles
                buySet.setDrawCircleHole(false);
                sellSet.setDrawCircleHole(false);

                // text size of values
                buySet.setValueTextSize(9f);
                sellSet.setValueTextSize(9f);

                ArrayList<ILineDataSet> buyDataSets = new ArrayList<>();
                buyDataSets.add(buySet); // add the data sets
                buyDataSets.add(buyVolumeSet);

                ArrayList<ILineDataSet> sellDataSets = new ArrayList<>();
                sellDataSets.add(sellSet); // add the data sets
                sellDataSets.add(sellVolumeSet);

                // create a data object with the data sets
                LineData data1 = new LineData(buyDataSets);
                LineData data2 = new LineData(sellDataSets);

                // set data
                chartBuyPrice.setData(data1);

                chartSellPrice.setData(data2);

            }
        }.start();
        //endregion
    }
}