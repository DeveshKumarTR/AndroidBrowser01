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

import com.browser.app.adapters.BookmarkAdapter;
import com.browser.app.database.BrowserDatabase;
import com.browser.app.database.entities.Bookmark;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookmarksActivity extends AppCompatActivity implements BookmarkAdapter.OnBookmarkClickListener {
    
    private RecyclerView recyclerView;
    private TextView emptyView;
    private BookmarkAdapter adapter;
    private BrowserDatabase database;
    private ExecutorService executor;
    private List<Bookmark> bookmarks;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);
        
        setupToolbar();
        initializeViews();
        setupRecyclerView();
        
        database = BrowserDatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();
        bookmarks = new ArrayList<>();
        
        loadBookmarks();
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.bookmarks);
        }
    }
    
    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.emptyView);
    }
    
    private void setupRecyclerView() {
        adapter = new BookmarkAdapter(bookmarks, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    
    private void loadBookmarks() {
        executor.execute(() -> {
            List<Bookmark> loadedBookmarks = database.bookmarkDao().getAllBookmarks();
            runOnUiThread(() -> {
                bookmarks.clear();
                bookmarks.addAll(loadedBookmarks);
                adapter.notifyDataSetChanged();
                
                if (bookmarks.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                    emptyView.setText(R.string.no_bookmarks);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                }
            });
        });
    }
    
    @Override
    public void onBookmarkClick(Bookmark bookmark) {
        Intent intent = new Intent();
        intent.putExtra("url", bookmark.url);
        setResult(RESULT_OK, intent);
        finish();
    }
    
    @Override
    public void onBookmarkLongClick(Bookmark bookmark) {
        showBookmarkOptionsDialog(bookmark);
    }
    
    private void showBookmarkOptionsDialog(Bookmark bookmark) {
        String[] options = {"Open", "Edit", "Delete"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(bookmark.title)
               .setItems(options, (dialog, which) -> {
                   switch (which) {
                       case 0: // Open
                           onBookmarkClick(bookmark);
                           break;
                       case 1: // Edit
                           showEditBookmarkDialog(bookmark);
                           break;
                       case 2: // Delete
                           deleteBookmark(bookmark);
                           break;
                   }
               })
               .show();
    }
    
    private void showEditBookmarkDialog(Bookmark bookmark) {
        // For simplicity, we'll just show a basic edit dialog
        // In a full implementation, you'd create a proper dialog with EditTexts
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Bookmark")
               .setMessage("Title: " + bookmark.title + "\nURL: " + bookmark.url)
               .setPositiveButton("OK", null)
               .show();
    }
    
    private void deleteBookmark(Bookmark bookmark) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Bookmark")
               .setMessage("Are you sure you want to delete this bookmark?")
               .setPositiveButton("Delete", (dialog, which) -> {
                   executor.execute(() -> {
                       database.bookmarkDao().delete(bookmark);
                       runOnUiThread(() -> {
                           bookmarks.remove(bookmark);
                           adapter.notifyDataSetChanged();
                           
                           if (bookmarks.isEmpty()) {
                               recyclerView.setVisibility(View.GONE);
                               emptyView.setVisibility(View.VISIBLE);
                               emptyView.setText(R.string.no_bookmarks);
                           }
                           
                           Toast.makeText(this, "Bookmark deleted", Toast.LENGTH_SHORT).show();
                       });
                   });
               })
               .setNegativeButton("Cancel", null)
               .show();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bookmarks_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.menu_clear_all) {
            showClearAllDialog();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void showClearAllDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Clear All Bookmarks")
               .setMessage("Are you sure you want to delete all bookmarks?")
               .setPositiveButton("Clear All", (dialog, which) -> {
                   executor.execute(() -> {
                       database.bookmarkDao().deleteAll();
                       runOnUiThread(() -> {
                           bookmarks.clear();
                           adapter.notifyDataSetChanged();
                           recyclerView.setVisibility(View.GONE);
                           emptyView.setVisibility(View.VISIBLE);
                           emptyView.setText(R.string.no_bookmarks);
                           Toast.makeText(this, "All bookmarks cleared", Toast.LENGTH_SHORT).show();
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
