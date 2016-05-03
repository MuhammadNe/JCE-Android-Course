package ex1.jce.com.jce_ex2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Muhammad on 4/29/2016.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "LOCATION_DATABASE";

    // Contacts table name
    private static final String TABLE_LOCATION = "location";

    // Contacts Table Columns names
    private static final String KEY_ID = "_id";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LNG = "lng";
    private static final String KEY_TIME = "time";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_LOCATION + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_LAT + " TEXT,"
                + KEY_LNG + " TEXT,"
                + KEY_TIME + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION);

        // Create tables again
        onCreate(db);
    }

    // Adding new location
    public void addLocation(LocationData location) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LAT, location.getLatitude()); // Add lat
        values.put(KEY_LNG, location.getLongitude()); // Add lng
        values.put(KEY_TIME, location.getTimeStamp()); // Add time stamp

        // Inserting Row
        db.insert(TABLE_LOCATION, null, values);
        db.close(); // Closing database connection
    }


    // Getting All locations
    public Cursor getAllLocations() {

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_LOCATION;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // return contact list
        return cursor;
    }

    public Cursor getLocation(int position) {

        String selectQuery = "SELECT * FROM " + TABLE_LOCATION
                + " WHERE " + KEY_ID + " = " + position;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        return cursor;
    }

    public Cursor searchLocation(String lat, String lng, String time) {


        String searchQuery = "SELECT * FROM " + TABLE_LOCATION + " WHERE ";
        if (!lat.isEmpty()) {
            searchQuery += KEY_LAT + " LIKE '" + lat + "%' AND ";
        }
        if (!lng.isEmpty()) {
            searchQuery += KEY_LNG + " LIKE '" + lng + "%' AND ";
        }
        if (!time.isEmpty()) {
            searchQuery += KEY_TIME + " LIKE '" + time + "%';";
        }
        if (searchQuery.endsWith("AND ")) {
            searchQuery = searchQuery.substring(0, searchQuery.length() - 4);
        }

/*
        String searchQuery = new String();
        if (!lat.isEmpty() && lng.isEmpty() && time.isEmpty()) {
            searchQuery = "SELECT * FROM + " + TABLE_LOCATION
                    + " WHERE " + KEY_LAT + " LIKE " + lat + "%";
        }
        if (lat.isEmpty() && !lng.isEmpty() && time.isEmpty()) {
            searchQuery = "SELECT * FROM + " + TABLE_LOCATION
                    + " WHERE " + KEY_LNG + " LIKE " + lng + "%";
        }
        if (lat.isEmpty() && lng.isEmpty() && !time.isEmpty()) {
            searchQuery = "SELECT * FROM + " + TABLE_LOCATION
                    + " WHERE " + KEY_TIME + " LIKE " + time + "%";
        }
        if (!lat.isEmpty() && !lng.isEmpty() && time.isEmpty()) {
            searchQuery = "SELECT * FROM + " + TABLE_LOCATION
                    + " WHERE " + KEY_LAT + " LIKE " + lat + "% "
                    + " AND " + KEY_LNG + " LIKE " + lng + "%";
        }
        if (!lat.isEmpty() && lng.isEmpty() && !time.isEmpty()) {
            searchQuery = "SELECT * FROM + " + TABLE_LOCATION
                    + " WHERE " + KEY_LAT + " LIKE " + lat + "% "
                    + " AND " + KEY_TIME + " LIKE " + time + "%";
        }
        if (!lat.isEmpty() && !lng.isEmpty() && !time.isEmpty()) {
            searchQuery = "SELECT * FROM + " + TABLE_LOCATION
                    + " WHERE " + KEY_LAT + " = " + lat
                    + " AND " + KEY_LNG + " LIKE " + lng + "%"
                    + " AND " + KEY_TIME + " LIKE " + time + "%";
        }
*/
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(searchQuery, null);
        return cursor;

    }

    public void emptyTable() {

        //String deleteQuery = "DELETE FROM " + TABLE_LOCATION + ";";
        String deleteQuery = "DROP TABLE " + TABLE_LOCATION + ";";
        getWritableDatabase().execSQL(deleteQuery);
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_LOCATION + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_LAT + " TEXT,"
                + KEY_LNG + " TEXT,"
                + KEY_TIME + " TEXT" + ")";
        getWritableDatabase().execSQL(CREATE_CONTACTS_TABLE);
    }
}
