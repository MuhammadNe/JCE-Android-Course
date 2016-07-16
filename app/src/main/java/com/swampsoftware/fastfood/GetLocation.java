package com.swampsoftware.fastfood;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by Muhammad on 7/15/2016.
 */
public class GetLocation extends Activity implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private static GoogleApiClient googleApiClient; // Object that is used to connect to google maps API, must be built to use fused location
    private LocationRequest locationRequest; //Object that requests a quality of service for location updates from fused location
    private Double lat = 0.0, lng = 0.0;

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public void connectGoogleApiClient() {
        googleApiClient.connect();
    }
    public void disconnectGoogleApiClient() {
        googleApiClient.disconnect();
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

        if(location != null) {
            this.lat = location.getLatitude();
            this.lng = location.getLongitude();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
