package edu.hm.eem_host.view;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
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
    private TextView netName, netPw, wifiText;
    private Switch swStartService, swUseHotspot;
    private HostProtocolManager hostProtocolManager = null;
    private HostServiceManager hostServiceManager = null;
    private DeviceViewModel model;
    private String examName;
    private LockHandler handler;

    private enum ProtocolTerminationReason{
        EXIT, START_SERVICE, HOTSPOT_ON, HOTSPOT_OFF
    }

    public class LockHandler extends Handler implements ProtocolHandler {
        private int id = 0;
        private LockHandler(Looper looper){
            super(looper);
        }

        public void notifyStudentLeft(String name){
            Notification.Builder builder = new Notification.Builder(LockActivity.this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_student_black)
                    .setContentTitle(getString(R.string.student_left))
                    .setStyle(new Notification.BigTextStyle()
                            .setSummaryText(name)
                            .bigText(getString(R.string.student_left_text, name)))
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE);
            nm.notify(++id, builder.build());
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
        wifiText = findViewById(R.id.wifi);
        swStartService = findViewById(R.id.sw_start_service);
        swUseHotspot = findViewById(R.id.sw_use_hotspot);
        wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        nm = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();
        hotspotManager = new HotspotManager(wm, this);
        model = ViewModelProviders.of(this).get(DeviceViewModel.class);
        handler = new LockHandler(Looper.getMainLooper());
        ((Toolbar)findViewById(R.id.toolbar)).setTitle(examName);
        swStartService.setOnCheckedChangeListener((buttonView, isChecked) -> {
            quitService();
            if (isChecked) {
                if(!model.getLivedata().isEmpty()) {
                    showExitDialog(ProtocolTerminationReason.START_SERVICE);
                } else {
                    prepareService();
                }
            } else {
                switchSetEnabled(swUseHotspot, true);
            }
        });
        swUseHotspot.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            if(model.getLivedata().isEmpty())
                changeHotSpot(isChecked);
            else
                showExitDialog(isChecked?ProtocolTerminationReason.HOTSPOT_ON:ProtocolTerminationReason.HOTSPOT_OFF);
        }));
    }

    private void changeHotSpot(boolean enable){
        quitProtocol();
        quitService();
        if(enable){
            WIFIANDLOCATIONCHECKER.checkLocation(LockActivity.this, lm, true);
            switchSetEnabled(swStartService, false);
        } else {
            hotspotManager.turnOffHotspot();
        }
    }

    private void prepareService(){
        quitProtocol();
        if (!swUseHotspot.isChecked())
            WIFIANDLOCATIONCHECKER.checkWifi(LockActivity.this, wm, true);
        else
            onWifiEnabled();
    }

    private void switchSetEnabled(Switch sw, boolean enable) {
        sw.setEnabled(enable);
        sw.setAlpha(enable ? 1.0f : 0.5f);
        if(sw == swUseHotspot)
            wifiText.setAlpha(enable ? 1.0f : 0.5f);
    }

    @Override
    protected void onStop() {
        super.onStop();
        hotspotManager.turnOffHotspot();
        quitProtocol();
        quitService();
    }

    private void quitProtocol() {
        if(hostProtocolManager!=null) {
            hostProtocolManager.quit();
            hostProtocolManager = null;
            model.getLivedata().clean(true);
        }
    }

    private void quitService() {
        if(hostServiceManager!=null) {
            hostServiceManager.quit();
            hostServiceManager = null;
        }
    }

    @Override
    public void onBackPressed() {
        if(!model.getLivedata().isEmpty())
            showExitDialog(ProtocolTerminationReason.EXIT);
        else
            super.onBackPressed();
    }

    private void showExitDialog(ProtocolTerminationReason reason){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_exit_teacher)
                .setPositiveButton(android.R.string.yes, (dialog, id) -> {
                    switch (reason){
                        case EXIT:
                            super.onBackPressed();
                            break;
                        case START_SERVICE:
                            prepareService();
                            break;
                        case HOTSPOT_ON:
                            changeHotSpot(true);
                            break;
                        case HOTSPOT_OFF:
                            changeHotSpot(false);
                            break;
                    }
                })
                .setNegativeButton(android.R.string.no, (dialog, id) -> {
                    if(reason==ProtocolTerminationReason.START_SERVICE)
                        swStartService.setChecked(false);
                    dialog.cancel();
                });
        builder.show();
    }

    @Override
    public void onWifiEnabled() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String profName = sharedPref.getString(getString(R.string.preferences_username), "Username");
        try {
            ServerSocket serverSocket = new ServerSocket(0);
            hostProtocolManager = new HostProtocolManager(LockActivity.this, model.getLivedata(), handler, examName);
            hostServiceManager = new HostServiceManager(this, serverSocket, profName, hostProtocolManager);
            switchSetEnabled(swUseHotspot,false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationEnabled() {
        hotspotManager.turnOnHotspot();
    }

    @Override
    public void onNotEnabled() {
        swUseHotspot.setChecked(false);
        swStartService.setChecked(false);
    }

    @Override
    public void OnHotspotEnabled(boolean enabled, @Nullable WifiConfiguration wifiConfiguration) {
        if (enabled) {
            if(wifiConfiguration != null) {
                netName.setText(wifiConfiguration.SSID);
                netPw.setText(wifiConfiguration.preSharedKey);
                switchSetEnabled(swStartService, true);
            }
        } else {
            netName.setText(getString(R.string.blank));
            netPw.setText(getString(R.string.blank));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,@NonNull int[] grantResults) {
        if (requestCode == WIFIANDLOCATIONCHECKER.PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                WIFIANDLOCATIONCHECKER.checkLocation(this, lm, true);
            else onNotEnabled();
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
