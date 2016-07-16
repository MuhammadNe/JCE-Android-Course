package com.swampsoftware.fastfood;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by Muhammad on 7/9/2016.
 */
public class ArrayHelper {


    Context context;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    public ArrayHelper(Context context) {
        this.context = context;
        // prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs = context.getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * Converts the provided ArrayList<String>
     * into a JSONArray and saves it as a single
     * string in the apps shared preferences
     *
     *  String key Preference key for SharedPreferences
     *  array  ArrayList<String> containing the list items
     */
    public void saveArray(String key, ArrayList<String> array) {
        JSONArray jArray = new JSONArray(array);
        editor.remove(key);
        editor.putString(key, jArray.toString());
        editor.commit();
    }

    public void saveMobileNumberID(String key, String mobileNumberID) {
        editor.putString(key, mobileNumberID);
        editor.commit();
    }

    public void saveControlPanelStatus(String key, String status) {
        editor.putString(key, status);
        editor.commit();
    }

    /**
     * Loads a JSONArray from shared preferences
     * and converts it to an ArrayList<String>
     *
     *  String key Preference key for SharedPreferences
     *  ArrayList<String> containing the saved values from the JSONArray
     */
    public ArrayList<String> getArray(String key) {
        ArrayList<String> array = new ArrayList<String>();
        String jArrayString = prefs.getString(key, "NOPREFSAVED");
        if (jArrayString.matches("NOPREFSAVED")) return getDefaultArray();
        else {
            try {
                JSONArray jArray = new JSONArray(jArrayString);
                for (int i = 0; i < jArray.length(); i++) {
                    array.add(jArray.getString(i));
                }
                return array;
            } catch (JSONException e) {
                return getDefaultArray();
            }
        }
    }

    public String getMobileNumberID(String key) {
        String mobileNumberID;
        mobileNumberID = prefs.getString(key, "NOPREFSAVED");
        if(mobileNumberID.matches("NOPREFSAVED")) {
            return getDefaultmobileNumberID();
        }
        return mobileNumberID;
    }

    public String getControlPanelStatus(String key) {
        String status;
        status = prefs.getString(key, "NOPREFSAVED");
        if(status.matches("NOPREFSAVED")) {
            return getDefaulStatus();
        }
        return status;
    }

    // Get a default array in the event that there is no array
    // saved or a JSONException occurred
    private ArrayList<String> getDefaultArray() {
        ArrayList<String> array = new ArrayList<String>();
        array.add("NoData");
        array.add("NoData");
        array.add("NoData");

        return array;
    }

    private String getDefaultmobileNumberID() {
        return "blankNumber";
    }

    private String getDefaulStatus() {
        return "blank";
    }

    public void setFilePath(String filePath) {


    }
}
