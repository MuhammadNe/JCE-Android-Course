package com.swampsoftware.fastfood;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RestaurantActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private DatabaseHandler db;
    private Cursor restaurantCursor, cursor;
    private ListView mListView;
    private ReviewCustomAdapter reviewCustomAdapter;
    private TextView TVname, TVrate, TVaddress;
    private AlertDialog.Builder alert;


    private String resName;
    private String resLat;
    private String resLng;
    private String resPosition;
    private String facebook_name;
    private String facebook_id;
    private String resRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        TVname = (TextView) findViewById(R.id.TVresName);
        TVrate = (TextView) findViewById(R.id.TVresRate);
        TVaddress = (TextView) findViewById(R.id.TVresAddress);

        resPosition = getIntent().getStringExtra("restaurant");
        Log.d("resPosition", resPosition + "");
        db = new DatabaseHandler(this);
        mListView = (ListView) findViewById(R.id.listView2);
        restaurantCursor = db.getRestaurant(resPosition);

        int resNameIndex = restaurantCursor.getColumnIndex("_name");
        int latIndex = restaurantCursor.getColumnIndex("_lat");
        int lngIndex = restaurantCursor.getColumnIndex("_lng");
        int rateIndex = restaurantCursor.getColumnIndex("_rate");

        resName = restaurantCursor.getString(resNameIndex);
        resLat = restaurantCursor.getString(latIndex);
        resLng = restaurantCursor.getString(lngIndex);
        resRate = restaurantCursor.getString(rateIndex);

        GetAddress getAddress = new GetAddress(Double.parseDouble(resLat), Double.parseDouble(resLng));
        getAddress.execute();

        TVname.setText(resName);
        //TVaddress.setText(resLat + " / " + resLng);
        TVrate.setText(resRate);

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
                        Log.d("Restaurant reviews", response.toString());
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


