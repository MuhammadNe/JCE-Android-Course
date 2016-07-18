package com.swampsoftware.fastfood;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Muhammad on 7/14/2016.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "RESTAURANT_DATABASE";

    // Contacts table name
    private static final String RESTAURANT_TABLE_NAME = "restaurant";
    private static final String REVIEW_TABLE_NAME = "review";
    private static final String FAVOURITE_TABLE_NAME = "favourite";
    /**
     * Restaurants Table
     */
    // Contacts Table Columns names
    private static final String KEY_ID = "_id";
    private static final String KEY_NAME = "_name";
    private static final String KEY_TYPE = "_type";
    private static final String KEY_LAT = "_lat";
    private static final String KEY_LNG = "_lng";
    private static final String KEY_RATE = "_rate";
    private static final String KEY_TELNum = "_telNum";
    private static final String KEY_TIME = "_time";
    private static final String KEY_FAVOURITE = "_favourite";
    private static final String KEY_DISTANCE = "_distance";

    // Create table restaurant
    private String CREATE_RESTAURANT_TABLE = "CREATE TABLE " + RESTAURANT_TABLE_NAME + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_NAME + " TEXT,"
            + KEY_TYPE + " TEXT,"
            + KEY_LAT + " TEXT,"
            + KEY_LNG + " TEXT,"
            + KEY_RATE + " TEXT,"
            + KEY_TELNum + " TEXT,"
            + KEY_FAVOURITE + " TEXT,"
            + KEY_DISTANCE + " DOUBLE,"
            + KEY_TIME + " TEXT" + ")";

    // Create table restaurant
    private String CREATE_FAVOURITE_TABLE = "CREATE TABLE " + FAVOURITE_TABLE_NAME + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_NAME + " TEXT,"
            + KEY_TYPE + " TEXT,"
            + KEY_LAT + " TEXT,"
            + KEY_LNG + " TEXT,"
            + KEY_RATE + " TEXT,"
            + KEY_TELNum + " TEXT,"
            + KEY_FAVOURITE + " TEXT,"
            + KEY_DISTANCE + " DOUBLE,"
            + KEY_TIME + " TEXT" + ")";

    /**
     * reviews Table
     */
    private static final String KEY_REVIEW = "_review";
    private static final String KEY_USER_NAME = "_name";
    private static final String KEY_USER_RATE = "_rate";

    // Create table review
    private String CREATE_REVIEW_TABLE = "CREATE TABLE " + REVIEW_TABLE_NAME + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_USER_NAME + " TEXT,"
            + KEY_LAT + " TEXT,"
            + KEY_LNG + " TEXT,"
            + KEY_USER_RATE + " TEXT,"
            + KEY_REVIEW + " TEXT" + ")";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // inCreate -> create the table
    @Override
    public void onCreate(SQLiteDatabase db) {
        if (db.isOpen()) {
            Log.d("DB", "OPENED");
        } else {
            Log.e("DB", "CLOSED");
        }
        db.execSQL(CREATE_RESTAURANT_TABLE);
        db.execSQL(CREATE_FAVOURITE_TABLE);
        db.execSQL(CREATE_REVIEW_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + RESTAURANT_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CREATE_FAVOURITE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + REVIEW_TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    // Adding new location
    public void addRestaurant(RestaurantObject restaurantObject) {

        SQLiteDatabase db = this.getWritableDatabase();


        ContentValues values = new ContentValues();
        values.put(KEY_NAME, restaurantObject.getName()); // Add name
        values.put(KEY_TYPE, restaurantObject.getType()); // Add type
        values.put(KEY_LAT, restaurantObject.getLat()); // Add lat
        values.put(KEY_LNG, restaurantObject.getLng()); // Add lng
        values.put(KEY_RATE, restaurantObject.getRate()); // Add rate
        values.put(KEY_TELNum, restaurantObject.getTelNumber()); // Add telephone number
        values.put(KEY_FAVOURITE, "0"); // Add favourite number - No by default
        values.put(KEY_TIME, restaurantObject.getOpenTime()); // Add open times

        Log.d("Values", values.toString());
        // Inserting Row
        db.insert(RESTAURANT_TABLE_NAME, null, values);
        db.close(); // Closing database connection

    }


    // Getting All locations
    public Cursor getAllRestaurants(String filter, String type) {

        String selectQuery;
        if (filter.equals("Rate") && type.equals("Any Type")) {
            // Select All Query order by rate, no type
            selectQuery = "SELECT  * FROM " + RESTAURANT_TABLE_NAME + " ORDER BY " + KEY_RATE + " DESC" + ";";
        } else if (filter.equals("Distance") && type.equals("Any Type")) {
            // Select All Query order by distance, no type
            selectQuery = "SELECT  * FROM " + RESTAURANT_TABLE_NAME + " ORDER BY " + KEY_DISTANCE + " ASC" + ";";
        } else if (filter.equals("Rate") && !type.equals("Any Type")) {
            // Select All Query order by rate, with type
            selectQuery = "SELECT * FROM " + RESTAURANT_TABLE_NAME + " WHERE " + KEY_TYPE + " = '" + type + "' ORDER BY " + KEY_RATE + " DESC" + ";";
        } else if (filter.equals("Distance") && !type.equals("Any Type")) {
            // Select All Query order by distance, with type
            selectQuery = "SELECT * FROM " + RESTAURANT_TABLE_NAME + " WHERE " + KEY_TYPE + " = '" + type + "' ORDER BY " + KEY_DISTANCE + " ASC" + ";";
        } else if (filter.equals("map") && type.equals("map")) {
            selectQuery = "SELECT * FROM " + RESTAURANT_TABLE_NAME + ";";
        } else {
            // else statement select all order by distance
            selectQuery = "SELECT  * FROM " + RESTAURANT_TABLE_NAME + " ORDER BY " + KEY_DISTANCE + " ASC" + ";";
        }


        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();

        // return contact list
        return cursor;
    }

    public void addFavRestaurant(String position) {

        String updateQuery = "UPDATE " + RESTAURANT_TABLE_NAME + " SET " + KEY_FAVOURITE + " = 1 "
                + "WHERE " + KEY_ID + " = " + position;

        SQLiteDatabase db = this.getWritableDatabase();

        String insertQuery = "INSERT INTO " + FAVOURITE_TABLE_NAME
                + " SELECT * FROM " + RESTAURANT_TABLE_NAME
                + " WHERE " + KEY_NAME + " LIKE '" + position + "'"
                + " AND NOT EXISTS (SELECT 1 FROM " + FAVOURITE_TABLE_NAME + " WHERE " + KEY_NAME + " LIKE '" + position + "');";

        db.execSQL(insertQuery);
        /*ContentValues values = new ContentValues();
        values.put(KEY_FAVOURITE, "1"); // update favourite number - No by default

        Log.d("Values", values.toString());
        // Inserting Row
        db.update(RESTAURANT_TABLE_NAME, values, KEY_ID + " = " + position, null);*/
        db.close(); // Closing database connection

    }

    public Cursor getAllFavouriteRestaurants(String filter, String type) {

        String selectQuery;
        if (filter.equals("Rate") && type.equals("Any Type")) {
            // Select All Query order by rate, no type
            selectQuery = "SELECT  * FROM " + FAVOURITE_TABLE_NAME + " ORDER BY " + KEY_RATE + " DESC" + ";";
        } else if (filter.equals("Distance") && type.equals("Any Type")) {
            // Select All Query order by distance, no type
            selectQuery = "SELECT  * FROM " + FAVOURITE_TABLE_NAME + " ORDER BY " + KEY_DISTANCE + " ASC" + ";";
        } else if (filter.equals("Rate") && !type.equals("Any Type")) {
            // Select All Query order by rate, with type
            selectQuery = "SELECT * FROM " + FAVOURITE_TABLE_NAME + " WHERE " + KEY_TYPE + " = '" + type + "' ORDER BY " + KEY_RATE + " DESC" + ";";
        } else if (filter.equals("Distance") && !type.equals("Any Type")) {
            // Select All Query order by distance, with type
            selectQuery = "SELECT * FROM " + FAVOURITE_TABLE_NAME + " WHERE " + KEY_TYPE + " = '" + type + "' ORDER BY " + KEY_DISTANCE + " ASC" + ";";
        } else {
            // else statement select all order by distance
            selectQuery = "SELECT  * FROM " + FAVOURITE_TABLE_NAME + " ORDER BY " + KEY_DISTANCE + " ASC" + ";";
        }

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        // return favourite restaurants list
        return cursor;
    }

    // Getting Restaurant from list
    public Cursor getRestaurant(String position) {

        String selectQuery = "SELECT * FROM " + RESTAURANT_TABLE_NAME
                + " WHERE " + KEY_NAME + " LIKE '" + position + "';";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();

        return cursor;
    }

    public void updateDisance(String id, Double stringDistance) {

        String selectQuery = "UPDATE " + RESTAURANT_TABLE_NAME + " SET " + KEY_DISTANCE + " = " + stringDistance
                + " WHERE " + KEY_ID + " = " + id + ";";

        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL(selectQuery);
        db.close();

    }

    public void addReviews(ReviewObject reviewObject) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_USER_NAME, reviewObject.getName()); // Add name
        values.put(KEY_LAT, reviewObject.getLat()); // Add lat
        values.put(KEY_LNG, reviewObject.getLng()); // Add lng
        values.put(KEY_USER_RATE, reviewObject.getRate()); // Add rate
        values.put(KEY_REVIEW, reviewObject.getReview()); // Add rate

        Log.d("Values", values.toString());
        // Inserting Row
        db.insert(REVIEW_TABLE_NAME, null, values);
        db.close(); // Closing database connection
    }

    // Getting Restaurant from list
    public Cursor getReviews(String lat, String lng) {

        String selectQuery = "SELECT * FROM " + REVIEW_TABLE_NAME
                + " WHERE _lat" + " LIKE '" + lat + "' AND "
                + "_lng LIKE '" + lng + "';";

        //selectQuery = "SELECT * FROM " + REVIEW_TABLE_NAME  + ";";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();

        return cursor;
    }

    /*
            // Search queries for locations
            public Cursor searchLocation(String lat, String lng, String time) {


                String searchQuery = "SELECT * FROM " + TABLE_NAME + " WHERE ";
                if (!lat.isEmpty()) {
                    searchQuery += KEY_LAT + " LIKE '" + lat + "%' AND ";
                }
                if (!lng.isEmpty()) {
                    searchQuery += KEY_LNG + " LIKE '" + lng + "%' AND ";
                }
                if (!time.isEmpty()) {
                    searchQuery += KEY_TIME + " LIKE '%" + time + "%';";
                }
                if (searchQuery.endsWith("AND ")) {
                    searchQuery = searchQuery.substring(0, searchQuery.length() - 4);
                }

                SQLiteDatabase db = this.getWritableDatabase();
                Cursor cursor = db.rawQuery(searchQuery, null);
                return cursor;

            }
        */
    // Empty table
    public void emptyTable() {

        String deleteQuery = "DROP TABLE " + RESTAURANT_TABLE_NAME + ";";
        getWritableDatabase().execSQL(deleteQuery);

        getWritableDatabase().execSQL(CREATE_RESTAURANT_TABLE);
    }

    // Empty table
    public void emptyTable_REVIEW() {

        String deleteQuery = "DROP TABLE " + REVIEW_TABLE_NAME + ";";
        getWritableDatabase().execSQL(deleteQuery);

        getWritableDatabase().execSQL(CREATE_REVIEW_TABLE);
    }


}
