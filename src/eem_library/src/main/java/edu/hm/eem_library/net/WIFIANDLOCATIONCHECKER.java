package edu.hm.eem_library.net;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import edu.hm.eem_library.R;

public final class WIFIANDLOCATIONCHECKER {
    public static final int PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION = 1;
    public static final int WIFI_REQUEST = 1;
    public static final int LOCATION_REQUEST = 2;

    /** Checks if wifi and location services are online for hotspot creation.
     *  Please implement the onRequestPermissionResult and onActivityResult in the using class.
     */
    public interface onWifiAndLocationEnabledListener {
        void onWifiEnabled();
        void onLocationEnabled();
        void onNotEnabled();
    }

    public static <T extends AppCompatActivity & onWifiAndLocationEnabledListener> void checkWifi(@NonNull T apl, @NonNull WifiManager wm, boolean firstCheck) {
        if (!wm.isWifiEnabled()) {
            if(firstCheck) showWifiOrLocationDialog(apl, wm, null);
            else apl.onNotEnabled();
        } else {
            apl.onWifiEnabled();
        }
    }

    public static <T extends AppCompatActivity & onWifiAndLocationEnabledListener> void checkLocation(@NonNull T apl, @NonNull LocationManager lm, boolean firstCheck) {
        if (apl.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if(firstCheck) apl.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION);
            else apl.onNotEnabled();
        } else if (!isLocationEnabled(apl, lm)) {
            if(firstCheck) showWifiOrLocationDialog(apl, null, lm);
            else apl.onNotEnabled();
        } else {
            apl.onLocationEnabled();
        }
    }

    @SuppressWarnings("deprecation")
    private static <T extends AppCompatActivity> boolean isLocationEnabled(@NonNull T apl, @NonNull LocationManager lm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is new method provided in API 28
            return lm.isLocationEnabled();
        } else {
            // This is Deprecated in API 28
            int mode = Settings.Secure.getInt(apl.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return (mode != Settings.Secure.LOCATION_MODE_OFF);
        }
    }

    private static <T extends AppCompatActivity & onWifiAndLocationEnabledListener> void showWifiOrLocationDialog(@NonNull final T apl, @Nullable WifiManager wm, @Nullable LocationManager lm) {
        final boolean wifi;
        if (lm != null)
            wifi = false;
        else if (wm != null)
            wifi = true;
        else
            throw new IllegalArgumentException("One of both managers musst be non null!");

        AlertDialog.Builder builder = new AlertDialog.Builder(apl);
        if (wifi)
            builder.setMessage(apl.getString(R.string.alert_wifi_for_app));
        else
            builder.setMessage(apl.getString(R.string.alert_location_for_scan));

        builder.setPositiveButton(apl.getString(android.R.string.ok), (dialog, which) -> {
            Intent intent;
            int request;
            if (wifi) {
                intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                request = WIFI_REQUEST;
            }
            else {
                intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                request = LOCATION_REQUEST;
            }
            apl.startActivityForResult(intent, request);
        });
        builder.setNegativeButton(apl.getString(android.R.string.cancel), (dialog, which) -> {
            dialog.cancel();
            apl.onNotEnabled();
        });
        builder.show();
    }
}
