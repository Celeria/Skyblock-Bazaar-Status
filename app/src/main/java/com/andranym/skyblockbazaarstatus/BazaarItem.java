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

    public ArrayList<String> getBuyMovingWeek() {
        return buyMovingWeek;
    }

    public ArrayList<String> getSellMovingWeek() {
        return sellMovingWeek;
    }

    public ArrayList<String> getBuyVolume() {
        return buyVolume;
    }

    public ArrayList<String> getSellVolume() {
        return sellVolume;
    }

    public BazaarItem(String itemName, ArrayList<String> buyPrices, ArrayList<String> sellPrices,
                      ArrayList<String> timesRetrieved, ArrayList<String> buyMovingWeek, ArrayList<String> sellMovingWeek,
                      ArrayList<String> buyVolume, ArrayList<String> sellVolume){
        this.itemName = itemName;
        this.buyPrices = buyPrices;
        this.sellPrices = sellPrices;
        this.timesRetrieved = timesRetrieved;
        this.buyMovingWeek = buyMovingWeek;
        this.sellMovingWeek = sellMovingWeek;
        this.buyVolume = buyVolume;
        this.sellVolume = sellVolume;
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

    @ColumnInfo
    public ArrayList<String> buyMovingWeek;

    @ColumnInfo
    public ArrayList<String> sellMovingWeek;

    @ColumnInfo
    public ArrayList<String> buyVolume;

    @ColumnInfo
    public ArrayList<String> sellVolume;
}
