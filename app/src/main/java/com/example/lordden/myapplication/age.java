package com.example.lordden.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class age extends AppCompatActivity {

    WifiManager wms, wms2;
    Boolean thread_kill = false;
    Boolean threadKill2 = false;
    int delay = 3 * 1000, flag0, flag1;

    String ssid = "Test AP";

    List<ScanResult> mscanres;


    List<String> lssid = new ArrayList<String>();
    List<Integer> llev = new ArrayList<Integer>();
    List<Integer> flag = new ArrayList<Integer>();
    List<Integer> beacon_strength = new ArrayList<Integer>();
    String key;
    Thread refresh;

    Firebase fireroot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_age);

        key = getIntent().getExtras().getString("key");
        Log.d("key", key);

        APManager.configWifi(age.this);

        Firebase.setAndroidContext(this);
        fireroot = new Firebase("https://wifiap-1361.firebaseio.com/");


        Query qref = fireroot.orderByChild("key").equalTo(key);
        qref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ch : dataSnapshot.getChildren()){
                    //lssid = ch.child("ap_list").getValue(String.class);
                    llev = ch.child("ap_lev").getValue(List.class);
                    Log.d("lev", llev.toString());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

//        wms2 = (WifiManager)getSystemService(Context.WIFI_SERVICE);
//        registerReceiver(mWifiScanReceiver2, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        wms = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));




//        int h = 0;
//        while(h == 0){
//            while (!threadKill2){
//                wms2.startScan();
//                threadKill2 = true;
//                ++h;
//            }
//        }


        refresh =  new Thread(){

            public void run(){
                while(!thread_kill){
                    try{
                        Thread.sleep(delay);
                        Log.d("SCAN", "start");
                        wms.startScan();
                    }catch (InterruptedException e){

                    }
                }
            }
        };
        refresh.start();


    }

    @Override
    public boolean onKeyUp(int keycode, KeyEvent event){
        if (keycode == event.KEYCODE_BACK){
            onBackPressed();
            return true;
        }
        return super.onKeyUp(keycode, event);
    }

    @Override
    public void onBackPressed(){
        thread_kill = true;
        finish();
    }

    @Override
    protected void onDestroy(){
        //unregisterReceiver(mWifiScanReceiver2);
        unregisterReceiver(wifiScanReceiver);
        super.onDestroy();
    }


    private final BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)){
                mscanres = wms.getScanResults();
                Log.d("mscan", mscanres.toString());
                List<String> ssid = new ArrayList<String>();
                List<Integer> strength = new ArrayList<Integer>();
                int j = 0;

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < mscanres.size(); ++i){
                    ssid.add(mscanres.get(i).SSID);
                    Log.d("mssid", ssid.get(i).toString());
                    strength.add(mscanres.get(i).level);
                }
                Log.d("for", ssid.toString());
                //compare(ssid, strength);

            }
        }
    };

    void compare(List<String> xssid, List<Integer> dbm){
        boolean flag_match = false;
        Log.d("xssid", xssid.toString());
        List<String> true_ssid = new ArrayList<String>();
        List<Integer> true_lev = new ArrayList<Integer>();
        int count = 0, c = 0;
        Log.d("lssis_size", lssid.size() + "");
        Log.d("fr", lssid.toString());

        for(int i = 0 ; i < 2; ++i){
            if (xssid.get(count).toString().equals(lssid.get(i).toString())){
                true_ssid.add(xssid.get(count).toString());
                true_lev.add(dbm.get(count));
                ++count;
            }else{
                continue;
            }
        }

        Log.d("List true", true_ssid.toString());
        Log.d("List true dbm", true_lev.toString());

        flag0 = 0;
        flag1 = 0;

        for (int j = 0; j < true_ssid.size(); ++j){
                if (true_lev.get(j) < llev.get(c)){
                    flag.add(c, 0);
                    ++c;
                    ++flag0;
                }else if (true_lev.get(j) > llev.get(c)){
                    flag.add(c, 1);
                    ++c;
                    ++flag1;
                }
        }
        Log.d("flag0, flag1", " " + flag0 + "|" + flag1 + "");
        beacon_compare();

    }

    void beacon_compare(){

        for (int i = 0; i < mscanres.size(); ++i){
            if ((mscanres.get(i).SSID).equals(ssid)){
                beacon_strength.add(mscanres.get(i).level);

            }
        }
    }

    private final BroadcastReceiver mWifiScanReceiver2 = new BroadcastReceiver() {
        String ssid, data;
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
                    ssid = mScanResults.get(i).SSID;
                    freq = mScanResults.get(i).frequency;
                    lev = mScanResults.get(i).level;

                    lssid.add(ssid);
                    llev.add(lev);

                    Log.d("ssid", lssid.get(i).toString());
                    Log.d("lev", llev.get(i).toString());


                }
                Log.d("lssid@size", lssid.size() + "");
                Log.d("lssid", lssid.toString());
//                Log.d("SR:", mScanResults + "");
//                tv.setText(sb.toString());


            }
        }
    };
}
