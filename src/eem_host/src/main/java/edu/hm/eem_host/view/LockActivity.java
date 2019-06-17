package edu.hm.eem_host.view;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProviders;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.IOException;
import java.net.ServerSocket;

import edu.hm.eem_host.R;
import edu.hm.eem_library.model.DeviceViewModel;
import edu.hm.eem_host.net.HostProtocolManager;
import edu.hm.eem_host.net.HostServiceManager;
import edu.hm.eem_library.model.ProtocolHandler;
import edu.hm.eem_library.net.WIFIANDLOCATIONCHECKER;
import edu.hm.eem_library.net.HotspotManager;
import edu.hm.eem_library.view.AbstractMainActivity;
import edu.hm.eem_library.view.ItemListFragment;

public class LockActivity extends AppCompatActivity
        implements WIFIANDLOCATIONCHECKER.onWifiAndLocationEnabledListener,
        HotspotManager.OnHotspotEnabledListener,
        ItemListFragment.OnListFragmentPressListener{
    private static final String CHANNEL_ID = "student_activity";
    private HotspotManager hotspotManager;
    private WifiManager wm;
    private LocationManager lm;
    private NotificationManager nm;
    private TextView netName;
    private TextView netPw;
    private Switch swStartService;
    private CheckBox cbUseHotspot;
    private HostProtocolManager hostProtocolManager;
    private HostServiceManager hostServiceManager;
    private DeviceViewModel model;
    private String examName;
    private LockHandler handler;

    public class LockHandler extends Handler implements ProtocolHandler {
        private LockHandler(Looper looper){
            super(looper);
        }

        public void notifyStudentLeft(String name){
            Notification.Builder builder = new Notification.Builder(LockActivity.this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_student_black)
                    .setContentTitle(getString(R.string.student_left))
                    .setStyle(new Notification.BigTextStyle()
                            .setSummaryText(name)
                            .bigText(getString(R.string.student_left_text1) + ' ' + name + ' ' + getString(R.string.student_left_text2)))
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE);
            nm.notify(0, builder.build());
        }

        @Override
        public void putToast(int resId) {
            this.post(() -> Toast.makeText(LockActivity.this.getApplicationContext(), resId, Toast.LENGTH_SHORT).show());
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);
        examName = getIntent().getStringExtra(AbstractMainActivity.EXAMNAME_FIELD);
        netName = findViewById(R.id.net_name);
        netPw = findViewById(R.id.net_pw);
        swStartService = findViewById(R.id.sw_start_service);
        cbUseHotspot = findViewById(R.id.cb_use_hotspot);
        swStartService.setOnCheckedChangeListener((buttonView, isChecked) -> {
            boolean useHotspot = cbUseHotspot.isChecked();
            if (isChecked) {
                if (useHotspot)
                    WIFIANDLOCATIONCHECKER.checkLocation(LockActivity.this, lm, true);
                else
                    WIFIANDLOCATIONCHECKER.checkWifi(LockActivity.this, wm, true);
            } else {
                quitService();
                quitProtocol();
                if (useHotspot) hotspotManager.turnOffHotspot();
                checkboxSetEnabled(true);
            }
        });
        wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        nm = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();
        hotspotManager = new HotspotManager(wm, this);
        model = ViewModelProviders.of(this).get(DeviceViewModel.class);
        handler = new LockHandler(Looper.getMainLooper());
        ((Toolbar)findViewById(R.id.toolbar)).setTitle(examName);
    }

    private void checkboxSetEnabled(boolean enable) {
        cbUseHotspot.setEnabled(enable);
        cbUseHotspot.setAlpha(enable ? 1.0f : 0.5f);
    }

    private void startService() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String profName = sharedPref.getString(getString(R.string.preferences_username), "Username");
        try {
            ServerSocket serverSocket = new ServerSocket(0);
            hostServiceManager = new HostServiceManager(this, serverSocket, profName);
            hostProtocolManager = new HostProtocolManager(LockActivity.this, serverSocket, model.getLivedata(), handler, examName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void quitProtocol() {
        hostProtocolManager.quit();
    }

    private void quitService() {
        hostServiceManager.quit();
    }

    @Override
    public void onWifiEnabled() {
        startService();
        checkboxSetEnabled(false);
    }

    @Override
    public void onLocationEnabled() {
        hotspotManager.turnOnHotspot();
        checkboxSetEnabled(false);
    }

    @Override
    public void onNotEnabled() {
        swStartService.setChecked(false);
    }

    @Override
    public void OnHotspotEnabled(boolean enabled, @Nullable WifiConfiguration wifiConfiguration) {
        if (enabled) {
            netName.setText(wifiConfiguration.SSID);
            netPw.setText(wifiConfiguration.preSharedKey);
            startService();
        } else {
            netName.setText(getString(R.string.blank));
            netPw.setText(getString(R.string.blank));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case WIFIANDLOCATIONCHECKER.PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    WIFIANDLOCATIONCHECKER.checkLocation(this, lm, false);
                else finish();
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        switch (requestCode) {
            case WIFIANDLOCATIONCHECKER.WIFI_REQUEST:
                WIFIANDLOCATIONCHECKER.checkWifi(this, wm, false);
                break;
            case WIFIANDLOCATIONCHECKER.LOCATION_REQUEST:
                WIFIANDLOCATIONCHECKER.checkLocation(this, lm, false);
                break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onListFragmentPress(int index) {
        hostProtocolManager.sendLightHouse(index);
    }
}
