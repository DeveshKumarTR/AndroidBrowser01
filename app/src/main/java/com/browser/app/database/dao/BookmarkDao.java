package com.browser.app.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.browser.app.database.entities.Bookmark;

import java.util.List;

@Dao
public interface BookmarkDao {
    
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    List<Bookmark> getAllBookmarks();
    
    @Query("SELECT * FROM bookmarks WHERE url = :url LIMIT 1")
    Bookmark getBookmarkByUrl(String url);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Bookmark bookmark);
    
    @Update
    void update(Bookmark bookmark);
    
    @Delete
    void delete(Bookmark bookmark);
    
    @Query("DELETE FROM bookmarks WHERE url = :url")
    void deleteByUrl(String url);
    
    @Query("DELETE FROM bookmarks")
    void deleteAll();
    
    @Query("SELECT COUNT(*) FROM bookmarks")
    int getBookmarkCount();
}
