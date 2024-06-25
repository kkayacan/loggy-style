package com.keremkayacan.loggiestyle;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by kerem.kayacan on 15.05.2017.
 */

public class Item {

    private String mId;
    private String mTime;
    private String mTitle;
    private String mText;
    private String mCreateTime;
    private String mUpdateTime;


    public Item(String id, String time, String title, String text) {
        mId = id;
        mTime = time;
        mTitle = title;
        mText = text;
        mCreateTime = time;
        mUpdateTime = time;
    }

    public String getId() {
        return mId;
    }
    public String getTime() { return mTime; }
    public String getTitle() { return mTitle; }
    public String getText() { return mText; }
    public String getCreateTime() { return mCreateTime; }
    public String getUpdateTime() { return mUpdateTime; }

    public void setTitle(String title) { mTitle = title; }
    public void setText(String text) { mText = text; }

    private static int lastItemId = 0;

    public static ArrayList<Item> createItemList(Context context) throws JSONException {
        ArrayList<Item> items = new ArrayList<Item>();

        JSONObject jsonContent = null;
        JSONArray itemsArray = null;
        StringBuilder jsonContentBuilder = null;

        File[] yearDirs = context.getFilesDir().listFiles();
        for (int i = yearDirs.length - 1; i>=0; i--) {
            File yearDir = yearDirs[i];
            if (yearDir.isDirectory()) {
                File[] monthDirs = yearDir.listFiles();
                for (int j = monthDirs.length - 1; j>=0; j--) {
                    File monthDir = monthDirs[j];
                    if (monthDir.isDirectory()) {
                        File[] dayFiles = monthDir.listFiles();
                        for (int k = dayFiles.length - 1; k>=0; k--) {
                            File dayFile = dayFiles[k];
                            if (!dayFile.isDirectory()) {
                                jsonContent = null;
                                itemsArray = null;
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
                                for (int m = 0; m < itemsArray.length(); m++){
                                    JSONObject item = itemsArray.getJSONObject(m);
                                    items.add(new Item(item.getString("id"),
                                            item.getString("time"),
                                            item.getString("title"),
                                            item.getString("text")));
                                }
                            }
                        }
                    }
                }
            }
        }

        return items;
    }
}