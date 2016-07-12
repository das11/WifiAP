package com.example.lordden.myapplication;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
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

import java.util.List;

public class Point extends AppCompatActivity implements
        SensorEventListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{

    WifiManager wms;
    Thread refresh;
    TextView tvt;

    private String ssid, pass, key;
    Firebase firessid, firepass;


    boolean thread_kill = false;
    int delay = 3 * 1000;


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
    private Firebase firelong, firelat, fireroot;

    private double testlong, testlat = 0;

    boolean chk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point);

        Firebase.setAndroidContext(this);

        key = getIntent().getExtras().getString("key");

        fireroot = new Firebase("https://wifiap-1361.firebaseio.com/");
        firessid = new Firebase("https://wifiap-1361.firebaseio.com/ssid");
        firepass = new Firebase("https://wifiap-1361.firebaseio.com/pass");

//        firessid.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                ssid = dataSnapshot.getValue(String.class);
//
//            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//
//            }
//        });
//
//        firepass.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                pass = dataSnapshot.getValue(String.class);
//            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//
//            }
//        });


        Query qref = fireroot.orderByChild("key").limitToFirst(1).equalTo(key);
        Log.d("query", qref.toString());
        Log.d("query 2", qref.getRef().toString());

        qref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ch : dataSnapshot.getChildren()){
                    Log.d("qer", ch.toString());
                    Log.d("qer 2", ch.getKey());

                    testlat = ch.child("beacon_lat").getValue(double.class);
                    testlong = ch.child("beacon_long").getValue(double.class);
                    ssid = ch.child("ssid").getValue(String.class);


                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


        tvt = (TextView) findViewById(R.id.tvt_point);

        wms = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        registerReceiver(mWifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        // wms.startScan();


        refresh = new Thread() {
            public void run() {

                while (!thread_kill) {
                    try {
                        Thread.sleep(delay);
                        Log.d("REF ::::::", "r");
                        wms.startScan();

                    } catch (InterruptedException e) {
                    }
                }
            }
        };
        refresh.start();

//###########

        firelong = new Firebase("https://wifiap-1361.firebaseio.com/beacon_long");
        firelat= new Firebase("https://wifiap-1361.firebaseio.com/beacon_lat");

//        firelong.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                double datalong = dataSnapshot.getValue(double.class);
//                testlong = datalong;
//                Log.d("firelonng", testlong + "");
//            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//
//            }
//        });
//
//        firelat.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                double datalat = dataSnapshot.getValue(double.class);
//                testlat = datalat;
//                Log.d("firelat", testlat + "");
//            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//
//            }
//        });

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mPointer = (ImageView) findViewById(R.id.point_pointer);


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
        Log.d("Bearing :: ", "" + res);

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
            float azimuthInDegress = (float)Math.toDegrees(azimuthInRadians);
//            if (azimuthInDegress < 0){
//                azimuthInDegress = azimuthInDegress + 360;
//            }

            azimuthInDegress = azimuthInDegress - (float)bearing;
            //float azimuthInDegress = ((float)(Math.toDegrees(azimuthInRadians)+360)%360) + (float)bearing;
            //azimuthInDegress = azimuthInDegress + (float)97.241;
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

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent objEvent) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }
        return super.onKeyUp(keyCode, objEvent);
    }

    @Override
    public void onBackPressed() {
        Log.d("back", "back");
        thread_kill = true;
        finish();
    }

    @Override
    protected void onDestroy(){
        Log.v("dsds","dsdsd");
        unregisterReceiver(mWifiScanReceiver);
        super.onDestroy();
    }

    private final BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
        String mssid, data;
        String[] datas = new String[10];
        String[] datafreq = new String[10];
        String[] datalev = new String[10];
        String[] datadis = new String[10];
        int lev;
        double dis, freq;

        @Override
        public void onReceive(Context c, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                List<ScanResult> mScanResults = wms.getScanResults();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < mScanResults.size(); ++i){
                    Log.d("po","");
                    mssid = mScanResults.get(i).SSID;
                    freq = mScanResults.get(i).frequency;
                    lev = mScanResults.get(i).level;
                    dis = calculateDistance(mScanResults.get(i).level, mScanResults.get(i).frequency);

                    if (mssid.equals(ssid)){
                        datas[i] = ssid;
                        datafreq[i] = freq + "";
                        datalev[i] = lev + "";
                        datadis[i] = dis + "";


                        sb.append("\n\n dis ::" + datadis[i]);

                        Log.d("RE", sb.toString());
                        Log.d("ss ::", "" + ssid);
                        Log.d("dbm ::", "" + lev);
                        Log.d("dis :: ", "" + dis);
                    }
                    else{
                        sb.append("No BEACON, GO HOME!");
                    }

                }
                Log.d("SR:", mScanResults + "");
                tvt.setText(sb.toString());


            }
        }
    };

    public double calculateDistance(double levelInDb, double freqInMHz)    {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(levelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }



}


