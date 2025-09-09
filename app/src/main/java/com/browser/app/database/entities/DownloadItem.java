package com.browser.app.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "downloads")
public class DownloadItem {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String fileName;
    public String url;
    public String filePath;
    public long timestamp;
    public long fileSize;
    public String mimeType;
    public boolean isCompleted;
    
    public DownloadItem() {}
    
    public DownloadItem(String fileName, String url, String filePath, long timestamp, long fileSize, String mimeType) {
        this.fileName = fileName;
        this.url = url;
        this.filePath = filePath;
        this.timestamp = timestamp;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.isCompleted = false;
    }
}
