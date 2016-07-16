package com.example.lordden.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class age extends AppCompatActivity {

    WifiManager wms;
    Boolean thread_kill = false;
    int delay = 3 * 1000;
    List<String> lssid = new ArrayList<String>();
    List<Integer> llev = new ArrayList<Integer>();
    List<Integer> flag = new ArrayList<Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_age);

        APManager.configWifi(age.this);

        wms = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));


    }


    private final BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)){
                List<ScanResult> mscanres = wms.getScanResults();
                List<String> ssid = new ArrayList<String>();
                List<Integer> strength = new ArrayList<Integer>();
                int j = 0;

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < mscanres.size(); ++i){
                    ssid.add(mscanres.get(i).SSID);
                    strength.add(mscanres.get(i).level);
                }
            }
        }
    };

    void compare(List<String> ssid, List<Integer> dbm){
        boolean flag_match = false;
        List<String> true_ssid = new ArrayList<String>();
        List<Integer> true_lev = new ArrayList<Integer>();
        int count = 0, i = 0;

        for(int i = 0; i < lssid.size(); ++i){
            if (ssid.get(count).toString().equals(lssid.get(i).toString())){
                true_ssid.add(ssid.get(count).toString());
                true_lev.add(dbm.get(count));
                ++count;
            }
        }

        for (int j = 0; j < true_ssid.size(); ++j){
                if (true_lev.get(j) < llev.get(i)){
                    flag.add(i, 0);
                }else if (true_lev.get(j) > llev.get(i)){
                    flag.add(i, 1);
                }
        }

    }
}
