package com.browser.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.webkit.URLUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class BrowserUtils {
    
    private static final String PREFS_NAME = "BrowserPrefs";
    private static final String PREF_HOMEPAGE = "homepage";
    private static final String PREF_SEARCH_ENGINE = "search_engine";
    private static final String PREF_USER_AGENT = "user_agent";
    private static final String PREF_JAVASCRIPT_ENABLED = "javascript_enabled";
    private static final String PREF_COOKIES_ENABLED = "cookies_enabled";
    
    private static final String DEFAULT_HOMEPAGE = "https://www.google.com";
    private static final String DEFAULT_SEARCH_ENGINE = "https://www.google.com/search?q=";
    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Mobile Safari/537.36";
    
    private static final Pattern URL_PATTERN = Pattern.compile(
        "^(https?://)?(([\\w\\d\\-_]+\\.)+[a-zA-Z]{2,})(:[0-9]+)?(/.*)?$"
    );
    
    public static boolean isValidUrl(String url) {
        if (TextUtils.isEmpty(url)) return false;
        return URLUtil.isValidUrl(url) || URL_PATTERN.matcher(url).matches();
    }
    
    public static String formatUrl(String url) {
        if (TextUtils.isEmpty(url)) return "";
        
        url = url.trim();
        
        if (isValidUrl(url)) {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                return "https://" + url;
            }
            return url;
        }
        
        // If not a valid URL, create a search query
        return getSearchEngine() + url.replace(" ", "+");
    }
    
    public static String getDomainFromUrl(String url) {
        if (TextUtils.isEmpty(url)) return "";
        
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }
            
            java.net.URL urlObj = new java.net.URL(url);
            return urlObj.getHost();
        } catch (Exception e) {
            return "";
        }
    }
    
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format(Locale.getDefault(), "%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
    
    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    
    // Preferences methods
    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public static String getHomepage(Context context) {
        return getPreferences(context).getString(PREF_HOMEPAGE, DEFAULT_HOMEPAGE);
    }
    
    public static void setHomepage(Context context, String homepage) {
        getPreferences(context).edit().putString(PREF_HOMEPAGE, homepage).apply();
    }
    
    public static String getSearchEngine() {
        return DEFAULT_SEARCH_ENGINE;
    }
    
    public static String getUserAgent() {
        return DEFAULT_USER_AGENT;
    }
    
    public static boolean isJavaScriptEnabled(Context context) {
        return getPreferences(context).getBoolean(PREF_JAVASCRIPT_ENABLED, true);
    }
    
    public static void setJavaScriptEnabled(Context context, boolean enabled) {
        getPreferences(context).edit().putBoolean(PREF_JAVASCRIPT_ENABLED, enabled).apply();
    }
    
    public static boolean areCookiesEnabled(Context context) {
        return getPreferences(context).getBoolean(PREF_COOKIES_ENABLED, true);
    }
    
    public static void setCookiesEnabled(Context context, boolean enabled) {
        getPreferences(context).edit().putBoolean(PREF_COOKIES_ENABLED, enabled).apply();
    }
    
    public static String extractTitle(String html) {
        if (TextUtils.isEmpty(html)) return "";
        
        try {
            Pattern pattern = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher matcher = pattern.matcher(html);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (Exception e) {
            // Ignore
        }
        
        return "";
    }
}
