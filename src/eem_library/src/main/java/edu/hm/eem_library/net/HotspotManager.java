package edu.hm.eem_library.net;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * Manager class for a {@link android.net.wifi.WifiManager.LocalOnlyHotspotReservation}.
 * This is a hotspot type, which gives no access to the mobile data of the host device,
 * hence is better suited for the application. But it is only available on Android 8 (Oreo)
 * and higher.
 */
public class HotspotManager {
    private final WifiManager wifiManager;
    private final OnHotspotEnabledListener onHotspotEnabledListener;
    private WifiManager.LocalOnlyHotspotReservation mReservation;

    /**
     * Constructor
     *
     * @param wifiManager              instance of the systems wifi manager
     * @param onHotspotEnabledListener a listener for callbacks if the hotspot starts successfully
     */
    public HotspotManager(WifiManager wifiManager, OnHotspotEnabledListener onHotspotEnabledListener) {
        this.wifiManager = wifiManager;
        this.onHotspotEnabledListener = onHotspotEnabledListener;
    }

    /**
     * turn the private hotspot on
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void turnOnHotspot() {
        wifiManager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {

            @Override
            public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                super.onStarted(reservation);
                mReservation = reservation;
                onHotspotEnabledListener.OnHotspotEnabled(true, mReservation.getWifiConfiguration());
            }

            @Override
            public void onFailed(int reason) {
                super.onFailed(reason);
            }
        }, new Handler());
    }

    /**
     * Turn the private hotspot off
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void turnOffHotspot() {
        if (mReservation != null) {
            mReservation.close();
            onHotspotEnabledListener.OnHotspotEnabled(false, null);
        }
    }

    /**
     * Listener for callbacks if the hotspot starts successfully
     */
    public interface OnHotspotEnabledListener {
        void OnHotspotEnabled(boolean enabled, @Nullable WifiConfiguration wifiConfiguration);
    }
}
