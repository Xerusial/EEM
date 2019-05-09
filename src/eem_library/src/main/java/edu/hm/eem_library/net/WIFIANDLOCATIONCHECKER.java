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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import edu.hm.eem_library.R;

public final class WIFIANDLOCATIONCHECKER {
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION = 1;
    private static final int WIFI_OR_LOCATION_REQUEST = 1;

    public interface onWifiAndLocationEnabledListener {
        void onWifiAndLocationEnabled();
    }

    public static <T extends AppCompatActivity & onWifiAndLocationEnabledListener> void check(@NonNull T apl, @NonNull WifiManager wm, @NonNull LocationManager lm) {
        if (apl.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            apl.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION);
        } else if (!isLocationEnabled(apl, lm)) {
            showWifiOrLocationDialog(apl, null, lm);
        } else if (!wm.isWifiEnabled()) {
            showWifiOrLocationDialog(apl, wm, null);
        } else {
            apl.onWifiAndLocationEnabled();
        }
    }

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

    private static <T extends AppCompatActivity> void showWifiOrLocationDialog(@NonNull final T apl, @Nullable WifiManager wm, @Nullable LocationManager lm) {
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

        builder.setPositiveButton(apl.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent;
                if (wifi)
                    //TODO Make it work
                    intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                else
                    intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                apl.startActivityForResult(intent, WIFI_OR_LOCATION_REQUEST);
            }
        });
        builder.setNegativeButton(apl.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                apl.finish();
            }
        });
        builder.show();
    }
}
