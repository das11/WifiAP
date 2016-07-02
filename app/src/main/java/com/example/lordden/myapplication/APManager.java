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

    public static void configCred(Context context){

        WifiManager WifiManager = (WifiManager)context.getSystemService(context.WIFI_SERVICE);
        WifiConfiguration config = new WifiConfiguration();

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

        try{
            Log.d("Cliop","2");

            Method setAPMethod = WifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            boolean APStatus = (Boolean)setAPMethod.invoke(WifiManager, config, true);

            Method isAPEnabledMethod = WifiManager.getClass().getMethod("isWifiApEnabled");

            Log.d("Cliop","3");
            if (isAPon(context)){
                WifiManager.setWifiEnabled(false);
            }

            Method getAPState = WifiManager.getClass().getMethod("getWifiApState");
            int APState = (Integer)getAPState.invoke(WifiManager);

            Method getAPConfigMethod = WifiManager.getClass().getMethod("getWifiApConfiguration");

            config = (WifiConfiguration)getAPConfigMethod.invoke(WifiManager);

            Log.d("Cliop","5");
            Log.d("Client:", "\nSSID:" + config.SSID + "\nPass:" + config.preSharedKey);

        }catch(Exception e){
            e.printStackTrace();
        }

    }

}
