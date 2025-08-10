package com.keremkayacan.loggiestyle;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    ArrayList<Item> items;
    ItemsAdapter mAdapter;
//    private AdView mAdView;

    private static final int REQUEST_STORAGE_PERMISSION = 101;
    private static final int REQUEST_IMPORT_FILE = 1002;
    private static final int REQUEST_EXPORT_FILE = 1003;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {

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

//        MobileAds.initialize(getApplicationContext());
//        mAdView = (AdView) findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);

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
        //requestStoragePermissions();
        enableEdgeToEdge();
    }

    private void enableEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    private void requestStoragePermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        } else {
            Toast.makeText(this, "All permissions are already granted", Toast.LENGTH_LONG).show();
        }
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
        if (requestCode == REQUEST_IMPORT_FILE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            readJsonFile(uri);
        }
        if (requestCode == REQUEST_EXPORT_FILE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            writeJsonToFile(uri);
        }
    }

    private String convertItemsToJson() {
        JSONArray jsonArray = new JSONArray();
        try {
            for (Item item : mAdapter.getItems()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", item.getId());
                jsonObject.put("time", item.getTime());
                jsonObject.put("title", item.getTitle());
                jsonObject.put("text", item.getText());
                jsonArray.put(jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray.toString();
    }


    private void writeJsonToFile(Uri uri) {
        String jsonContent = convertItemsToJson();
        try {
            ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "w");
            FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
            fileOutputStream.write(jsonContent.getBytes());
            fileOutputStream.close();
            pfd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void readJsonFile(Uri uri) {
        ArrayList<Item> items = new ArrayList<>();
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            inputStream.close();
            String jsonString = stringBuilder.toString();

            // JSON veriyi doğrudan JSONArray olarak işle
            JSONArray itemsArray = new JSONArray(jsonString);

            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject itemObj = itemsArray.getJSONObject(i);
                String id = itemObj.getString("id");
                String time = itemObj.getString("time");
                String title = itemObj.getString("title");
                String text = itemObj.getString("text");
                items.add(new Item(id, time, title, text));
            }

            if (!items.isEmpty() && mAdapter != null) {
                runOnUiThread(() -> mAdapter.updateItems(items));
            }
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Error loading JSON", Toast.LENGTH_SHORT).show());
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
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivityForResult(intent, 2);
            return true;
        }
        if (id == R.id.action_import_json) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            startActivityForResult(intent, REQUEST_IMPORT_FILE);
            return true;
        }
        if (id == R.id.action_export_json) {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            intent.putExtra(Intent.EXTRA_TITLE, "backup.json");
            startActivityForResult(intent, REQUEST_EXPORT_FILE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
