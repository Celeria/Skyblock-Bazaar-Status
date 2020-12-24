package com.andranym.skyblockbazaarstatus;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BazaarDao {
    @Query("SELECT * FROM BazaarItem")
    List<BazaarItem> getAllItems();

    @Query("SELECT * FROM BazaarItem where itemName = :itemName")
    BazaarItem getAnAuctionItem(String itemName);

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    void insertAll(BazaarItem...bazaarItems) ;

}