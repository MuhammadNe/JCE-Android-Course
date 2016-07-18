package com.swampsoftware.fastfood;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by Muhammad on 7/14/2016.
 */
public class RestaurantCustomAdapter extends CursorAdapter {

    private LayoutInflater inflater;
    private UserCurrentLocationObject userCurrentLocationObject;
    private DatabaseHandler db;

    public RestaurantCustomAdapter(Context context, Cursor c) {
        super(context, c);
        userCurrentLocationObject = new UserCurrentLocationObject();
        inflater = LayoutInflater.from(context);
        db = new DatabaseHandler(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.restaurant_customview, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView TVresName = (TextView) view.findViewById(R.id.TVresName);
        TextView TVrate = (TextView) view.findViewById(R.id.TVrate);
        TextView TVdistance = (TextView) view.findViewById(R.id.TVdistance);

        //Log.d("CURSOR name", cursor.getColumnIndex("_name") + "");
        //Log.d("CURSOR 2", cursor.getString(2));
        //Log.d("CURSOR 3", cursor.getString(3));
        //Log.d("CUSTOM ADAPTER", "IN");

        int nameIndex = cursor.getColumnIndex("_name");
        int rateIndex = cursor.getColumnIndex("_rate");
        int latIndex = cursor.getColumnIndex("_lat");
        int lngIndex = cursor.getColumnIndex("_lng");
        int idIndex = cursor.getColumnIndex("_id");

        float floatDistance;
        String stringDistance;

        if (userCurrentLocationObject.getLat() >= 0 && userCurrentLocationObject.getLng() >= 0) {


            Double lat = Double.parseDouble(cursor.getString(latIndex));
            Double lng = Double.parseDouble(cursor.getString(lngIndex));

            Double currentLat = userCurrentLocationObject.getLat();
            Double currentLng = userCurrentLocationObject.getLng();

            Location currentLocation = new Location("currentLocation");
            Location restaurantLocation = new Location("restaurantLocation");

            currentLocation.setLatitude(currentLat);
            currentLocation.setLongitude(currentLng);

            restaurantLocation.setLatitude(lat);
            restaurantLocation.setLongitude(lng);

            stringDistance = (currentLocation.distanceTo(restaurantLocation)/1000) + "";
            // keep only 2 digits after the .
            int dot_occurrence = stringDistance.indexOf(".");
            if ((stringDistance.length() - 1) - stringDistance.indexOf(".") > 3) {
                stringDistance = stringDistance.substring(0, stringDistance.indexOf(".") + 2);
            }
            Log.d("Distance", stringDistance);

            // Save distance inside the database for each row
            String id = cursor.getString(idIndex);
            db.updateDisance(id, Double.parseDouble(stringDistance));
        } else {
            stringDistance = "";
        }


        TVresName.setText(cursor.getString(nameIndex));
        TVrate.setText(cursor.getString(rateIndex));
        TVdistance.setText(stringDistance + " KM");
    }
}
