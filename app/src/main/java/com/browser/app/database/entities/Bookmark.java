package com.browser.app.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "bookmarks")
public class Bookmark {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String title;
    public String url;
    public long timestamp;
    public String favicon;
    
    public Bookmark() {}
    
    public Bookmark(String title, String url, long timestamp) {
        this.title = title;
        this.url = url;
        this.timestamp = timestamp;
    }
}
