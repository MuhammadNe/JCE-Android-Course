package com.swampsoftware.fastfood;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.facebook.AccessToken;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private static GoogleApiClient googleApiClient; // Object that is used to connect to google maps API, must be built to use fused location
    private LocationRequest locationRequest; //Object that requests a quality of service for location updates from fused location
    private Marker currentLocationMarker;
    private FrameLayout frameLayout;
    private AlertDialog.Builder alert;
    private String lat, lng;
    private DatabaseHandler db;
    private Marker marker;
    private ArrayList<Marker> markerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        frameLayout = (FrameLayout) findViewById(R.id.mapfl);
        db = new DatabaseHandler(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // Initializing map
        initMap();

        frameLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (googleApiClient.isConnected()) {
                    googleApiClient.disconnect();
                }
                return false;
            }
        });

    }

    private void setMarkers(Cursor cursor) {

        String intentName = "";
        if (getIntent().hasExtra("name")) {
            intentName = getIntent().getStringExtra("name");
            Log.d("EXTRA", intentName);
        } else {
            Log.e("EXTRA", "NO EXTRA");
        }
        if (markerList != null) {
            markerList.clear();
        }

        markerList = new ArrayList<>();

        while (cursor.moveToNext()) {

            int latIndex = cursor.getColumnIndex("_lat");
            int lngIndex = cursor.getColumnIndex("_lng");
            int nameIndex = cursor.getColumnIndex("_name");

            String lat = cursor.getString(latIndex);
            String lng = cursor.getString(lngIndex);
            String name = cursor.getString(nameIndex);

            Double _lat = Double.parseDouble(lat);
            Double _lng = Double.parseDouble(lng);

            LatLng latlng = new LatLng(_lat, _lng);
            //marker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).position(ll).title("You're Here"));
            marker = mMap.addMarker(new MarkerOptions().position(latlng).title(name));
            if (name.equals(intentName)) {
                Location location = new Location("RestarantLocation");
                location.setLatitude(_lat);
                location.setLongitude(_lng);
                gotoLocation(location);
                googleApiClient.disconnect();
            }
            markerList.add(marker);

        }

    }

    protected void onPause() {
        super.onPause();
        googleApiClient.disconnect();
        Log.d("googleApiClient", "Disconnected");
        if (googleApiClient.isConnected()) {
            Log.e("googleApiClient", "Error Disconnecting googleApiClient");
        }
    }

    // Method that animates the camera to a new location
    public void gotoLocation(Location location) {


        LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
        }
        currentLocationMarker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).position(ll).title("You're Here"));
        currentLocationMarker.showInfoWindow();
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 15);
        mMap.animateCamera(update);

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    // Initialize GoogleMap -> mMap
    public void initMap() {

        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

        } else {
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        // connecting googleApiClient on map ready, to avoid null pointer exception;
        Log.d("googleApiClient", "Connecting");
        googleApiClient.connect();
        if (!googleApiClient.isConnected()) {
            Log.e("googleApiClient", "Error Connecting googleApiClient");
        }

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        Cursor cursor = db.getAllRestaurants("map", "map");
        setMarkers(cursor);
    }

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


        lat = location.getLatitude() + "";
        lng = location.getLongitude() + "";
        Log.d("Location", location.getLatitude() + " " + location.getLongitude());
            gotoLocation(location);


    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void startUpdate(View view) {

        if (!googleApiClient.isConnected()) {

            googleApiClient.connect();
        }
    }

    public void gotoAdd(View view) {
        //Check FACEBOOK Login - CHECK ACCESS TOKEN
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken == null) {
            Toast.makeText(getApplicationContext(), "Login To Facebook", Toast.LENGTH_SHORT).show();
        } else {
            LinearLayout layout = new LinearLayout(getApplicationContext());
            layout.setOrientation(LinearLayout.VERTICAL);


            alert = new AlertDialog.Builder(this);

            final TextView TVname = new TextView(getApplicationContext());
            final TextView TVtype = new TextView(getApplicationContext());

            TVname.setText("Name");
            TVtype.setText("Type");
            TVname.setTextColor(Color.BLACK);
            TVname.setTextSize(20);
            TVtype.setTextColor(Color.BLACK);
            TVtype.setTextSize(20);

            final EditText edittext = new EditText(getApplicationContext());
            edittext.setTextColor(Color.BLACK);
            edittext.setHint("Restaurant Name");


            String[] s = {"Choose Type", "FastFood", "Italian", "Mexican", "Chinese", "Middle Eastern", "Cafe"};
            final Spinner mSpinner = new Spinner(getApplicationContext());
            final ArrayAdapter<String> adp = new ArrayAdapter<String>(MapsActivity.this,
                    android.R.layout.simple_spinner_item, s);
            mSpinner.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            mSpinner.setAdapter(adp);
            alert.setTitle("Add Restaurant");

            layout.addView(TVname);
            layout.addView(edittext);
            layout.addView(TVtype);
            layout.addView(mSpinner);

            alert.setView(layout);


            alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {


                    //check invalid input
                    if (edittext.getText().toString() != null && !edittext.getText().toString().matches("")) {
                        String type = mSpinner.getSelectedItem().toString();
                        String name = edittext.getText().toString();
                        try {
                            // URL ENCODE
                            type = URLEncoder.encode(type, "UTF-8");
                            name = URLEncoder.encode(name, "UTF-8");
                            volleyGetHttp("addRestaurant", name, lat, lng, type);
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

    //******************** VOLLEY HTTP REQUEST *********************

    public void volleyGetHttp(String urlQuery, final String name, final String lat, final String lng, final String type) {

        // Tokens for creating basic authentication with BACKAND
        final String MASTER_TOKEN = "6ca20768-1155-4a04-a5d9-481c1a28d079";
        final String USER_TOKEN = "d1a59639-4209-11e6-a39f-0ed7053426cb";

        // DIALOG
        final ProgressDialog dialog = ProgressDialog.show(MapsActivity.this, "",
                "Loading. Please wait...", true);
        //BACKAND URL
        //https://api.backand.com/1/query/data/addRestaurant?parameters=%7B%22name%22:%22adeem%22,%22lat%22:%2236%22,%22lng%22:%2236%22,%22type%22:%22italian%22%7D
        String url = "https://api.backand.com/1/query/data/" + urlQuery + "?parameters=%7B%22name%22:%22" + name + "%22,%22lat%22:%22" + lat + "%22,%22lng%22:%22" + lng + "%22,%22type%22:%22" + type + "%22%7D";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        //Update database
                        db = new DatabaseHandler(MapsActivity.this);
                        RestaurantObject restaurantObject = new RestaurantObject();
                        restaurantObject.setName(name);
                        restaurantObject.setLat(lat);
                        restaurantObject.setLng(lng);
                        restaurantObject.setType(type);
                        db.addRestaurant(restaurantObject);
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

}
