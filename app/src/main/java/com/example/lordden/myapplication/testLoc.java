package com.example.lordden.myapplication;

import android.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class testLoc extends AppCompatActivity implements
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location location;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest mLocationRequest;
    private Marker marker;
    Toast testtoast;

    private String dirresponse, query_url = "http://maps.googleapis.com/maps/api/directions/json?origin=26.1861458,91.7535008&destination=26.1834051,91.78202229999999";
    String poly_S;
    double distance;
    private List<LatLng> dpoly = new ArrayList<>();

    private Firebase fireroot;
    private String key;
    private double currentlat, currentlong;
    private WifiManager wms;

    private List<String> lssid = new ArrayList<String>();
    private List<Integer> llev = new ArrayList<Integer>();

    Thread refresh, runAP;
    int h = 0;
    boolean thread_kill = false, flag_list = false, thread_kill_2 = false;
    String tempssid, templev;
    APManager man;


    boolean chk;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_loc);

        Firebase.setAndroidContext(this);
        fireroot = new Firebase("https://wifiap-1361.firebaseio.com/");

        key = getIntent().getExtras().getString("key");
        Log.d("key_beacon", key);

        wms = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        registerReceiver(wifiscanreceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        refresh =  new Thread(){

            public void run(){
                while(!thread_kill){
                    try{
                        Thread.sleep(1000);
                        Log.d("SCAN", "start");
                        wms.startScan();
                        h = 1;
                    }catch (InterruptedException e){

                    }
                }
            }
        };
        refresh.start();


        //thread kill 2 runs once to initiate hotspot :: if flag successful
        runAP = new Thread(){
            public void run(){
                while (!thread_kill_2){
                    if (flag_list){
                        Log.d("run AP", "");
                        man.configWifi(testLoc.this);
                        man.configAPState(testLoc.this);
                        man.configCred(testLoc.this);
                        thread_kill_2 = true;
                    }
                }
            }
        };
        runAP.start();


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        if (chk = checkperm()){
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                    .setFastestInterval(4 * 1000); // 1 second, in milliseconds
        }else{
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 12);
            if (checkperm()){
                mLocationRequest = LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                        .setFastestInterval(4 * 1000); // 4 seconds, in milliseconds
            }
        }



        Query qref = fireroot.orderByChild("busy").limitToFirst(1).equalTo("pappuram");
        Log.d("query", qref.toString());
        Log.d("query 2", qref.getRef().toString());

        qref.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ch : dataSnapshot.getChildren()){
                    Log.d("qer", ch.toString());
                    Log.d("qer 2", ch.getKey());

                    ch.child("key").getRef().setValue(key);
                    ch.child("busy").getRef().setValue("true");
//                    ch.child("beacon_lat").getRef().setValue(currentlat);
//                    ch.child("beacon_long").getRef().setValue(currentlong);
//                    Log.d("co 2 ", currentlat + " " + currentlong);
                    //ch.child("ssid").getRef().setValue(ssid);


                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onStop(){
        man.configAPState(testLoc.this);
        man.configWifi(testLoc.this);
        super.onStop();
    }

    @Override
    protected void onDestroy(){
        unregisterReceiver(wifiscanreceiver);
        super.onDestroy();
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
        }

    }

    public boolean checkperm(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return false;
        }else{
            return true;
        }
    }



    @Override
    public void onConnected(Bundle bundle) {

        if (checkperm()){
            location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
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
        Log.d("locc", location.toString());

        currentlat = location.getLatitude();
        currentlong = location.getLongitude();

        Log.d("co", currentlat + " " + currentlong);

        Query qref = fireroot.orderByChild("key").limitToFirst(1).equalTo(key);
        Log.d("query", qref.toString());
        Log.d("query 2", qref.getRef().toString());

        qref.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ch : dataSnapshot.getChildren()){

                    ch.child("beacon_lat").getRef().setValue(currentlat);
                    ch.child("beacon_long").getRef().setValue(currentlong);
                    Log.d("co 2 ", currentlat + " " + currentlong);
                    //ch.child("ssid").getRef().setValue(ssid);
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

        if(marker != null){
            marker.remove();
        }

        LatLng latlong = new LatLng(currentlat, currentlong);
        MarkerOptions moptions = new MarkerOptions()
                .position(latlong);
        //marker = mMap.addMarker(moptions);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latlong)
                .tilt(60)
                .zoom(16)
                .build();

        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latlong));
//        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000, null);




        //##################################### Drawwing poly too

        //Toast.makeText(getApplicationContext(), "::" + distance, Toast.LENGTH_SHORT).show();

//        if (testtoast != null){
//            testtoast.cancel();
//        }
//        testtoast = Toast.makeText(getApplicationContext(),"At: "+ latlong.toString() + "\n::" + distance,Toast.LENGTH_SHORT);
//        testtoast.show();


    }

    @Override
    public void onLocationChanged(Location location)
    {
        handleLocation(location);
    }

    private void setupmap(){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(41.889, -87.622), 16));

        // You can customize the marker image using images bundled with
        // your app, or dynamically generated bitmaps.
        mMap.addMarker(new MarkerOptions()
                .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                .position(new LatLng(41.889, -87.622)));
    }

    private final BroadcastReceiver wifiscanreceiver = new BroadcastReceiver() {
        String ssid;
        int lev, max, temp, run_once = 0;

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)){
                List<ScanResult> scanres = wms.getScanResults();


                lssid.clear();
                llev.clear();
                for (int i = 0; i < scanres.size(); ++i){
                    ssid = scanres.get(i).SSID;
                    lev = scanres.get(i).level;

                    lssid.add(ssid);
                    llev.add(lev);
                }

                Log.d("lssid@size", lssid.size() + "");
                Log.d("lssid", lssid.toString());
                Log.d("lssid lev", llev.toString());


                Query qref = fireroot.orderByChild("key").limitToFirst(1).equalTo(key);
                Log.d("query", qref.toString());
                Log.d("query 2", qref.getRef().toString());

                templev = "";
                tempssid = "";
                tempssid = lssid.toString();
                templev = llev.toString();

                Log.d("lssid 2", tempssid);
                Log.d("lssid 2 lev", templev);

                while (run_once == 0){
                    max = lssid.size();
                    run_once = 1;
                }
                temp = lssid.size();
                if (temp >= max) {
                    max = temp;

                    qref.addValueEventListener(new ValueEventListener(){
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot ch : dataSnapshot.getChildren()){
                                ch.child("ap_lev").getRef().setValue(llev);
                                ch.child("ap_list").getRef().setValue(tempssid);

                                Log.d("list ap", tempssid);
                                Log.d("list ap lev", templev);


                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });
                }else {
                    thread_kill = true;
                    flag_list = true;
                }

            }


        }
    };

}