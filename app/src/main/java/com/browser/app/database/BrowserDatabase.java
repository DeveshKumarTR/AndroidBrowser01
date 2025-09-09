package com.browser.app.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.browser.app.database.dao.BookmarkDao;
import com.browser.app.database.dao.DownloadDao;
import com.browser.app.database.dao.HistoryDao;
import com.browser.app.database.entities.Bookmark;
import com.browser.app.database.entities.DownloadItem;
import com.browser.app.database.entities.HistoryItem;

@Database(
    entities = {Bookmark.class, HistoryItem.class, DownloadItem.class},
    version = 1,
    exportSchema = false
)
public abstract class BrowserDatabase extends RoomDatabase {
    
    private static final String DATABASE_NAME = "browser_database";
    private static BrowserDatabase instance;
    
    public static synchronized BrowserDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                context.getApplicationContext(),
                BrowserDatabase.class,
                DATABASE_NAME
            )
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build();
        }
        return instance;
    }
    
    public abstract BookmarkDao bookmarkDao();
    public abstract HistoryDao historyDao();
    public abstract DownloadDao downloadDao();
}
