package ex1.jce.com.jce_ex2;

import android.Manifest;
import android.app.ProgressDialog;

import android.content.pm.PackageManager;
import android.database.Cursor;


import android.location.Address;
import android.location.Geocoder;

import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient googleApiClient; // Object that is used to connect to google maps API, must be built to use fused location
    private LocationRequest locationRequest; //Object that requests a quality of service for location updates from fused location
    private Location oldLocation = null; // Var for checking if the new location is > 1m than the old location

    final private int PERMISSION_REQUEST_CODE = 1; // request code for permissions

    // Declaring objects for database and cursor
    private DatabaseHandler db;
    private Cursor cursor;
    private CustomCursorAdapter customCursorAdapter;

    // Declaring views
    private ListView listView;
    private Button clearB, searchB;
    private TextView addressTV;
    private EditText latED, lngED, timeED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializing views
        listView = (ListView) findViewById(R.id.listView);
        clearB = (Button) findViewById(R.id.deleteQuery);
        searchB = (Button) findViewById(R.id.search);
        addressTV = (TextView) findViewById(R.id.addressTV);
        latED = (EditText) findViewById(R.id.latED);
        lngED = (EditText) findViewById(R.id.lngED);
        timeED = (EditText) findViewById(R.id.timeED);

        //build google api client for fused location method
        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // Build database
        db = new DatabaseHandler(this);

        // Get records of saves location, save them in cursor and bind them to listview
        cursor = db.getAllLocations();
        customCursorAdapter = new CustomCursorAdapter(getApplicationContext(), cursor);
        listView.setAdapter(customCursorAdapter);
        listView.setOnItemClickListener(this); // Define onItemClickListener for listView


        // Button listener for deleting all records from database : this will drop the entire database and create a new one
        clearB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.emptyTable(); // drop table and create a new one
                //db = new DatabaseHandler(getApplicationContext());
                cursor = db.getAllLocations();
                customCursorAdapter.changeCursor(cursor);

            }
        });

        // Button clickListener for search using lat lng time
        searchB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // If at least one field is not empty then apply the search
                if (!latED.getText().toString().isEmpty() || !lngED.getText().toString().isEmpty() || !timeED.getText().toString().isEmpty()) {
                    cursor = db.searchLocation(latED.getText().toString(), lngED.getText().toString(), timeED.getText().toString());
                    customCursorAdapter.changeCursor(cursor);

                }
            }
        });
    }


    // This is done using onResume instead of onStart to overRide when the user pauses the app to allow permissions from settings.
    // Or if user paused the app and gets back to it for any reason.
    protected void onResume() {

        super.onResume();

        // Check if user granted permissions
        if (checkPermissions()) {
            googleApiClient.connect(); // connect only if user granted permissions
        } else {
            System.out.println("> Permissions are DENIED, Requesting from user : ");
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET};
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    //  Disconnect apiClient and close cursor
    protected void onPause() {

        super.onPause();
        googleApiClient.disconnect();
    }

    protected void onStop() {

        super.onStop();
        googleApiClient.disconnect();
        cursor.close();
    }


    // Function to check permissions
    // If all permissions are granted then return true;
    // If false then grant the user to allow permissions
    private boolean checkPermissions() {

        int access_fine_location_permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int Internet_permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INTERNET);

        if (access_fine_location_permissionCheck == 0 && Internet_permissionCheck == 0) {
            return true;
        } else {
            return false;
        }
    }

    // This method as called a result for interacting with the user, check if the user allowed or denied permissions
    public void onRequestPermissionsResult(int request_code, String[] permissions, int[] results) {
        switch (request_code) {
            case PERMISSION_REQUEST_CODE: {
                if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED && results[1] == PackageManager.PERMISSION_GRANTED) {

                    googleApiClient.connect();
                } else {
                }
            }
        }
    }


    // If permissions are granted, connect the googleApi
    @Override
    public void onConnected(Bundle bundle) {

        System.out.println("> googleApiClient CONNECTED : Building locationRequest");

        locationRequest = locationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000); // Search for new location each 5 seconds
        locationRequest.setFastestInterval(5000);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    // On location changed method
    @Override
    public void onLocationChanged(Location location) {

        // Check if location  != null, if yes then check if old location != null, if yes, then check if the difference between old location and new is more than 1m
        if (location != null) {

            // if oldLocation isnt set then set it as the new location, after that compare with the new location
            if (oldLocation == null) {
                oldLocation = location;
            } else {

                // check if distance between locations is more than 1M
                float distanceInMeters = oldLocation.distanceTo(location);
               // Toast.makeText(getApplicationContext(), distanceInMeters + "", Toast.LENGTH_SHORT).show();
                if (distanceInMeters >= 1.0) {
                    // Get the time using Calendar class, result is in Millisecond
                    // Parse the result to get all the data.

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    String cYear = Integer.toString(calendar.get(Calendar.YEAR));
                    String cMonth = Integer.toString(calendar.get(Calendar.MONTH) + 1);
                    String cDay = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
                    String cHour = Integer.toString(calendar.get(Calendar.HOUR_OF_DAY));
                    String cMinute = Integer.toString(calendar.get(Calendar.MINUTE));
                    String cSecond = Integer.toString(calendar.get(Calendar.SECOND));

                    // Add new location to database
                    db.addLocation(new LocationData(Double.toString(location.getLatitude()), Double.toString(location.getLongitude()), cYear + "-" + cMonth + "-" + cDay
                            + " " + cHour + ":" + cMinute + ":" + cSecond));
                    cursor = db.getAllLocations();
                    customCursorAdapter.changeCursor(cursor); // refresh adapter
                    oldLocation = location; // sets the old location as the existing location to compare with the upcoming location
                } else {

                }
            }

        } else {
        }
    }

    // Item click listener
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        cursor = db.getLocation(position + 1); // position in list is +1 than position in cursor

        cursor.moveToFirst(); // make sure that the cursor is pointing to the current cell.

        // Asynctask for getting the address from lng and lat
        // Send params (lat lng)
        GetAddress getAddress = new GetAddress(Double.parseDouble(cursor.getString(1)), Double.parseDouble(cursor.getString(2)));
        getAddress.execute();
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
            geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
            addresses = geocoder.getFromLocation(lat, lng, maxResult);

            return addresses;
        }

        // onPreExcecute : prepare progress dialog
        protected void onPreExecute() {

            progress = new ProgressDialog(MainActivity.this);
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
            addressTV.setText("Address : " + content);
            progress.dismiss();
        }
    }

}
