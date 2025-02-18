package com.keremkayacan.loggiestyle;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Created by kerem.kayacan on 15.05.2017.
 */

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class ItemsAdapter extends
        RecyclerView.Adapter<ItemsAdapter.ViewHolder> {

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView timeTextView;
        public TextView titleTextView;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            timeTextView = (TextView) itemView.findViewById(R.id.time);
            titleTextView = (TextView) itemView.findViewById(R.id.title);
        }


    }


    // Store a member variable for the items
    private List<Item> mItems;
    // Store the context for easy access
    private Context mContext;

    // Pass in the contact array into the constructor
    public ItemsAdapter(Context context, List<Item> items) {
        mItems = items;
        mContext = context;
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return mContext;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public ItemsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View itemView = inflater.inflate(R.layout.log_item, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(itemView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(ItemsAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        Item item = mItems.get(position);

        // Set item views based on your views and data model
        String formattedTime = AppUtil.getFormattedDateTime(item.getTime(), mContext);
        TextView textView = viewHolder.timeTextView;
        textView.setText(formattedTime);
        textView = viewHolder.titleTextView;
        String title = item.getTitle();
        if (!title.equals("")){
            textView.setText(item.getTitle());
        } else {
            textView.setText("New item");
        }

    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public Item getItem(int position){ return mItems.get(position); }

    public void remove(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mItems.size());
    }

    public void updateItems(List<Item> newItems) {
        mItems.clear();
        mItems.addAll(newItems);
        notifyDataSetChanged();
    }

    public List<Item> getItems(){
        return mItems;
    }
}
