package com.keremkayacan.loggystyle;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.SyncStateContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by kerem.kayacan on 15.05.2017.
 */

public class AppUtil {

    public static final String ADMOB_APP_ID = Constants.ADMOB_APP_ID;
    public static final String ADMOB_AD_UNIT_ID = Constants.ADMOB_AD_UNIT_ID;

    private static final Pattern PATTERN = Pattern.compile("(\\D*)(\\d*)");
    public static JSONArray sortJson(JSONArray jsonArraylab, String type)
    {
        final String value=type;
        JSONArray sortedJsonArray = new JSONArray();
        List<JSONObject> jsonValues = new ArrayList<>();
        for (int i = 0; i < jsonArraylab.length(); i++) {
            try {
                jsonValues.add(jsonArraylab.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Collections.sort( jsonValues, new Comparator<JSONObject>() {
            //You can change "Name" with "ID" if you want to sort by ID

            private final String KEY_NAME = value;
            @Override
            public int compare(JSONObject a, JSONObject b) {
                String valA = new String();
                String valB = new String();
                try {
                    valA = (String) a.getString(KEY_NAME);
                    valB = (String) b.getString(KEY_NAME);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Matcher m1 = PATTERN.matcher(valA);
                Matcher m2 = PATTERN.matcher(valB);
                // The only way find() could fail is at the end of a string
                while (m1.find() && m2.find()) {
                    // matcher.group(1) fetches any non-digits captured by the
                    // first parentheses in PATTERN.
                    int nonDigitCompare = m1.group(1).compareTo(m2.group(1));
                    if (0 != nonDigitCompare) {
                        return nonDigitCompare;
                    }
                    // matcher.group(2) fetches any digits captured by the
                    // second parentheses in PATTERN.
                    if (m1.group(2).isEmpty()) {
                        return m2.group(2).isEmpty() ? 0 : -1;
                    } else if (m2.group(2).isEmpty()) {
                        return +1;
                    }
                    BigInteger n1 = new BigInteger(m1.group(2));
                    BigInteger n2 = new BigInteger(m2.group(2));
                    int numberCompare = n1.compareTo(n2);
                    if (0 != numberCompare) {
                        return numberCompare;
                    }
                }
                // Handle if one string is a prefix of the other.
                // Nothing comes before something.
                return m1.hitEnd() && m2.hitEnd() ? 0 :m1.hitEnd()? -1 : +1;
            }
        });
        for (int i = ( jsonValues.size() - 1 ); i >= 0; i--) {
            sortedJsonArray.put(jsonValues.get(i));
        }
        return sortedJsonArray;
    }

    public static String getMonthDirName(int month){
        if (month<9){
            return "0" + (month+1);
        } else {
            return "" + (month+1);
        }
    }

    public static String getDayFileName(int day){
        if (day<10){
            return "0" + day + ".json";
        } else {
            return "" + day + ".json";
        }
    }

    public static String getFormattedDate(String time, Context context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String dateFormat = pref.getString("date_format", "");
        switch (dateFormat) {
            case "0":
                return  time.substring(6,8) + "." +
                        time.substring(4,6) + "." +
                        time.substring(0,4);
            case "1":
                return  time.substring(4,6) + "." +
                        time.substring(6,8) + "." +
                        time.substring(0,4);
        }
        return  time.substring(6,8) + "." +
                time.substring(4,6) + "." +
                time.substring(0,4);
    }

    public static String getFormattedTime(String time){
        return  time.substring(8,10)  + ":" +
                time.substring(10,12) + ":" +
                time.substring(12,14);
    }

    public static String getFormattedDateTime(String time, Context context){
        return getFormattedDate(time, context) + " " + getFormattedTime(time);
    }
}
