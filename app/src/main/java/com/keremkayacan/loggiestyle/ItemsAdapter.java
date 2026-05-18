package com.keremkayacan.loggiestyle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ViewHolder> {

    private List<Item> mItemsOriginal;
    private List<Item> mItemsFiltered;

    public ItemsAdapter(List<Item> items) {

        this.mItemsOriginal = new ArrayList<>(items != null ? items : new ArrayList<>());
        this.mItemsFiltered = new ArrayList<>(this.mItemsOriginal);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView timeTextView;
        public final TextView titleTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            timeTextView = itemView.findViewById(R.id.time);
            titleTextView = itemView.findViewById(R.id.title);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.log_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = mItemsFiltered.get(position);


        String formattedTime = AppUtil.getFormattedDateTime(item.getTime(), holder.itemView.getContext());
        holder.timeTextView.setText(formattedTime);

        String title = item.getTitle();
        if (title != null && !title.trim().isEmpty()) {
            holder.titleTextView.setText(title);
        } else {
            holder.titleTextView.setText("New item");
        }
    }

    @Override
    public int getItemCount() {
        return mItemsFiltered.size();
    }

    public void filter(String query) {
        mItemsFiltered.clear();
        if (query == null || query.trim().isEmpty()) {
            mItemsFiltered.addAll(mItemsOriginal);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Item item : mItemsOriginal) {
                if (item.getTitle() != null && item.getTitle().toLowerCase().contains(lowerCaseQuery)) {
                    mItemsFiltered.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public Item getItem(int position) {
        return mItemsFiltered.get(position);
    }

    public void remove(int position) {
        Item itemToRemove = mItemsFiltered.get(position);
        mItemsOriginal.remove(itemToRemove);
        mItemsFiltered.remove(position);
        notifyItemRemoved(position);
    }

    public void updateItems(List<Item> newItems) {
        mItemsOriginal.clear();
        mItemsOriginal.addAll(newItems);
        filter("");
    }

    public List<Item> getItems() {
        return mItemsFiltered;
    }
}