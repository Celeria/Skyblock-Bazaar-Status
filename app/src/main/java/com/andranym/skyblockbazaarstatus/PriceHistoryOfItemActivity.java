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

                // create a dataset and give it a type
                buySet = new LineDataSet(buyValues,"Buy Price");
                sellSet = new LineDataSet(sellValues,"Sell Price");

                buySet.setDrawIcons(false);
                sellSet.setDrawIcons(false);

                // black lines and points
                buySet.setColor(Color.BLUE);
                buySet.setCircleColor(Color.BLACK);

                sellSet.setColor(Color.RED);
                sellSet.setCircleColor(Color.BLACK);

                // line thickness and point size
                buySet.setLineWidth(1f);
                buySet.setCircleRadius(3f);

                sellSet.setLineWidth(1f);
                sellSet.setCircleRadius(3f);

                // draw points as solid circles
                buySet.setDrawCircleHole(false);
                sellSet.setDrawCircleHole(false);

                // text size of values
                buySet.setValueTextSize(9f);
                sellSet.setValueTextSize(9f);

                ArrayList<ILineDataSet> buyDataSets = new ArrayList<>();
                buyDataSets.add(buySet); // add the data sets

                ArrayList<ILineDataSet> sellDataSets = new ArrayList<>();
                sellDataSets.add(sellSet); // add the data sets

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