package com.browser.app.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "history")
public class HistoryItem {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String title;
    public String url;
    public long timestamp;
    public String favicon;
    public int visitCount;
    
    public HistoryItem() {
        this.visitCount = 1;
    }
    
    public HistoryItem(String title, String url, long timestamp) {
        this.title = title;
        this.url = url;
        this.timestamp = timestamp;
        this.visitCount = 1;
    }
}
