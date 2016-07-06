package com.example.lordden.myapplication;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Created by kdas on 26/6/16.
 */
public class APManager {

    static String ssid, pass;

    public static boolean isAPon(Context context){

        WifiManager wifimanager = (WifiManager)context.getSystemService(context.WIFI_SERVICE);
        try{
            Method method = wifimanager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);

            return (Boolean) method.invoke(wifimanager);
        }
        catch (Throwable e){
            e.printStackTrace();
        }

        return false;
    }

    public static boolean isWifion(Context context){
        WifiManager wm = (WifiManager)context.getSystemService(context.WIFI_SERVICE);
        if (wm.isWifiEnabled()){
            return true;
        }
        else
            return false;
    }

//    void perm(Context c){
//        if (ActivityCompat.shouldShowRequestPermissionRationale(c,
//                Manifest.permission.WRITE_SETTINGS)){
//
//        }else {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.WRITE_SETTINGS},
//                    121);
//        }
//    }

    public static boolean configAPState(Context context){
        WifiManager wifiManager = (WifiManager)context.getSystemService(context.WIFI_SERVICE);
        WifiConfiguration wificonfig = null;

        Log.d("test", "config");

        try{
            if(isAPon(context)){
                wifiManager.setWifiEnabled(false);
                Log.d("test", "config2");
            }
            if (isWifion(context)){
                wifiManager.setWifiEnabled(false);
            }

            Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(wifiManager, wificonfig, !isAPon(context));

            Log.d("test", "config3");

            return true;
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return false;
    }

    public static void configWifi(Context context){
        WifiManager wman = (WifiManager)context.getSystemService(context.WIFI_SERVICE);
        wman.setWifiEnabled(true);
    }

    public static void configCred(Context context){

      //  WifiManager WifiManager = (WifiManager)context.getSystemService(context.WIFI_SERVICE);

        try{
            Log.d("Cliop","2");

//            Method getConfig = WifiManager.getClass().getMethod("getWifiApConfiguration");
//            WifiConfiguration config = (WifiConfiguration)getConfig.invoke(WifiManager);

            WifiManager mWifiManager= (WifiManager) context.getSystemService(context.WIFI_SERVICE);
            Method getConfigMethod = mWifiManager.getClass().getMethod("getWifiApConfiguration");
            WifiConfiguration config= (WifiConfiguration) getConfigMethod.invoke(mWifiManager);

            //config.SSID = "popopopo";

            Method setConfigMethod = mWifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
            setConfigMethod.invoke(mWifiManager, config);

            Log.d("Cliop","1");

            config.SSID = "Test AP";
            config.preSharedKey = "\"passpass\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);

            Method setAPMethod = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            boolean APStatus = (Boolean)setAPMethod.invoke(mWifiManager, config, true);


            Method isAPEnabledMethod = mWifiManager.getClass().getMethod("isWifiApEnabled");

            Log.d("Cliop","3");
            if (isAPon(context)){
                mWifiManager.setWifiEnabled(false);
            }

            Method getAPState = mWifiManager.getClass().getMethod("getWifiApState");
            int APState = (Integer)getAPState.invoke(mWifiManager);

            Method getAPConfigMethod = mWifiManager.getClass().getMethod("getWifiApConfiguration");

            config = (WifiConfiguration)getAPConfigMethod.invoke(mWifiManager);

            Log.d("Cliop","5");
            Log.d("Client:", "\nSSID:" + config.SSID + "\nPass:" + config.preSharedKey);

            ssid = config.SSID.toString();
            pass = config.preSharedKey.toString();

            Log.d("sssss ap", ssid);

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public static String ssid(){
        return ssid;
    }


    public static String pass(){
        return pass;
    }

}
