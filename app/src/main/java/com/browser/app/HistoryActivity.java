package com.browser.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.browser.app.adapters.HistoryAdapter;
import com.browser.app.database.BrowserDatabase;
import com.browser.app.database.entities.HistoryItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryActivity extends AppCompatActivity implements HistoryAdapter.OnHistoryClickListener {
    
    private RecyclerView recyclerView;
    private TextView emptyView;
    private HistoryAdapter adapter;
    private BrowserDatabase database;
    private ExecutorService executor;
    private List<HistoryItem> historyItems;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        
        setupToolbar();
        initializeViews();
        setupRecyclerView();
        
        database = BrowserDatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();
        historyItems = new ArrayList<>();
        
        loadHistory();
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.history);
        }
    }
    
    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.emptyView);
    }
    
    private void setupRecyclerView() {
        adapter = new HistoryAdapter(historyItems, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    
    private void loadHistory() {
        executor.execute(() -> {
            List<HistoryItem> loadedHistory = database.historyDao().getAllHistory();
            runOnUiThread(() -> {
                historyItems.clear();
                historyItems.addAll(loadedHistory);
                adapter.notifyDataSetChanged();
                
                if (historyItems.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                    emptyView.setText(R.string.no_history);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                }
            });
        });
    }
    
    @Override
    public void onHistoryClick(HistoryItem historyItem) {
        Intent intent = new Intent();
        intent.putExtra("url", historyItem.url);
        setResult(RESULT_OK, intent);
        finish();
    }
    
    @Override
    public void onHistoryLongClick(HistoryItem historyItem) {
        showHistoryOptionsDialog(historyItem);
    }
    
    private void showHistoryOptionsDialog(HistoryItem historyItem) {
        String[] options = {"Open", "Delete"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(historyItem.title)
               .setItems(options, (dialog, which) -> {
                   switch (which) {
                       case 0: // Open
                           onHistoryClick(historyItem);
                           break;
                       case 1: // Delete
                           deleteHistoryItem(historyItem);
                           break;
                   }
               })
               .show();
    }
    
    private void deleteHistoryItem(HistoryItem historyItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete History Item")
               .setMessage("Are you sure you want to delete this history item?")
               .setPositiveButton("Delete", (dialog, which) -> {
                   executor.execute(() -> {
                       database.historyDao().delete(historyItem);
                       runOnUiThread(() -> {
                           historyItems.remove(historyItem);
                           adapter.notifyDataSetChanged();
                           
                           if (historyItems.isEmpty()) {
                               recyclerView.setVisibility(View.GONE);
                               emptyView.setVisibility(View.VISIBLE);
                               emptyView.setText(R.string.no_history);
                           }
                           
                           Toast.makeText(this, "History item deleted", Toast.LENGTH_SHORT).show();
                       });
                   });
               })
               .setNegativeButton("Cancel", null)
               .show();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.history_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.menu_clear_history) {
            showClearHistoryDialog();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void showClearHistoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Clear History")
               .setMessage("Are you sure you want to clear all browsing history?")
               .setPositiveButton("Clear All", (dialog, which) -> {
                   executor.execute(() -> {
                       database.historyDao().deleteAll();
                       runOnUiThread(() -> {
                           historyItems.clear();
                           adapter.notifyDataSetChanged();
                           recyclerView.setVisibility(View.GONE);
                           emptyView.setVisibility(View.VISIBLE);
                           emptyView.setText(R.string.no_history);
                           Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show();
                       });
                   });
               })
               .setNegativeButton("Cancel", null)
               .show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}
