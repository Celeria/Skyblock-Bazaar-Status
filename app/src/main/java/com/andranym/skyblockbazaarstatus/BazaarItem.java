package com.andranym.skyblockbazaarstatus;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;

@Entity
public class BazaarItem {

    @NonNull
    public String getItemName() {
        return itemName;
    }

    public ArrayList<String> getBuyPrices() {
        return buyPrices;
    }

    public ArrayList<String> getSellPrices() {
        return sellPrices;
    }

    public ArrayList<String> getTimesRetrieved() {
        return timesRetrieved;
    }

    public BazaarItem(String itemName, ArrayList<String> buyPrices, ArrayList<String> sellPrices, ArrayList<String> timesRetrieved){
        this.itemName = itemName;
        this.buyPrices = buyPrices;
        this.sellPrices = sellPrices;
        this.timesRetrieved = timesRetrieved;
    }

    @NonNull
    @PrimaryKey
    public String itemName;

    @ColumnInfo
    public ArrayList<String> buyPrices;

    @ColumnInfo
    public ArrayList<String> sellPrices;

    @ColumnInfo
    public ArrayList<String> timesRetrieved;
}
