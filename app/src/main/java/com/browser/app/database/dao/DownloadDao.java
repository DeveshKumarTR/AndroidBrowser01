package com.browser.app.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.browser.app.database.entities.DownloadItem;

import java.util.List;

@Dao
public interface DownloadDao {
    
    @Query("SELECT * FROM downloads ORDER BY timestamp DESC")
    List<DownloadItem> getAllDownloads();
    
    @Query("SELECT * FROM downloads WHERE id = :id LIMIT 1")
    DownloadItem getDownloadById(int id);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DownloadItem downloadItem);
    
    @Update
    void update(DownloadItem downloadItem);
    
    @Delete
    void delete(DownloadItem downloadItem);
    
    @Query("DELETE FROM downloads")
    void deleteAll();
    
    @Query("SELECT COUNT(*) FROM downloads")
    int getDownloadCount();
    
    @Query("SELECT * FROM downloads WHERE isCompleted = 1 ORDER BY timestamp DESC")
    List<DownloadItem> getCompletedDownloads();
    
    @Query("SELECT * FROM downloads WHERE isCompleted = 0 ORDER BY timestamp DESC")
    List<DownloadItem> getPendingDownloads();
}
