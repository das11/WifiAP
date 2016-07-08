package com.example.lordden.myapplication;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.File;
import java.io.IOException;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //private BroadcastRec receiver;
    private WifiManager wms;
    APManager man;

    Firebase firebusy, fireroot, ref;
    boolean busy = false, mbusy;
    String key, test_busy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try{
            File filename = new File(Environment.getExternalStorageDirectory()+"/mylog.log");
            filename.createNewFile();
            String cmd = "logcat -d -f"+filename.getAbsolutePath();
            Runtime.getRuntime().exec(cmd);
            Log.d("crash", "lool");
            Toast.makeText(getApplicationContext(), "LOG SAVED", Toast.LENGTH_LONG);
        }catch (IOException e){
            e.printStackTrace();
        }

        final RadioButton busybtn = (RadioButton)findViewById(R.id.busybtn);
        Button push = (Button)findViewById(R.id.pushbtn);
        final EditText keyedit = (EditText)findViewById(R.id.keyedit);

        push.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                key = keyedit.getText().toString();
            }
        });



        busybtn.setText("Checking backend ...");

        Firebase.setAndroidContext(this);
        fireroot = new Firebase("https://wifiap-1361.firebaseio.com/");
        firebusy = new Firebase("https://wifiap-1361.firebaseio.com/busy");

        fireroot.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//                for (DataSnapshot data : dataSnapshot.getChildren()){
////                    for (DataSnapshot av_node : data.getChildren()){
//////                        mbusy = av_node.getValue(boolean.class);
//////                        if (!mbusy){
//////                            for (DataSnapshot key_da : data.getChildren()){
//////                                if ((key_da.getKey().toString()).equals("key")){
//////                                    ref = key_da.getRef();
//////                                    ref.setValue(key);
//////
//////                                    Log.d("FIRE ::", key + busy + "");
//////                                }
//////                            }
//////                        }
////                        Log.d("Fire \n", dataSnapshot + "");
////                    }
//                    Log.d("Fire \n", dataSnapshot + "");
//                }
                Log.d("DUB", dataSnapshot.getKey().toString());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

////        //fireroot.addChildEventListener(new ChildEventListener() {
////            @Override
////            public void onDataChange(DataSnapshot dataSnapshot) {
////                for (DataSnapshot data : dataSnapshot.getChildren()){
//////                    for (DataSnapshot av_node : data.getChildren()){
////////                        mbusy = av_node.getValue(boolean.class);
////////                        if (!mbusy){
////////                            for (DataSnapshot key_da : data.getChildren()){
////////                                if ((key_da.getKey().toString()).equals("key")){
////////                                    ref = key_da.getRef();
////////                                    ref.setValue(key);
////////
////////                                    Log.d("FIRE ::", key + busy + "");
////////                                }
////////                            }
////////                        }
//////                        Log.d("Fire \n", dataSnapshot + "");
//////                    }
////                    Log.d("Fire \n", dataSnapshot + "");
////                }
////            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//
//            }
//        });

        firebusy.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                busy = dataSnapshot.getValue(boolean.class);

                if (!busy){
                    busybtn.setChecked(false);
                    busybtn.setText("Backend : Available");
                }else {
                    busybtn.setChecked(true);
                    busybtn.setText("Backend : Busy");
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

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
        Button btn6 = (Button)findViewById(R.id.pointbtn);
       // Button btn5 = (Button)findViewById(R.id.btn5);


//
//        btn2.setVisibility(View.INVISIBLE);
//        btn3.setVisibility(View.INVISIBLE);
//        btn4.setVisibility(View.INVISIBLE);

       // btn.setVisibility(View.INVISIBLE);

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);
        btn.setOnClickListener(this);
        btn6.setOnClickListener(this);
        busybtn.setOnClickListener(this);
        //btn5.setOnClickListener(this);

//        checkForUpdates();


    }

    @Override
    protected void onResume(){
        super.onResume();
//        checkForCrashes();

    }

    @Override
    public void onPause() {
        super.onPause();
//        unregisterManagers();
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
//        unregisterManagers();
    }

//    private void checkForCrashes() {
//        CrashManager.register(this, "a26d924cbc6c40239cd7041c45ed8b33");
//        Log.d("crash", "c");
//    }
//
//    private void checkForUpdates() {
//        // Remove this for store builds!
//        UpdateManager.register(this);
//        Log.d("crash", "c2");
//    }
//
//    private void unregisterManagers() {
//        UpdateManager.unregister();
//        Log.d("crash", "c3");
//    }

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
                case R.id.pointbtn:
                    Intent i6 = new Intent(MainActivity.this, Point.class);
                    startActivity(i6);
                    break;

                default: // not RAMBO

            }
    }

}