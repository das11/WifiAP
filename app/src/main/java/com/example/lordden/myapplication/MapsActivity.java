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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.ads.mediation.MediationAdapter;
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
        SensorEventListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private ImageView mPointer;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float mCurrentDegree = 0f;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location location;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest mLocationRequest;
    private Marker marker;

    private double bearing;
    private  Firebase firelong, firelat;

    private double testlong, testlat;

    boolean chk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);

        Firebase.setAndroidContext(this);
        firelong = new Firebase("https://wifiap-1361.firebaseio.com/beacon_long");
        firelat= new Firebase("https://wifiap-1361.firebaseio.com/beacon_lat");

        firelong.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double datalong = dataSnapshot.getValue(double.class);
                testlong = datalong;
                Log.d("firelonng", testlong + "");
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        firelat.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double datalat = dataSnapshot.getValue(double.class);
                testlat = datalat;
                Log.d("firelat", testlat + "");
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mPointer = (ImageView) findViewById(R.id.pointer_maps_act);


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
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
    }


    @Override
    protected void onPause(){
        super.onPause();
        if (mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
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

        bearing = bearing(currentlong, currentlat, testlong, testlat);

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


        res = Math.atan2(Math.toRadians(x),Math.toRadians(y));
        res = Math.toDegrees(res);
//        if (res < 0){
//            res = res + 360;
//            Log.d("res - ", res + "");
//
//            return res;
//        }
        Log.d("res", "" + res);

        return res;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
            float azimuthInRadians = mOrientation[0];
            float azimuthInDegress = ((float)(Math.toDegrees(azimuthInRadians)+360)%360);
            azimuthInDegress = azimuthInDegress + (float)bearing;
            //float azimuthInDegress = (float)bearing;
            Log.d("azimuth + bearing", azimuthInDegress + "");
            RotateAnimation ra = new RotateAnimation(
                    mCurrentDegree,
                    -azimuthInDegress,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);

            ra.setDuration(250);

            ra.setFillAfter(true);

            mPointer.startAnimation(ra);
            mCurrentDegree = -azimuthInDegress;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }
}
