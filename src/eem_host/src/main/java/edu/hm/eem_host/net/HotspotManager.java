package edu.hm.eem_host.net;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;

import static android.support.constraint.Constraints.TAG;

public class HotspotManager {
    private final WifiManager wifiManager;
    private WifiManager.LocalOnlyHotspotReservation mReservation;

    //call with Hotspotmanager(getApplicationContext().getSystemService(Context.WIFI_SERVICE))
    public HotspotManager(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }

    private WifiConfiguration setHotspotParams(WifiConfiguration config){
        config.SSID = "ExamNet";
        config.preSharedKey = "lockExam";
        return config;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void turnOnHotspot() {
        wifiManager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {

            @Override
            public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                super.onStarted(reservation);
                Log.d(TAG, "Wifi Hotspot is on now");
                mReservation = reservation;

                wifiManager.updateNetwork(setHotspotParams(reservation.getWifiConfiguration()));
            }

            @Override
            public void onStopped() {
                super.onStopped();
                Log.d(TAG, "onStopped: ");
            }

            @Override
            public void onFailed(int reason) {
                super.onFailed(reason);
                Log.d(TAG, "onFailed: ");
            }
        }, new Handler());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void turnOffHotspot() {
        if (mReservation != null) {
            mReservation.close();
        }
    }
}
