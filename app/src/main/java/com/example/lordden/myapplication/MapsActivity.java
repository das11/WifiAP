package com.example.lordden.myapplication;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

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

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location location;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest mLocationRequest;
    private Marker marker;

    boolean chk;

    private int mAzimuth = 0; // degree

    SensorManager mSensorManager;

    private SensorEventListener mSensorEventListener = new SensorEventListener() {

        float[] orientation = new float[3];
        float[] rMat = new float[9];

        public void onAccuracyChanged( Sensor sensor, int accuracy ) {}

        @Override
        public void onSensorChanged( SensorEvent event ) {
            if( event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR ){
                // calculate th rotation matrix
                SensorManager.getRotationMatrixFromVector( rMat, event.values );
                // get the azimuth value (orientation[0]) in degree
                mAzimuth = (int) ( Math.toDegrees( SensorManager.getOrientation( rMat, orientation )[0] ) + 360 ) % 360;

                Log.d("Aziimuth", mAzimuth + "");
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);

        Log.d("sens", "");
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        Log.d("1","11");

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

        //Test location
        double testlong = 91.75411;
        double testlat = 26.185160;

        double bearing = bearing(currentlong, currentlat, testlong, testlat);

        Toast.makeText(getApplicationContext(),"lat :: " + currentlat + "long :: " + currentlong + "\nDIRn : " + bearing,Toast.LENGTH_SHORT).show();

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
    }

    public double bearing(double Along, double Alat, double Blong, double Blat){

        double x,y, res, t;
        t = (Along - Blong) * (-1);

        x = Math.cos(Math.toRadians(Blat)) * Math.sin(Math.toRadians(t));
        y = Math.cos(Math.toRadians(Alat)) * Math.sin(Math.toRadians(Blat)) - Math.sin(Math.toRadians(Alat)) * Math.cos(Math.toRadians(Blat)) * Math.cos(Math.toRadians(t));

        Log.d("t","" + t);
        Log.d("X","" + x);
        Log.d("Y","" + y);


        res = Math.toDegrees(Math.atan2(Math.toRadians(x),Math.toRadians(y)));
        Log.d("res", "" + res);

        return res;
    }
}
