package com.swampsoftware.fastfood;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

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

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, AdapterView.OnItemSelectedListener {

    // Declaring objects for database and cursor
    private CallbackManager callbackManager;
    private DatabaseHandler db;
    private ListView mListView;
    private Cursor cursor;
    private RestaurantCustomAdapter restaurantCustomAdapter;
    private TabHost tabHost;
    private Spinner SPfilter, SPtype;

    private boolean longClick = false; //  variable that prevents onclick from happening when onlong click happened
    private boolean isResponseSuccess = false; //  variable that allows table update only when the user gets current location

    private static GoogleApiClient googleApiClient; // Object that is used to connect to google maps API, must be built to use fused location
    private LocationRequest locationRequest; //Object that requests a quality of service for location updates from fused location
    private Double lat = 0.0, lng = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the SDK before executing any other operations,
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_main);

        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();

        mListView = (ListView) findViewById(R.id.listView);
        SPfilter = (Spinner) findViewById(R.id.SPfilter);
        SPtype = (Spinner) findViewById(R.id.SPtype);

        SPfilter.setOnItemSelectedListener(this);
        SPtype.setOnItemSelectedListener(this);
        String[] filter = {"Rate", "Distance"};
        String[] filterType = {"Any Type", "FastFood", "Italian", "Mexican", "Chinese", "Middle Eastern", "Cafe"};
        ArrayAdapter<String> arrayAdapter_filter = new ArrayAdapter<String>(getApplicationContext(),R.layout.spinner_custom, filter);
        ArrayAdapter<String> arrayAdapter_filterType = new ArrayAdapter<String>(getApplicationContext(),R.layout.spinner_custom, filterType);

        SPfilter.setAdapter(arrayAdapter_filter);
        SPtype.setAdapter(arrayAdapter_filterType);
        // ******* TAB *******
        tabHost = (TabHost) findViewById(R.id.TabHost01);
        tabHost.setup();

        tabHost.addTab(tabHost.newTabSpec("1").setIndicator("Restaurants").setContent(mDummyTabContent));
        tabHost.addTab(tabHost.newTabSpec("2").setIndicator("Favourites").setContent(mDummyTabContent));
        tabHost.setOnTabChangedListener(mOnTabChangedListener);
        // ******* TAB *******

        // *********** Get all restaurants data ***********
        // Build database
        db = new DatabaseHandler(this);

        // Get records of saves location, save them in cursor and bind them to listview
        cursor = db.getAllRestaurants(SPfilter.getSelectedItem().toString(), SPtype.getSelectedItem().toString());
        restaurantCustomAdapter = new RestaurantCustomAdapter(getApplicationContext(), cursor);
        mListView.setAdapter(restaurantCustomAdapter);
        mListView.setOnItemClickListener(this); // Define onItemClickListener for listView
        mListView.setOnItemLongClickListener(this);
        volleyGetHttp("getRestaurants");
        // *********** Get all restaurants data ***********

        //mOnTabChangedListener.onTabChanged("1");

        //===================== FACEBOOK START========================
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);

        //This block of code gets the hash key and displays it in the console.
        try {
            PackageInfo info = this.getPackageManager().getPackageInfo(
                    "com.swampsoftware.fastfood", PackageManager.GET_SIGNATURES);
            for (android.content.pm.Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.i("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("NameNotFoundException", e.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("NoSuchAlgorithmExc", e.toString());
        }

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            //facebookGraph();
        }

        //callback to handle the results of the login attempts and register it
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult result) {

				/*System.out.println("User ID: "
                        + result.getAccessToken().getUserId()
						+ "\n" +
						"Auth Token: "
						+ result.getAccessToken().getToken());*/
                //facebookGraph();
            }

            @Override
            public void onCancel() {
                Log.d("Facebook Cancle", "ATTEPT CANCLED");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("Facebook error", error.toString());

            }

        });
        //===================== FACEBOOK END========================

        Log.d("SPTYPE",SPtype.getSelectedItem().toString());
        Log.d("SPFILTER",SPfilter.getSelectedItem().toString());
    }

    protected void onResume() {
        super.onResume();

        if (tabHost.getCurrentTab() == 0) {
            cursor = db.getAllRestaurants(SPfilter.getSelectedItem().toString(), SPtype.getSelectedItem().toString());
        } else if (tabHost.getCurrentTab() == 1) {
            cursor = db.getAllFavouriteRestaurants(SPfilter.getSelectedItem().toString(), SPtype.getSelectedItem().toString());
        }
        restaurantCustomAdapter.changeCursor(cursor);
    }

    protected void onPause() {
        super.onPause();
        googleApiClient.disconnect();
    }

    private final TabHost.TabContentFactory mDummyTabContent = new TabHost.TabContentFactory() {
        @Override
        public View createTabContent(String tag) {
            return mListView;
        }
    };

    private TabHost.OnTabChangeListener mOnTabChangedListener = new TabHost.OnTabChangeListener() {
        @Override
        public void onTabChanged(String tabId) {

            if (tabId.equalsIgnoreCase("1")) {
                // TODO : Get all restaurants data
                Log.d("TAB TAG", "1");
                cursor = db.getAllRestaurants(SPfilter.getSelectedItem().toString(), SPtype.getSelectedItem().toString());
                restaurantCustomAdapter.changeCursor(cursor);
            } else if (tabId.equalsIgnoreCase("2")) {
                // TODO : Get favourite restaurants
                Log.d("TAB TAG", "2");
                cursor = db.getAllFavouriteRestaurants(SPfilter.getSelectedItem().toString(), SPtype.getSelectedItem().toString());
                restaurantCustomAdapter.changeCursor(cursor);
            }

            // TODO : Notify cursor change

        }

    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void facebookGraph() {
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
                            String id = object.get("id").toString();
                            String name = object.get("name").toString();
                            Log.d("FACEBOOK Name", name);
                            Log.d("FACEBOOK ID", id);

                        } catch (JSONException e) {
                            // Auto-generated catch block
                            e.printStackTrace();
                        }

                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link");
        request.setParameters(parameters);
        request.executeAsync();
        parameters.putString("fields", "id,name,link");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        //cursor = db.getRestaurant(position + 1);
        Intent intent = new Intent(getApplicationContext(), RestaurantActivity.class);
        //intent.putExtra("restaurant", position + 1);
        intent.putExtra("restaurant", ((TextView) view.findViewById(R.id.TVresName)).getText().toString());
        //TextView textView = (TextView) view.findViewById(R.id.TVname);
        //intent.putExtra("restaurant", );
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }

    /*
    *
    *  Button Views Listener
    *
     */

    // Navigate to map
    public void gotoMaps(View view) {
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        startActivity(intent);
    }

    // Refresh the list
    public void gotoRefresh(View view) {

        googleApiClient.connect();
        volleyGetHttp("getRestaurants");

    }


    //******************** VOLLEY HTTP REQUEST *********************

    public void volleyGetHttp(String urlQuery) {

        // Tokens for creating basic authentication with BACKAND
        final String MASTER_TOKEN = "6ca20768-1155-4a04-a5d9-481c1a28d079";
        final String USER_TOKEN = "d1a59639-4209-11e6-a39f-0ed7053426cb";

        // DIALOG
        final ProgressDialog dialog = ProgressDialog.show(MainActivity.this, "",
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

                            for (int i = 0; i < response.length(); i++) {

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

                            isResponseSuccess = true;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error Fetching Data", Toast.LENGTH_SHORT).show();
                            isResponseSuccess = false;
                        }
                        dialog.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();

                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Connection Error", Toast.LENGTH_SHORT).show();
                        isResponseSuccess = false;
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

    //******************** Google Api Client *********************

    @Override
    public void onConnected(Bundle bundle) {

        locationRequest = locationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(1000);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        if (location != null && isResponseSuccess) {
            googleApiClient.disconnect();

            UserCurrentLocationObject userCurrentLocationObject = new UserCurrentLocationObject();
            userCurrentLocationObject.setLat(location.getLatitude());
            userCurrentLocationObject.setLng(location.getLongitude());

            Log.d("Location", userCurrentLocationObject.getLat() + " / " + userCurrentLocationObject.getLng());

            // Update the current tab : All / Favourite
            if (tabHost.getCurrentTab() == 0) {
                cursor = db.getAllRestaurants(SPfilter.getSelectedItem().toString(), SPtype.getSelectedItem().toString());
            } else if (tabHost.getCurrentTab() == 1) {
                cursor = db.getAllFavouriteRestaurants(SPfilter.getSelectedItem().toString(), SPtype.getSelectedItem().toString());
            }
            restaurantCustomAdapter.changeCursor(cursor);

        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (tabHost.getCurrentTab() == 0) {
            cursor = db.getAllRestaurants(SPfilter.getSelectedItem().toString(), SPtype.getSelectedItem().toString());
        } else if (tabHost.getCurrentTab() == 1) {
            cursor = db.getAllFavouriteRestaurants(SPfilter.getSelectedItem().toString(), SPtype.getSelectedItem().toString());
        }
        restaurantCustomAdapter.changeCursor(cursor);

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    //******************** Google Api Client *********************
}
