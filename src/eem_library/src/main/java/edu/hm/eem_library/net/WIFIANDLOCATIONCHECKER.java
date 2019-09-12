package edu.hm.eem_library.net;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import edu.hm.eem_library.R;

/**
 * Static class with utility functions to check if wifi or location functionality is turned on.
 * Location services are needed for the private hotspot.
 */
public final class WIFIANDLOCATIONCHECKER {
    public static final int PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION = 1;
    public static final int WIFI_REQUEST = 1;
    public static final int LOCATION_REQUEST = 2;

    private WIFIANDLOCATIONCHECKER() {
    }

    /**
     * Check if wifi is enabled
     *
     * @param apl        calling activity
     * @param cm         instance of system's connectivity manager
     * @param firstCheck is this the first time we are checking? Needed if permission was not granted on first try
     * @param <T>        make sure the calling activity has the right callback listeners defined
     */
    public static <T extends AppCompatActivity & onWifiAndLocationEnabledListener> void checkWifi(@NonNull T apl, @NonNull ConnectivityManager cm, boolean firstCheck) {
        @SuppressLint("MissingPermission") NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null && ni.isConnected()) {
            apl.onWifiEnabled();
        } else {
            if (firstCheck) showWifiOrLocationDialog(apl, cm, null);
            else apl.onNotEnabled();
        }
    }

    /**
     * Check if location services are enabled (no GPS needed, just network location)
     *
     * @param apl        calling activity
     * @param lm         instance of system's location manager
     * @param firstCheck is this the first time we are checking? Needed if permission was not granted on first try
     * @param <T>        make sure the calling activity has the right callback listeners defined
     */
    public static <T extends AppCompatActivity & onWifiAndLocationEnabledListener> void checkLocation(@NonNull T apl, @NonNull LocationManager lm, boolean firstCheck) {
        if (apl.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (firstCheck)
                apl.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION);
            else apl.onNotEnabled();
        } else if (!isLocationEnabled(apl, lm)) {
            if (firstCheck) showWifiOrLocationDialog(apl, null, lm);
            else apl.onNotEnabled();
        } else {
            apl.onLocationEnabled();
        }
    }

    /**
     * Used to make correct checks on different Android versions
     *
     * @param apl calling activity
     * @param lm  instance of system's location manager
     * @param <T> make sure its an activity calling
     * @return are location services up?
     */
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

    /**
     * If wifi or location are not up, show the user a dialog guiding him to the right settings page
     *
     * @param apl calling activity
     * @param cm  instance of system's connectivity manager
     * @param lm  instance of system's location manager
     * @param <T> make sure the calling activity has the right callback listeners defined
     */
    private static <T extends AppCompatActivity & onWifiAndLocationEnabledListener> void showWifiOrLocationDialog(@NonNull final T apl, @Nullable ConnectivityManager cm, @Nullable LocationManager lm) {
        final boolean wifi;
        if (lm != null)
            wifi = false;
        else if (cm != null)
            wifi = true;
        else
            throw new IllegalArgumentException("One of both managers musst be non null!");

        AlertDialog.Builder builder = new AlertDialog.Builder(apl);
        if (wifi)
            builder.setMessage(apl.getString(R.string.dialog_wifi_for_app));
        else
            builder.setMessage(apl.getString(R.string.dialog_location_for_scan));

        builder.setPositiveButton(apl.getString(android.R.string.ok), (dialog, which) -> {
            Intent intent;
            int request;
            if (wifi) {
                intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                request = WIFI_REQUEST;
            } else {
                intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                request = LOCATION_REQUEST;
            }
            apl.startActivityForResult(intent, request);
        })
                .setNegativeButton(apl.getString(android.R.string.cancel), (dialog, which) -> dialog.cancel())
                .setOnCancelListener(dialog -> apl.onNotEnabled())
                .show();
    }

    /**
     * Interface for callbacks on enabled services
     */
    public interface onWifiAndLocationEnabledListener {
        void onWifiEnabled();

        void onLocationEnabled();

        void onNotEnabled();
    }
}
