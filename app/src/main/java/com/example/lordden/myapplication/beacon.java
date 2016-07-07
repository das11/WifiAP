package com.example.lordden.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class beacon extends AppCompatActivity implements
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{

    APManager man;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location location;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest mLocationRequest;
    private Marker marker;

    private double bearing;
    private Firebase firelong, firelat, firessid, firepass, firebusy;

    String ssid, pass;

    boolean chk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon);

        Firebase.setAndroidContext(this);
        firelong = new Firebase("https://wifiap-1361.firebaseio.com/beacon_long");
        firelat= new Firebase("https://wifiap-1361.firebaseio.com/beacon_lat");
        firessid = new Firebase("https://wifiap-1361.firebaseio.com/ssid");
        firepass = new Firebase("https://wifiap-1361.firebaseio.com/pass");
        firebusy = new Firebase("https://wifiap-1361.firebaseio.com/busy");

        firebusy.setValue("true");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (Settings.System.canWrite(beacon.this)){
                Log.d("perm _ write", "");
            }
            else
            {
                Log.d("perm _ 2", "");
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

        }



        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        if (chk = checkperm()){
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                    .setFastestInterval(4 * 1000); // 4 second, in milliseconds
            Log.d("2","11");
        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 12);
            if (checkperm()){
                mLocationRequest = LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                        .setFastestInterval(4 * 1000); // 4 seconds, in milliseconds
                Log.d("2:2","11");
            }
        }



    }

    @Override
    protected void onResume(){
        super.onResume();
        mGoogleApiClient.connect();

        man.isAPon(beacon.this);
        man.configAPState(beacon.this);
        man.configCred(beacon.this);

        ssid = man.ssid();
        pass = man.pass();

        firessid.setValue(ssid);
        firepass.setValue(pass);
        Log.d("ssidf", ssid);

        Log.d("beacon", "beacon");
    }


    @Override
    protected void onPause(){
        super.onPause();
        if (mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
    }

    public boolean checkperm(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return false;
        }else{
            return true;
        }
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (checkperm()){
            mMap.setMyLocationEnabled(true);
            Log.d("3","11");
        }

    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("1","11");

        if (checkperm()){
            location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            Log.d("4","11");
        }else {

            //denied, cant reach here if denied anyway, so hakuna-matata
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed( ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("test", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    private void handleLocation(Location location)
    {
        //Toast.makeText(getApplicationContext(),"polo2", Toast.LENGTH_SHORT).show();
        Log.d("current_location", location.toString());

        //Current location
        double currentlat = location.getLatitude();
        double currentlong = location.getLongitude();

        firelong.setValue(currentlong);
        firelat.setValue(currentlat);

        Toast.makeText(getApplicationContext(), "Coordinates : " + currentlong + " , " +  currentlat, Toast.LENGTH_SHORT).show();

        //Test location

        //Toast.makeText(getApplicationContext(),"lat :: " + currentlat + "long :: " + currentlong + "\nDIRn : " + bearing,Toast.LENGTH_SHORT).show();

        if(marker != null){
            marker.remove();
        }

        LatLng latlong = new LatLng(currentlat, currentlong);
        MarkerOptions moptions = new MarkerOptions()
                .position(latlong);
//        marker = mMap.addMarker(moptions);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latlong)
                .tilt(60)
                .zoom(16)
                .build();

        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latlong));
        // mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000, null);



    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("5","11");
        handleLocation(location);

        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onStop(){
        man.configAPState(beacon.this);
        man.configWifi(beacon.this);

        firebusy.setValue("false");
        super.onStop();
    }
}
