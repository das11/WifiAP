package com.example.lordden.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class beacon extends AppCompatActivity {

    APManager man;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon);

        man.isAPon(beacon.this);
        man.configAPState(beacon.this);
        man.configCred(beacon.this);
        Log.d("beacon", "beacon");
    }

    @Override
    protected void onStop(){
        man.configAPState(beacon.this);
        super.onStop();
    }
}
