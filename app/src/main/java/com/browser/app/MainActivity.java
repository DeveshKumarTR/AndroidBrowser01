package com.browser.app;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.browser.app.database.BrowserDatabase;
import com.browser.app.database.entities.Bookmark;
import com.browser.app.database.entities.HistoryItem;
import com.browser.app.utils.BrowserUtils;

import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final String DEFAULT_URL = "https://www.google.com";
    
    private WebView webView;
    private EditText urlEditText;
    private ImageButton backButton, forwardButton, refreshButton, homeButton, bookmarkButton;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView pageTitle;
    
    private BrowserDatabase database;
    private ExecutorService executor;
    private boolean isBookmarked = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initializeViews();
        setupToolbar();
        setupWebView();
        setupControls();
        
        database = BrowserDatabase.getInstance(this);
        executor = Executors.newFixedThreadPool(2);
        
        // Check for URL intent
        Intent intent = getIntent();
        String url = intent.getDataString();
        if (url != null) {
            loadUrl(url);
        } else {
            loadUrl(DEFAULT_URL);
        }
        
        requestPermissions();
    }
    
    private void initializeViews() {
        webView = findViewById(R.id.webView);
        urlEditText = findViewById(R.id.urlEditText);
        backButton = findViewById(R.id.backButton);
        forwardButton = findViewById(R.id.forwardButton);
        refreshButton = findViewById(R.id.refreshButton);
        homeButton = findViewById(R.id.homeButton);
        bookmarkButton = findViewById(R.id.bookmarkButton);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        pageTitle = findViewById(R.id.pageTitle);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }
    
    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        
        // Enable JavaScript
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        
        // Enable DOM storage
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        
        // Enable local storage
        webSettings.setAppCacheEnabled(true);
        webSettings.setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
        
        // Enable geolocation
        webSettings.setGeolocationEnabled(true);
        
        // Enable zoom controls
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        
        // Enable multiple windows
        webSettings.setSupportMultipleWindows(true);
        
        // Set user agent
        webSettings.setUserAgentString(BrowserUtils.getUserAgent());
        
        // Enable mixed content
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        
        // Enable file access
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        
        // Enable cookies
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);
        
        // Set cache mode
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        
        setupWebViewClient();
        setupWebChromeClient();
        setupDownloadListener();
    }
    
    private void setupWebViewClient() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.startsWith("mailto:") || url.startsWith("tel:") || url.startsWith("sms:")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                return false;
            }
            
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                urlEditText.setText(url);
                progressBar.setVisibility(View.VISIBLE);
                updateNavigationButtons();
                checkBookmarkStatus(url);
            }
            
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                updateNavigationButtons();
                
                // Save to history
                saveToHistory(view.getTitle(), url);
            }
            
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
    
    private void setupWebChromeClient() {
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                }
            }
            
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                pageTitle.setText(title);
            }
            
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                runOnUiThread(() -> request.grant(request.getResources()));
            }
            
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });
    }
    
    private void setupDownloadListener() {
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, 
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                    return;
                }
                
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimeType);
                
                String fileName = URLUtil.guessFileName(url, contentDisposition, mimeType);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setAllowedOverMetered(true);
                request.setAllowedOverRoaming(true);
                
                DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                downloadManager.enqueue(request);
                
                Toast.makeText(MainActivity.this, "Download started: " + fileName, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void setupControls() {
        urlEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO || 
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String url = urlEditText.getText().toString().trim();
                loadUrl(url);
                return true;
            }
            return false;
        });
        
        backButton.setOnClickListener(v -> {
            if (webView.canGoBack()) {
                webView.goBack();
            }
        });
        
        forwardButton.setOnClickListener(v -> {
            if (webView.canGoForward()) {
                webView.goForward();
            }
        });
        
        refreshButton.setOnClickListener(v -> webView.reload());
        
        homeButton.setOnClickListener(v -> loadUrl(DEFAULT_URL));
        
        bookmarkButton.setOnClickListener(v -> toggleBookmark());
        
        swipeRefreshLayout.setOnRefreshListener(() -> webView.reload());
    }
    
    private void loadUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        
        // If not a URL, search with Google
        if (!url.startsWith("http://") && !url.startsWith("https://") && !url.contains(".")) {
            url = "https://www.google.com/search?q=" + URLEncoder.encode(url);
        } else if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        
        webView.loadUrl(url);
    }
    
    private void updateNavigationButtons() {
        backButton.setEnabled(webView.canGoBack());
        forwardButton.setEnabled(webView.canGoForward());
        
        backButton.setAlpha(webView.canGoBack() ? 1.0f : 0.5f);
        forwardButton.setAlpha(webView.canGoForward() ? 1.0f : 0.5f);
    }
    
    private void checkBookmarkStatus(String url) {
        executor.execute(() -> {
            Bookmark bookmark = database.bookmarkDao().getBookmarkByUrl(url);
            runOnUiThread(() -> {
                isBookmarked = bookmark != null;
                bookmarkButton.setImageResource(isBookmarked ? 
                    R.drawable.ic_bookmark_filled : R.drawable.ic_bookmark_border);
            });
        });
    }
    
    private void toggleBookmark() {
        String url = webView.getUrl();
        String title = webView.getTitle();
        
        if (url == null) return;
        
        executor.execute(() -> {
            if (isBookmarked) {
                database.bookmarkDao().deleteByUrl(url);
                runOnUiThread(() -> {
                    isBookmarked = false;
                    bookmarkButton.setImageResource(R.drawable.ic_bookmark_border);
                    Toast.makeText(this, "Bookmark removed", Toast.LENGTH_SHORT).show();
                });
            } else {
                Bookmark bookmark = new Bookmark();
                bookmark.title = title != null ? title : url;
                bookmark.url = url;
                bookmark.timestamp = System.currentTimeMillis();
                database.bookmarkDao().insert(bookmark);
                
                runOnUiThread(() -> {
                    isBookmarked = true;
                    bookmarkButton.setImageResource(R.drawable.ic_bookmark_filled);
                    Toast.makeText(this, "Bookmark added", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void saveToHistory(String title, String url) {
        if (url == null || url.equals("about:blank")) return;
        
        executor.execute(() -> {
            HistoryItem historyItem = new HistoryItem();
            historyItem.title = title != null ? title : url;
            historyItem.url = url;
            historyItem.timestamp = System.currentTimeMillis();
            database.historyDao().insert(historyItem);
        });
    }
    
    private void requestPermissions() {
        String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION
        };
        
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.menu_bookmarks) {
            startActivity(new Intent(this, BookmarksActivity.class));
            return true;
        } else if (id == R.id.menu_history) {
            startActivity(new Intent(this, HistoryActivity.class));
            return true;
        } else if (id == R.id.menu_downloads) {
            startActivity(new Intent(this, DownloadsActivity.class));
            return true;
        } else if (id == R.id.menu_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.menu_new_tab) {
            // For simplicity, just load home page
            loadUrl(DEFAULT_URL);
            return true;
        } else if (id == R.id.menu_share) {
            shareCurrentPage();
            return true;
        } else if (id == R.id.menu_find_in_page) {
            showFindInPageDialog();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void shareCurrentPage() {
        String url = webView.getUrl();
        String title = webView.getTitle();
        
        if (url != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, url);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
            startActivity(Intent.createChooser(shareIntent, "Share page"));
        }
    }
    
    private void showFindInPageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Find in page");
        
        EditText input = new EditText(this);
        builder.setView(input);
        
        builder.setPositiveButton("Find", (dialog, which) -> {
            String searchText = input.getText().toString();
            if (!TextUtils.isEmpty(searchText)) {
                webView.findAllAsync(searchText);
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}
