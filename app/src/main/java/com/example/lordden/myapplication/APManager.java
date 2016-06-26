package com.example.lordden.myapplication;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;

/**
 * Created by lordden on 26/6/16.
 */
public class APManager {

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

}
