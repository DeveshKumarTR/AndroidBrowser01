package com.browser.app;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.browser.app.utils.BrowserUtils;

public class SettingsActivity extends AppCompatActivity {
    
    private Switch javascriptSwitch;
    private Switch cookiesSwitch;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        setupToolbar();
        initializeViews();
        loadSettings();
        setupListeners();
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.settings);
        }
    }
    
    private void initializeViews() {
        javascriptSwitch = findViewById(R.id.javascriptSwitch);
        cookiesSwitch = findViewById(R.id.cookiesSwitch);
    }
    
    private void loadSettings() {
        javascriptSwitch.setChecked(BrowserUtils.isJavaScriptEnabled(this));
        cookiesSwitch.setChecked(BrowserUtils.areCookiesEnabled(this));
    }
    
    private void setupListeners() {
        javascriptSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BrowserUtils.setJavaScriptEnabled(this, isChecked);
            Toast.makeText(this, isChecked ? "JavaScript enabled" : "JavaScript disabled", 
                Toast.LENGTH_SHORT).show();
        });
        
        cookiesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BrowserUtils.setCookiesEnabled(this, isChecked);
            Toast.makeText(this, isChecked ? "Cookies enabled" : "Cookies disabled", 
                Toast.LENGTH_SHORT).show();
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
}
