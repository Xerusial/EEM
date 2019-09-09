package edu.hm.eem_host.view;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.IOException;
import java.net.ServerSocket;

import edu.hm.eem_host.R;
import edu.hm.eem_library.model.ClientItemViewModel;
import edu.hm.eem_host.net.HostProtocolManager;
import edu.hm.eem_host.net.HostServiceManager;
import edu.hm.eem_library.net.ProtocolHandler;
import edu.hm.eem_library.net.SignalPacket;
import edu.hm.eem_library.net.WIFIANDLOCATIONCHECKER;
import edu.hm.eem_library.net.HotspotManager;
import edu.hm.eem_library.view.AbstractMainActivity;
import edu.hm.eem_library.view.ItemListFragment;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class LockActivity extends AppCompatActivity
        implements WIFIANDLOCATIONCHECKER.onWifiAndLocationEnabledListener,
        HotspotManager.OnHotspotEnabledListener,
        ItemListFragment.OnListFragmentPressListener,
        NsdManager.RegistrationListener {
    private static final String SHOWCASE_ID = "LockActivity";
    private static final String CHANNEL_ID = "student_activity";
    private HotspotManager hotspotManager;
    private ConnectivityManager cm;
    private LocationManager lm;
    private NotificationManager nm;
    private TextView netName, netPw, wifiText;
    private Switch swStartService, swUseHotspot, swLock;
    private HostProtocolManager hostProtocolManager = null;
    private HostServiceManager hostServiceManager = null;
    private ClientItemViewModel model;
    private LockHandler handler;
    private View hotspotCredentials;
    private CheckBox cbServiceRunning;
    private boolean locked;

    private enum ProtocolTerminationReason {
        EXIT, CHANGE_CONNECTION
    }

    public class LockHandler extends Handler implements ProtocolHandler {
        private int id = 0;

        private LockHandler(Looper looper) {
            super(looper);
        }

        public void notifyStudentLeft(String name) {
                this.post(() -> {
                    if (locked) {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(LockActivity.this, CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_student_black)
                                .setContentTitle(getString(R.string.student_left, name))
                                .setStyle(new NotificationCompat.BigTextStyle()
                                        .bigText(getString(R.string.student_left_text, name)))
                                .setCategory(NotificationCompat.CATEGORY_MESSAGE);
                        nm.notify(++id, builder.build());
                        model.getLivedata().disconnected(name);
                    } else
                        model.getLivedata().remove(name,false);
                });
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
        String examName = getIntent().getStringExtra(AbstractMainActivity.EXAMNAME_FIELD);
        netName = findViewById(R.id.net_name);
        netPw = findViewById(R.id.net_pw);
        wifiText = findViewById(R.id.wifi);
        swStartService = findViewById(R.id.sw_start_service);
        swUseHotspot = findViewById(R.id.sw_use_hotspot);
        swLock = findViewById(R.id.sw_lock_students);
        hotspotCredentials = findViewById(R.id.hotspot_credentials);
        cbServiceRunning = findViewById(R.id.cb_service_running);
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        nm = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        createNotificationChannel();
        hotspotManager = new HotspotManager(wm, this);
        model = ViewModelProviders.of(this).get(ClientItemViewModel.class);
        handler = new LockHandler(Looper.getMainLooper());
        ((Toolbar) findViewById(R.id.toolbar)).setTitle(examName);
        swStartService.setOnClickListener(v -> {
            if (swStartService.isChecked()) {
                prepareService();
            } else {
                quitService();
                switchSetEnabled(swUseHotspot, true);
                switchSetEnabled(swLock, false);
                cbServiceRunning.setVisibility(View.GONE);
            }
        });
        swUseHotspot.setOnClickListener(v -> {
            if(model.getLivedata().getValue().size() != 0)
                showExitDialog(ProtocolTerminationReason.CHANGE_CONNECTION);
            else
                changeHotSpot(swUseHotspot.isChecked());
        });
        switchSetEnabled(swLock, false);
        swLock.setOnClickListener(v -> {
            if (swLock.isChecked()) {
                if (model.getLivedata().getSelectionCount() != model.getLivedata().getValue().size())
                    showDialog(R.string.dialog_still_unchecked_documents, (dialog, id) -> lock(true), dialog -> swLock.setChecked(false));
                else
                    lock(true);
            } else {
                lock(false);
            }
        });
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String profName = sharedPref.getString(getString(R.string.preferences_username), "Username");
        hostProtocolManager = new HostProtocolManager(this, model.getLivedata(), handler, examName);
        hostServiceManager = new HostServiceManager(this, profName, hostProtocolManager);
        tutorial();
    }

    private void lock(boolean enable) {
        if (enable) {
            quitService();
            swStartService.setChecked(false);
            cbServiceRunning.setVisibility(View.GONE);
            hostProtocolManager.sendSignal(SignalPacket.Signal.LOCK, HostProtocolManager.TO_ALL);
            switchSetEnabled(swStartService, false);
            model.getLivedata().setSelected();
            locked = true;
        } else {
            model.getLivedata().clearDisconnected(false);
            switchSetEnabled(swUseHotspot, true);
            switchSetEnabled(swStartService, true);
            switchSetEnabled(swLock, false);
            locked = false;
        }
        hotspotCredentials.setVisibility(enable ? View.GONE : View.VISIBLE);
    }

    private void showDialog(@StringRes int message, DialogInterface.OnClickListener click, DialogInterface.OnCancelListener cancel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton(R.string.string_continue, click)
                .setNegativeButton(android.R.string.cancel, (dialog, id) -> dialog.cancel())
                .setOnCancelListener(cancel)
                .show();
    }

    private void changeHotSpot(boolean enable) {
        quitProtocol();
        quitService();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (enable) {
                WIFIANDLOCATIONCHECKER.checkLocation(LockActivity.this, lm, true);
                switchSetEnabled(swStartService, false);
            } else {
                hotspotManager.turnOffHotspot();
            }
        } else {
            if(enable) {
                showDialog(R.string.dialog_hotspot_api23, (dialogInterface, i) -> {
                    final Intent intent = new Intent(Intent.ACTION_MAIN, null);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    final ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.TetherSettings");
                    intent.setComponent(cn);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }, dialog -> swUseHotspot.setChecked(false));
            }
        }
    }

    private void prepareService() {
        if (!swUseHotspot.isChecked())
            WIFIANDLOCATIONCHECKER.checkWifi(LockActivity.this, cm, true);
        else
            onWifiEnabled();
    }

    private void switchSetEnabled(Switch sw, boolean enable) {
        sw.setEnabled(enable);
        if (sw == swUseHotspot)
            wifiText.setAlpha(enable ? 1.0f : 0.5f);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            hotspotManager.turnOffHotspot();
        quitProtocol();
        quitService();
    }

    private void quitProtocol() {
        if (hostProtocolManager != null) {
            hostProtocolManager.quit();
            model.getLivedata().clean(true);
        }
    }

    private void quitService() {
        if (hostServiceManager != null) {
            hostServiceManager.quit();
        }
    }

    @Override
    public void onBackPressed() {
        if (!model.getLivedata().isEmpty())
            showExitDialog(ProtocolTerminationReason.EXIT);
        else
            super.onBackPressed();
    }

    private void showExitDialog(ProtocolTerminationReason reason) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_exit_teacher)
                .setPositiveButton(android.R.string.yes, (dialog, id) -> {
                    switch (reason) {
                        case EXIT:
                            super.onBackPressed();
                            break;
                        case CHANGE_CONNECTION:
                            changeHotSpot(swUseHotspot.isChecked());
                            break;
                    }
                })
                .setNegativeButton(android.R.string.no, (dialog, id) -> dialog.cancel())
                .setOnCancelListener(dialog -> {
                    if (reason == ProtocolTerminationReason.CHANGE_CONNECTION)
                        swUseHotspot.setChecked(!swUseHotspot.isChecked());
                });
        builder.show();
    }

    @Override
    public void onWifiEnabled() {
        try {
            ServerSocket serverSocket = new ServerSocket(0);
            hostServiceManager.init(serverSocket);
            switchSetEnabled(swUseHotspot, false);
            cbServiceRunning.setVisibility(View.VISIBLE);
            cbServiceRunning.setChecked(false);
            switchSetEnabled(swUseHotspot, false);
            switchSetEnabled(swLock, true);
        } catch (IOException e) {
            e.printStackTrace();
            swStartService.setChecked(false);
        }
    }

    @Override
    public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
        hostServiceManager.quit();
        cbServiceRunning.setVisibility(View.GONE);
        swStartService.setChecked(false);
    }

    @Override
    public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {

    }

    @Override
    public void onServiceRegistered(NsdServiceInfo serviceInfo) {
        handler.post(() -> cbServiceRunning.setChecked(true));
    }

    @Override
    public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onLocationEnabled() {
        hotspotManager.turnOnHotspot();
    }

    @Override
    public void onNotEnabled() {
        swUseHotspot.setChecked(false);
        swStartService.setChecked(false);
        swLock.setChecked(false);
        cbServiceRunning.setVisibility(View.GONE);
    }

    @Override
    public void OnHotspotEnabled(boolean enabled, @Nullable WifiConfiguration wifiConfiguration) {
        if (enabled) {
            if (wifiConfiguration != null) {
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
                WIFIANDLOCATIONCHECKER.checkWifi(this, cm, false);
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

    private void tutorial(){
        ShowcaseConfig config = new ShowcaseConfig();

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, SHOWCASE_ID);

        sequence.setConfig(config);

        sequence.addSequenceItem(swUseHotspot,
                getString(edu.hm.eem_library.R.string.tutorial_hotspot_switch), getString(android.R.string.ok));

        sequence.addSequenceItem(swStartService,
                getString(edu.hm.eem_library.R.string.tutorial_service_switch), getString(android.R.string.ok));

        sequence.addSequenceItem(findViewById(R.id.header_con_devices),
                getString(edu.hm.eem_library.R.string.tutorial_connected_devices), getString(android.R.string.ok));

        sequence.addSequenceItem(swLock,
                getString(edu.hm.eem_library.R.string.tutorial_lock_switch), getString(android.R.string.ok));

        sequence.start();
    }
}
