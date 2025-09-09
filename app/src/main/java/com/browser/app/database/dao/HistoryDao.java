package com.browser.app.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.browser.app.database.entities.HistoryItem;

import java.util.List;

@Dao
public interface HistoryDao {
    
    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    List<HistoryItem> getAllHistory();
    
    @Query("SELECT * FROM history WHERE url = :url LIMIT 1")
    HistoryItem getHistoryByUrl(String url);
    
    @Query("SELECT * FROM history WHERE title LIKE :query OR url LIKE :query ORDER BY timestamp DESC")
    List<HistoryItem> searchHistory(String query);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HistoryItem historyItem);
    
    @Update
    void update(HistoryItem historyItem);
    
    @Delete
    void delete(HistoryItem historyItem);
    
    @Query("DELETE FROM history WHERE url = :url")
    void deleteByUrl(String url);
    
    @Query("DELETE FROM history")
    void deleteAll();
    
    @Query("DELETE FROM history WHERE timestamp < :timestamp")
    void deleteOlderThan(long timestamp);
    
    @Query("SELECT COUNT(*) FROM history")
    int getHistoryCount();
}
