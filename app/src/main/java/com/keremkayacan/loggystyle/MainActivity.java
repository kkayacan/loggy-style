package com.keremkayacan.loggystyle;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    ArrayList<Item> items;
    ItemsAdapter mAdapter;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/

                Calendar calendar = Calendar.getInstance();

                File yearDir = new File(getFilesDir(), Integer.toString(calendar.get(Calendar.YEAR)));
                if (!yearDir.exists()) {
                    yearDir.mkdir();
                }

                File monthDir = new File(yearDir, AppUtil.getMonthDirName(calendar.get(Calendar.MONTH)));
                if (!monthDir.exists()) {
                    monthDir.mkdir();
                }

                JSONObject jsonContent = null;
                JSONArray itemsArray = null;
                StringBuilder jsonContentBuilder = null;

                File dayFile = new File(monthDir, AppUtil.getDayFileName(calendar.get(Calendar.DAY_OF_MONTH)));
                if (dayFile.exists()) {
                    //Read the file and convert to string
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(dayFile);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader bufferedReader = new BufferedReader(isr);
                    jsonContentBuilder = new StringBuilder();
                    String jsonContentString = null;
                    try {
                        while ((jsonContentString = bufferedReader.readLine()) != null) {
                            jsonContentBuilder.append(jsonContentString);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //Convert string to JSON object
                    try {
                        jsonContent = new JSONObject(jsonContentBuilder.toString());
                        itemsArray = jsonContent.getJSONArray("items");

                    } catch (JSONException je) {
                        je.printStackTrace();
                    }

                }

                if (jsonContent == null) {
                    //File not found -> create new JSON object
                    jsonContent = new JSONObject();
                    itemsArray = new JSONArray();
                    try {
                        jsonContent.put("items", itemsArray);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                //Create new item and put into JSON
                JSONObject itemContent = new JSONObject();
                try {
                    itemContent.put("id", UUID.randomUUID().toString());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                    itemContent.put("date", dateFormat.format(calendar.getTime()));
                    SimpleDateFormat timeFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                    itemContent.put("time", timeFormat.format(calendar.getTime()));
                    itemContent.put("title", "");
                    itemContent.put("text", "");
                    itemsArray.put(itemContent);
                    JSONArray sortedArray = AppUtil.sortJson(itemsArray, "time");
                    jsonContent.remove("items");
                    jsonContent.put("items", sortedArray);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //write file
                try {
                    FileOutputStream out = new FileOutputStream(dayFile);
                    out.write(jsonContent.toString().getBytes());
                    out.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Add item to adapter and notify recyclerview
                try {
                    items.add(0, new Item(itemContent.getString("id"),
                            itemContent.getString("time"),
                            itemContent.getString("title"),
                            itemContent.getString("text")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mAdapter.notifyItemInserted(0);
                RecyclerView rvItems = (RecyclerView) findViewById(R.id.rvItems);
                rvItems.smoothScrollToPosition(0);

            }
        });

        // Lookup the recyclerview in activity layout
        RecyclerView rvItems = (RecyclerView) findViewById(R.id.rvItems);

        // Initialize items
        try {
            items = Item.createItemList(getApplicationContext());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Create adapter passing in the sample user data
        mAdapter = new ItemsAdapter(this, items);
        // Attach the adapter to the recyclerview to populate items
        rvItems.setAdapter(mAdapter);
        // Set layout manager to position the items
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        // That's all!

        MobileAds.initialize(getApplicationContext(), AppUtil.ADMOB_APP_ID);
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        rvItems.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        Intent intent = new Intent(getApplicationContext(), EditActivity.class);
                        intent.putExtra("position", Integer.toString(position));
                        intent.putExtra("id", mAdapter.getItem(position).getId());
                        intent.putExtra("createTime", mAdapter.getItem(position).getCreateTime());
                        intent.putExtra("updateTime", mAdapter.getItem(position).getUpdateTime());
                        intent.putExtra("time", mAdapter.getItem(position).getTime());
                        intent.putExtra("title", mAdapter.getItem(position).getTitle());
                        intent.putExtra("text", mAdapter.getItem(position).getText());
                        startActivityForResult(intent, 1);
                    }
                })
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String posString = (String) data.getExtras().get("position");
            Integer position = Integer.parseInt(posString);
            mAdapter.getItem(position).setTitle((String) data.getExtras().get("title"));
            mAdapter.getItem(position).setText((String) data.getExtras().get("text"));
            mAdapter.notifyItemChanged(position);
        } else if (requestCode == 1 && resultCode == EditActivity.RESULT_DELETED) {
            String posString = (String) data.getExtras().get("position");
            Integer position = Integer.parseInt(posString);
            mAdapter.remove(position);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivityForResult(intent, 2);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
