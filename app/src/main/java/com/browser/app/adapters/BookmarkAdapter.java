package com.browser.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.browser.app.R;
import com.browser.app.database.entities.Bookmark;
import com.browser.app.utils.BrowserUtils;

import java.util.List;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder> {
    
    private List<Bookmark> bookmarks;
    private OnBookmarkClickListener listener;
    
    public interface OnBookmarkClickListener {
        void onBookmarkClick(Bookmark bookmark);
        void onBookmarkLongClick(Bookmark bookmark);
    }
    
    public BookmarkAdapter(List<Bookmark> bookmarks, OnBookmarkClickListener listener) {
        this.bookmarks = bookmarks;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public BookmarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bookmark, parent, false);
        return new BookmarkViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull BookmarkViewHolder holder, int position) {
        Bookmark bookmark = bookmarks.get(position);
        
        holder.titleTextView.setText(bookmark.title);
        holder.urlTextView.setText(bookmark.url);
        holder.dateTextView.setText(BrowserUtils.formatDate(bookmark.timestamp));
        
        // Set a default favicon
        holder.faviconImageView.setImageResource(R.drawable.ic_bookmark_filled);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookmarkClick(bookmark);
            }
        });
        
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onBookmarkLongClick(bookmark);
            }
            return true;
        });
    }
    
    @Override
    public int getItemCount() {
        return bookmarks.size();
    }
    
    static class BookmarkViewHolder extends RecyclerView.ViewHolder {
        ImageView faviconImageView;
        TextView titleTextView;
        TextView urlTextView;
        TextView dateTextView;
        
        BookmarkViewHolder(@NonNull View itemView) {
            super(itemView);
            faviconImageView = itemView.findViewById(R.id.faviconImageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            urlTextView = itemView.findViewById(R.id.urlTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }
    }
}
