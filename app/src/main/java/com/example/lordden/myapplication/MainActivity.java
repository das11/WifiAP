package com.example.lordden.myapplication;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //private BroadcastRec receiver;
    private WifiManager wms;
    APManager man;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
       // fab.hide();

        Button btn1 = (Button)findViewById(R.id.btn1);
        Button btn2 = (Button)findViewById(R.id.btn2);
        Button btn3 = (Button)findViewById(R.id.btn3);
        Button btn4 = (Button)findViewById(R.id.btn4);
        Button btn = (Button)findViewById(R.id.button);
       // Button btn5 = (Button)findViewById(R.id.btn5);

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);
        btn.setOnClickListener(this);
        //btn5.setOnClickListener(this);

    }

    @Override
    public void onStop(){

        Log.v("stop main_act", "stop");
        //APManager.configAPState(MainActivity.this);
        super.onStop();
    }

    @Override
    public void onDestroy(){
        Log.v("dsds","dsdsd");
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {

            switch (view.getId()){

                case R.id.btn1 :
                    Intent i2 = new Intent(MainActivity.this, beacon.class);
                    startActivity(i2);
                    break;
                case R.id.btn2 :
                    Intent i = new Intent(MainActivity.this, Main2Activity.class);
                    startActivity(i);
                    break;
                case R.id.btn3 :
                    Intent i3 = new Intent(MainActivity.this, target.class);
                    startActivity(i3);
                    break;
                case R.id.btn4:
                    Intent i4 = new Intent(MainActivity.this, MapsActivity.class);
                    startActivity(i4);
                    break;
                case R.id.button:
                    Intent i5 = new Intent(MainActivity.this, TestNorth.class);
                    startActivity(i5);
                    break;

                default: // not RAMBO

            }
    }

}