package com.browser.app;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.browser.app.database.BrowserDatabase;
import com.browser.app.database.entities.DownloadItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadsActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private TextView emptyView;
    private BrowserDatabase database;
    private ExecutorService executor;
    private List<DownloadItem> downloadItems;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads);
        
        setupToolbar();
        initializeViews();
        setupRecyclerView();
        
        database = BrowserDatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();
        downloadItems = new ArrayList<>();
        
        loadDownloads();
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.downloads);
        }
    }
    
    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.emptyView);
    }
    
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // For simplicity, we'll just show empty state
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        emptyView.setText("Downloads will appear here");
    }
    
    private void loadDownloads() {
        executor.execute(() -> {
            List<DownloadItem> loadedDownloads = database.downloadDao().getAllDownloads();
            runOnUiThread(() -> {
                downloadItems.clear();
                downloadItems.addAll(loadedDownloads);
                
                if (downloadItems.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                    emptyView.setText("No downloads yet");
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                    // Here you would set up the adapter
                }
            });
        });
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}
