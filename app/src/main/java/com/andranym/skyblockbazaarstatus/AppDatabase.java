package com.andranym.skyblockbazaarstatus;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {BazaarItem.class}, version = 2)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract BazaarDao BazaarDao();
}

