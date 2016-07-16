package com.swampsoftware.fastfood;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RestaurantActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private DatabaseHandler db;
    private Cursor restaurantCursor, cursor;
    private ListView mListView;
    private ReviewCustomAdapter reviewCustomAdapter;
    private TextView TVname, TVrate, TVaddress;

    private String resName;
    private String resLat;
    private String resLng;
    private int resPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        TVname = (TextView) findViewById(R.id.TVresName);
        TVrate = (TextView) findViewById(R.id.TVresRate);
        TVaddress = (TextView) findViewById(R.id.TVresAddress);

        resPosition = getIntent().getIntExtra("restaurant", -1);
        Log.d("resPosition", resPosition + "");
        db = new DatabaseHandler(this);
        mListView = (ListView) findViewById(R.id.listView2);
        restaurantCursor = db.getRestaurant(resPosition);

        int resNameIndex = restaurantCursor.getColumnIndex("_name");
        int latIndex = restaurantCursor.getColumnIndex("_lat");
        int lngIndex = restaurantCursor.getColumnIndex("_lng");

        resName = restaurantCursor.getString(resNameIndex);
        resLat = restaurantCursor.getString(latIndex);
        resLng = restaurantCursor.getString(lngIndex);

        TVname.setText(resName);
        TVaddress.setText(resLat + " / " + resLng);

        cursor = db.getReviews(resLat, resLng);
        reviewCustomAdapter = new ReviewCustomAdapter(getApplicationContext(), cursor);
        mListView.setAdapter(reviewCustomAdapter);
        mListView.setOnItemClickListener(this); // Define onItemClickListener for listView
        volleyGetHttp("getReviews", resLat, resLng);


    }


    //******************** VOLLEY HTTP REQUEST *********************

    public void volleyGetHttp(String urlQuery, final String lat, String lng) {

        // Tokens for creating basic authentication with BACKAND
        final String MASTER_TOKEN = "6ca20768-1155-4a04-a5d9-481c1a28d079";
        final String USER_TOKEN = "d1a59639-4209-11e6-a39f-0ed7053426cb";

        // DIALOG
        final ProgressDialog dialog = ProgressDialog.show(RestaurantActivity.this, "",
                "Loading. Please wait...", true);
        //BACKAND URL
        String url = "https://api.backand.com/1/query/data/" + urlQuery + "?parameters=%7B%22lat%22:%22" + lat + "%22,%22lng%22:%22" + lng + "%22%7D";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        try {

                            db.emptyTable_REVIEW();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonObject = response.getJSONObject(i);
                                String userName = jsonObject.getString("name");
                                String userRate = jsonObject.getString("rate");
                                String userReview = jsonObject.getString("review");
                                String resLat = jsonObject.getString("lat");
                                String resLng = jsonObject.getString("lng");

                                ReviewObject reviewObject = new ReviewObject();
                                reviewObject.setName(userName);
                                reviewObject.setRate(userRate);
                                reviewObject.setReview(userReview);
                                reviewObject.setLat(resLat);
                                reviewObject.setLng(resLng);

                                db.addReviews(reviewObject);

                            }
                            cursor = db.getReviews(resLat, resLng);
                            reviewCustomAdapter.changeCursor(cursor);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("Restaurant Reviews", response.toString());
                        dialog.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();

                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Connection Error", Toast.LENGTH_SHORT).show();
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

    //******************** VOLLEY HTTP REQUEST *********************
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }


    public void gotoFav(View view) {

        db.addFavRestaurant(resPosition + "");
    }
}
