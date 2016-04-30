package ex1.jce.com.jce_ex2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.location.Location;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.security.Timestamp;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient googleApiClient; // Object that is used to connect to google maps API, must be built to use fused location
    private LocationRequest locationRequest; //Object that requests a quality of service for location updates from fused location
    private Location oldLocation = null;

    final private int PERMISSION_REQUEST_CODE = 1;

    DatabaseHandler db;
    Cursor cursor;
    ListView listView;
    CustomCursorAdapter customCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listView);
        //build google api client for fused location method
        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        db = new DatabaseHandler(this);


        /**
         * CRUD Operations
         * */
        cursor = db.getAllLocations();
        while (cursor.moveToNext()) {
            System.out.print(cursor.getString(0) + " / ");
            System.out.print(cursor.getString(1) + " / ");
            System.out.print(cursor.getString(2) + " / ");
            System.out.println(cursor.getString(3));

        }

        customCursorAdapter = new CustomCursorAdapter(getApplicationContext(), cursor);
        listView.setAdapter(customCursorAdapter);
        listView.setOnItemClickListener(this);


    }


    protected void onResume() {

        super.onResume();

        // Check if user granted permissions
        if (checkPermissions()) {
            //System.out.println("Permission Check : " + access_fine_location_permissionCheck);
            System.out.println("> Permissions are GRANTED.");
            System.out.println("> Connecting googleApiClient.");
            googleApiClient.connect();
        } else {
            System.out.println("> Permissions are DENIED, Requesting from user : ");

            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET};
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    protected void onPause() {

        super.onPause();
        googleApiClient.disconnect();
    }

    protected void onStop() {

        super.onStop();
        googleApiClient.disconnect();
    }


    private boolean checkPermissions() {

        System.out.println("> Checking Permissions : ");

        int access_fine_location_permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int Internet_permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INTERNET);

        if (access_fine_location_permissionCheck == 0 && Internet_permissionCheck == 0) {
            return true;
        } else {
            return false;
        }
    }

    public void onRequestPermissionsResult(int request_code, String[] permissions, int[] results) {
        switch (request_code) {
            case PERMISSION_REQUEST_CODE: {
                if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED && results[1] == PackageManager.PERMISSION_GRANTED) {

                    Log.w("MainActivity", "Permissions Granted");
                    googleApiClient.connect();
                } else {
                    Log.e("MainActivity", "Permissions Denied");
                }
            }
        }
    }


    @Override
    public void onConnected(Bundle bundle) {

        System.out.println("> googleApiClient CONNECTED : Building locationRequest");

        locationRequest = locationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(5000);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        if (location != null) {
            //if(oldLocation != null)

            db.addLocation(new LocationData(Double.toString(location.getLatitude()), Double.toString(location.getLongitude()), Long.toString(System.currentTimeMillis())));
            cursor = db.getAllLocations();
            customCursorAdapter.changeCursor(cursor);
           // customCursorAdapter = new CustomCursorAdapter(getApplicationContext(), cursor);
           // listView.setAdapter(customCursorAdapter);



            System.out.println(location.getLatitude() + " // " + location.getLongitude());
        } else {
            System.out.println("> Location = NULL");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Cursor cursor = db.getLocation(position + 1);

        cursor.moveToFirst();

        System.out.print(cursor.getString(0) + " / ");
        System.out.print(cursor.getString(1) + " / ");
        System.out.print(cursor.getString(2) + " / ");
        System.out.println(cursor.getString(3));


        System.out.println("Position : " + position);
    }


}
