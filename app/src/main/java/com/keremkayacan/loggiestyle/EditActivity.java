package com.keremkayacan.loggiestyle;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import java.io.InputStreamReader;

public class EditActivity extends AppCompatActivity {

    public static final int RESULT_DELETED = 91;
//    private AdView mAdView;

    private void updateItem(){
        File yearDir = new File(getFilesDir(), getIntent().getStringExtra("time").substring(0,4));
        File monthDir = new File(yearDir, getIntent().getStringExtra("time").substring(4,6));
        File dayFile = new File(monthDir, getIntent().getStringExtra("time").substring(6,8)+".json");
        String itemId = getIntent().getStringExtra("id");

        FileInputStream fis = null;
        JSONObject jsonContent = null;
        JSONArray itemsArray = null;
        StringBuilder jsonContentBuilder = null;

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
            for (int i = 0; i<itemsArray.length(); i++){
                JSONObject itemContent = (JSONObject) itemsArray.get(i);
                if (itemContent.getString("id").equals(getIntent().getStringExtra("id"))){
                    EditText etTitle = (EditText)findViewById(R.id.txtTitle);
                    itemContent.put("title", etTitle.getText().toString());
                    EditText etText = (EditText)findViewById(R.id.txtText);
                    itemContent.put("text", etText.getText().toString());
                    break;
                }
            }

        } catch (JSONException je) {
            je.printStackTrace();
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

    }

    private void deleteItem(){

        File yearDir = new File(getFilesDir(), getIntent().getStringExtra("time").substring(0,4));
        File monthDir = new File(yearDir, getIntent().getStringExtra("time").substring(4,6));
        File dayFile = new File(monthDir, getIntent().getStringExtra("time").substring(6,8)+".json");
        String itemId = getIntent().getStringExtra("id");

        FileInputStream fis = null;
        JSONObject jsonContent = null;
        JSONArray itemsArray = null;
        StringBuilder jsonContentBuilder = null;

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
            for (int i = 0; i<itemsArray.length(); i++){
                JSONObject itemContent = (JSONObject) itemsArray.get(i);
                if (itemContent.getString("id").equals(getIntent().getStringExtra("id"))){
                    itemsArray.remove(i);
                    break;
                }
            }

        } catch (JSONException je) {
            je.printStackTrace();
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

    }

    private void setActionResult(int result){
        Intent returnIntent = new Intent();
        returnIntent.putExtra("position", getIntent().getStringExtra("position"));
        EditText etTitle = (EditText)findViewById(R.id.txtTitle);
        returnIntent.putExtra("title", etTitle.getText().toString());
        EditText etText = (EditText)findViewById(R.id.txtText);
        returnIntent.putExtra("text", etText.getText().toString());
        setResult(result, returnIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();

            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        TextView txtDate = (TextView) findViewById(R.id.txtDate);
        txtDate.setText(AppUtil.getFormattedDate(intent.getStringExtra("time"), getApplicationContext()));
        TextView txtTime = (TextView) findViewById(R.id.txtTime);
        txtTime.setText(AppUtil.getFormattedTime(intent.getStringExtra("time")));
        EditText txtTitle = (EditText) findViewById(R.id.txtTitle);
        txtTitle.setText(intent.getStringExtra("title"));
        EditText txtText = (EditText) findViewById(R.id.txtText);
        txtText.setText(intent.getStringExtra("text"));

//        mAdView = (AdView) findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public void finish() {
        updateItem();
        setActionResult(RESULT_OK);
        super.finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_delete:
                deleteItem();
                setActionResult(RESULT_DELETED);
                super.finish();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

}