//******************** VOLLEY HTTP REQUEST *********************

    public void volleyAddReviewHttp(String urlQuery,final String id, final String name, final String lat, final String lng, final String rate, final String review,
    final String _id, final String _name, final String _lat, final String _lng, final String _rate, final String _review) {

        // Tokens for creating basic authentication with BACKAND
        final String MASTER_TOKEN = "6ca20768-1155-4a04-a5d9-481c1a28d079";
        final String USER_TOKEN = "d1a59639-4209-11e6-a39f-0ed7053426cb";

        // DIALOG
        final ProgressDialog dialog = ProgressDialog.show(RestaurantActivity.this, "",
                "Loading. Please wait...", true);
        //BACKAND URL
        //https://api.backand.com/1/query/data/addReview?parameters=%7B%22facebookid%22:%22abc%22,%22facebookname%22:%22twinck5ara%22,%22lat%22:%2230%22,%22lng%22:%2230%22,%22rate%22:%224%22,%22review%22:%22wow,%20much%20eat%22%7D
        String url = "https://api.backand.com/1/query/data/" + urlQuery + "?parameters=%7B%22facebookid%22:%22" + id + "%22,%22facebookname%22:%22" + name
                + "%22,%22lat%22:%22" + lat + "%22,%22lng%22:%22" + lng + "%22,%22rate%22:%22" + rate + "%22,%22review%22:%22" + review + "%22%7D";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        //Update database
                        DatabaseHandler db = new DatabaseHandler(RestaurantActivity.this);
                        ReviewObject reviewObject = new ReviewObject();
                        reviewObject.setName(_name);
                        reviewObject.setLat(_lat);
                        reviewObject.setLng(_lng);
                        reviewObject.setRate(_rate);
                        reviewObject.setReview(_review);
                        db.addReviews(reviewObject);
                        cursor = db.getReviews(resLat, resLng);
                        reviewCustomAdapter.changeCursor(cursor);
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

    public void gotoMaps(View view) {

        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        intent.putExtra("name", resName);
        startActivity(intent);
    }

    public void gotoFav(View view) {

        db.addFavRestaurant(resPosition + "");
        Toast.makeText(getApplicationContext(), "Added To Favourites", Toast.LENGTH_SHORT).show();
    }

    public void gotofaceShare(View view) {
        startActivity(new Intent(getApplicationContext(), PhotoShare.class));
    }

    public void gotoAddReview(View view) throws Exception {


        //Check FACEBOOK Login - CHECK ACCESS TOKEN
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken == null) {
            Toast.makeText(getApplicationContext(), "Login To Facebook", Toast.LENGTH_SHORT).show();
        } else {
            facebookGraph(); // Get facebook User Name
            LinearLayout layout = new LinearLayout(getApplicationContext());
            layout.setOrientation(LinearLayout.VERTICAL);


            alert = new AlertDialog.Builder(this);

            final TextView TVname = new TextView(getApplicationContext());
            final TextView TVtype = new TextView(getApplicationContext());

            TVname.setText("Review");
            TVtype.setText("Rate");
            TVname.setTextColor(Color.BLACK);
            TVname.setTextSize(20);
            TVtype.setTextColor(Color.BLACK);
            TVtype.setTextSize(20);

            final EditText edittext = new EditText(getApplicationContext());
            edittext.setTextColor(Color.BLACK);
            edittext.setHint("Restaurant Name");


            String[] s = {"1", "2", "3", "4", "5"};
            final Spinner mSpinner = new Spinner(getApplicationContext());
            final ArrayAdapter<String> adp = new ArrayAdapter<String>(RestaurantActivity.this,
                    android.R.layout.simple_spinner_item, s);
            mSpinner.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            mSpinner.setAdapter(adp);
            alert.setTitle("Add Review");

            layout.addView(TVname);
            layout.addView(edittext);
            layout.addView(TVtype);
            layout.addView(mSpinner);

            alert.setView(layout);


            alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {


                    //check invalid input
                    if (edittext.getText().toString() != null && !edittext.getText().toString().matches("")) {
                        String rate = mSpinner.getSelectedItem().toString();
                        String review = edittext.getText().toString();
                        try {
                            // URL ENCODE
                            String _facebook_id = URLEncoder.encode(facebook_id, "UTF-8");
                            String _facebook_name = URLEncoder.encode(facebook_name, "UTF-8");
                            String _resLat = URLEncoder.encode(resLat, "UTF-8");
                            String _resLng = URLEncoder.encode(resLng, "UTF-8");
                            String _rate = URLEncoder.encode(rate, "UTF-8");
                            String _review = URLEncoder.encode(review, "UTF-8");
                            volleyAddReviewHttp("addReview",_facebook_id, _facebook_name, _resLat, _resLng, _rate,_review, facebook_id, facebook_name, resLat, resLng, rate, review);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });

            alert.setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // what ever you want to do with No option.
                }
            });

            alert.show();
        }
    }

    public void facebookGraph() throws Exception {
        try {
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            Log.d("Facebook Access Token", accessToken.getToken());

            GraphRequest request = GraphRequest.newMeRequest(
                    accessToken,
                    new GraphRequest.GraphJSONObjectCallback() {


                        @Override
                        public void onCompleted(JSONObject object,
                                                GraphResponse response) {
                            // Auto-genString id,name;
                            try {
                                if(object.has("id") && object.has("name")) {
                                    facebook_id = object.get("id").toString();
                                    facebook_name = object.get("name").toString();
                                    Log.d("FACEBOOK Name", facebook_name);
                                    Log.d("FACEBOOK ID", facebook_id);
                                }
                            } catch (JSONException e) {
                                // Auto-generated catch block
                                e.printStackTrace();
                            }
                            catch (Exception e) {

                            }

                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,link");
            request.setParameters(parameters);
            request.executeAsync();
            parameters.putString("fields", "id,name,link");
        } catch(Exception e) {

        }
    }



    //*******************************************************
    // INNER ASYNCTASK CLASS
    //*******************************************************
    class GetAddress extends AsyncTask<Void, Void, String> {

        // Declaring variables
        private double lat;
        private double lng;
        private ProgressDialog progress;

        // Constructor for initializing variables
        public GetAddress(double lat, double lng) {

            this.lat = lat;
            this.lng = lng;
        }

        // Method that converts lat lng to address using GEOCODER,
        // returns list with 1 cell
        public List<Address> getFromLocation(double lat, double lng, int maxResult) throws IOException {

            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(RestaurantActivity.this, Locale.getDefault());
            addresses = geocoder.getFromLocation(lat, lng, maxResult);

            return addresses;
        }

        // onPreExcecute : prepare progress dialog
        protected void onPreExecute() {

            progress = new ProgressDialog(RestaurantActivity.this);
            progress.setMessage("Getting address");
            progress.setIndeterminate(true);
            progress.show();
        }

        // onackGround : get location
        @Override
        protected String doInBackground(Void... params) {
            String address_string = "";
            try {

                if (getFromLocation(lat, lng, 1).size() > 0) {

                    Address address = getFromLocation(lat, lng, 1).get(0);
                    System.out.println("Address : " + address); // Bilding the string to set in the textview
                    for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                        address_string += address.getAddressLine(i).toString() + ", ";
                    }
                    if (address_string.endsWith(", ")) {
                        address_string = address_string.substring(0, address_string.length() - 2);
                    }
                    address_string += ".";
                } else {
                    // if list is empty then write Unavailable
                    address_string = "Unavailable";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return address_string;
        }

        // onPost close progress dialog
        protected void onPostExecute(String content) {

            System.out.println("Content onPostExecute : " + content);
            TVaddress.setText("Address : " + content);
            progress.dismiss();
        }
    }
}
