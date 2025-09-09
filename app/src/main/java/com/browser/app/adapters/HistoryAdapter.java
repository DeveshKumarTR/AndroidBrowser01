package com.browser.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.browser.app.R;
import com.browser.app.database.entities.HistoryItem;
import com.browser.app.utils.BrowserUtils;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    
    private List<HistoryItem> historyItems;
    private OnHistoryClickListener listener;
    
    public interface OnHistoryClickListener {
        void onHistoryClick(HistoryItem historyItem);
        void onHistoryLongClick(HistoryItem historyItem);
    }
    
    public HistoryAdapter(List<HistoryItem> historyItems, OnHistoryClickListener listener) {
        this.historyItems = historyItems;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryItem historyItem = historyItems.get(position);
        
        holder.titleTextView.setText(historyItem.title);
        holder.urlTextView.setText(historyItem.url);
        holder.dateTextView.setText(BrowserUtils.formatDate(historyItem.timestamp));
        
        // Set visit count if greater than 1
        if (historyItem.visitCount > 1) {
            holder.visitCountTextView.setText("Visited " + historyItem.visitCount + " times");
            holder.visitCountTextView.setVisibility(View.VISIBLE);
        } else {
            holder.visitCountTextView.setVisibility(View.GONE);
        }
        
        // Set a default favicon
        holder.faviconImageView.setImageResource(R.drawable.ic_history);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHistoryClick(historyItem);
            }
        });
        
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onHistoryLongClick(historyItem);
            }
            return true;
        });
    }
    
    @Override
    public int getItemCount() {
        return historyItems.size();
    }
    
    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        ImageView faviconImageView;
        TextView titleTextView;
        TextView urlTextView;
        TextView dateTextView;
        TextView visitCountTextView;
        
        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            faviconImageView = itemView.findViewById(R.id.faviconImageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            urlTextView = itemView.findViewById(R.id.urlTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            visitCountTextView = itemView.findViewById(R.id.visitCountTextView);
        }
    }
}
