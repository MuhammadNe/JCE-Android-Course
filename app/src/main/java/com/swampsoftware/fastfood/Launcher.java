package com.swampsoftware.fastfood;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;


public class Launcher extends FragmentActivity {

    private DatabaseHandler db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        // Build database
        //db = new DatabaseHandler(this);

        //volleyGetHttp("getRestaurants");

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    /*
    *
    *  Getting all restaurants data from the server
    *  Function will attempt to get all the data, if it succeeds then the Restaurants table will be cleared, and new data will be set,
    *  if not then the user will get the old data from his local databae ~SQLITE~
    *
     */
    public void volleyGetHttp(String urlQuery) {

        // Tokens for creating basic authentication with BACKAND
        final String MASTER_TOKEN = "6ca20768-1155-4a04-a5d9-481c1a28d079";
        final String USER_TOKEN = "d1a59639-4209-11e6-a39f-0ed7053426cb";

        // DIALOG
        final ProgressDialog dialog = ProgressDialog.show(Launcher.this, "",
                "Loading. Please wait...", true);
        //BACKAND URL
        String url = "https://api.backand.com/1/query/data/" + urlQuery;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {

                        try {
                            // Parse the result ' JSONArray ' into objects and get the keys,
                            // Build the response using Restaurant Object and save it in the database on SUCCESS!!

                            // EMPTY TABLE THEN REBUILD IT
                            db.emptyTable();

                            for(int i = 0; i < response.length(); i++) {

                                JSONObject innerJsonObject = response.getJSONObject(i);
                                RestaurantObject restaurantObject = new RestaurantObject();
                                restaurantObject.setName(innerJsonObject.getString("name"));
                                restaurantObject.setType(innerJsonObject.getString("type"));
                                restaurantObject.setLat(innerJsonObject.getString("lat"));
                                restaurantObject.setLng(innerJsonObject.getString("lng"));
                                restaurantObject.setRate(innerJsonObject.getString("rate"));
                                restaurantObject.setTelNumber(innerJsonObject.getString("telNumber"));
                                restaurantObject.setOpenTime(innerJsonObject.getString("openTime"));

                                db.addRestaurant(restaurantObject);

                            }
                            Log.d("RESTAURANTS RESPONSE", response.toString());

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error Fetching Data", Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();

                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Connection Error", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
        )
            // Custom headers :
            // Adding ' Authorization, Basic TOKEN ' to the http request
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                String encodedCredentials = Base64.encodeToString((MASTER_TOKEN + ":" + USER_TOKEN).getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + encodedCredentials);
                return headers;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonArrayRequest);

    }


}

