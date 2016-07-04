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
import android.widget.TextView;

import java.util.List;

public class target extends AppCompatActivity {

    WifiManager wms;
    Thread refresh;
    TextView tvt;

    boolean thread_kill = false;

    int delay = 3 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target);

        tvt = (TextView) findViewById(R.id.tvt);

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
                    dis = calculateDistance(mScanResults.get(i).level, mScanResults.get(i).frequency);

                    if (ssid.equals("Hash_Wave")){
                        datas[i] = ssid;
                        datafreq[i] = freq + "";
                        datalev[i] = lev + "";
                        datadis[i] = dis + "";


                        sb.append("\n\nSSID :: " + datas[i] + " Freq :: " + datafreq[i] + " dBm ::" + datalev[i] + " dis ::" + datadis[i]);

                        Log.d("RE", sb.toString());
                        Log.d("ss ::", "" + ssid);
                        Log.d("dbm ::", "" + lev);
                        Log.d("dis :: ", "" + dis);
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
