package edu.hm.eem_host.view;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
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
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Objects;

import edu.hm.eem_host.R;
import edu.hm.eem_host.net.HostProtocolService;
import edu.hm.eem_host.net.HostServiceManager;
import edu.hm.eem_library.model.ClientItemViewModel;
import edu.hm.eem_library.model.ItemViewModel;
import edu.hm.eem_library.net.HotspotManager;
import edu.hm.eem_library.net.ProtocolHandler;
import edu.hm.eem_library.net.SignalPacket;
import edu.hm.eem_library.net.WIFIANDLOCATIONCHECKER;
import edu.hm.eem_library.view.AbstractMainActivity;
import edu.hm.eem_library.view.ItemListFragment;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

/**
 * Core activity for the host user. Here, logged in clients are listed and can be monitored.
 */
public class LockActivity extends AppCompatActivity
        implements WIFIANDLOCATIONCHECKER.onWifiAndLocationEnabledListener,
        HotspotManager.OnHotspotEnabledListener,
        ItemListFragment.OnListFragmentPressListener,
        NsdManager.RegistrationListener,
        ItemListFragment.ViewmodelFromServiceProvider {
    private static final String SHOWCASE_ID = "LockActivity";
    private HotspotManager hotspotManager;
    private ConnectivityManager cm;
    private LocationManager lm;
    private TextView cntStudents, netName, netPw, wifiText;
    private Switch swStartService, swUseHotspot, swLock;
    private HostProtocolService hostProtocolService = null;
    private HostServiceManager hostServiceManager = null;
    private ClientItemViewModel model;
    private LockHandler handler;
    private View hotspotCredentials;
    private CheckBox cbServiceRunning;
    private String profName, examName;

    /**
     * Init views, managers, the handler, the list viewmodel and the clicklisteners for the switch
     *
     * @param savedInstanceState Android basics
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        examName = getIntent().getStringExtra(AbstractMainActivity.EXAMNAME_FIELD);
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        hotspotManager = new HotspotManager(wm, this);
        handler = new LockHandler(Looper.getMainLooper());
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        profName = sharedPref.getString(getString(R.string.preferences_username), "Username");
        attachToReceiverService();
    }

    private void attachToReceiverService(){
        Intent serviceStarter = new Intent(this, HostProtocolService.class);
        serviceStarter.putExtra(HostProtocolService.EXTRA_EXAM_NAME, examName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceStarter);
        } else {
            startService(serviceStarter);
        }
        bindService(serviceStarter, connection, Context.BIND_ABOVE_CLIENT);
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            HostProtocolService.HostProtocolBinder binder = (HostProtocolService.HostProtocolBinder) service;
            hostProtocolService = binder.getService();
            hostServiceManager = new HostServiceManager(LockActivity.this, profName, hostProtocolService);
            model = hostProtocolService.getViewModelInstance();
            initUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            unbindService(this);
        }
    };

    /**
     * Init the UI after the service has been started, so the ViewModel is available
     */
    private void initUI(){
        setContentView(R.layout.activity_lock);
        cntStudents = findViewById(R.id.number_students);
        netName = findViewById(R.id.net_name);
        netPw = findViewById(R.id.net_pw);
        wifiText = findViewById(R.id.wifi);
        swStartService = findViewById(R.id.sw_start_service);
        swUseHotspot = findViewById(R.id.sw_use_hotspot);
        swLock = findViewById(R.id.sw_lock_students);
        hotspotCredentials = findViewById(R.id.hotspot_credentials);
        cbServiceRunning = findViewById(R.id.cb_service_running);
        ((Toolbar) findViewById(R.id.toolbar)).setTitle(examName);
        switchSetEnabled(swLock, false);
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
            if (Objects.requireNonNull(model.getLivedata().getValue()).size() != 0)
                showExitDialog(ProtocolTerminationReason.CHANGE_CONNECTION);
            else
                changeHotSpot(swUseHotspot.isChecked());
        });
        swLock.setOnClickListener(v -> {
            if (swLock.isChecked()) {
                if (model.getLivedata().getSelectionCount() != Objects.requireNonNull(model.getLivedata().getValue()).size())
                    showDialog(R.string.dialog_still_unchecked_documents, (dialog, id) -> lock(true), dialog -> swLock.setChecked(false));
                else
                    lock(true);
            } else {
                lock(false);
            }
        });
        model.getLivedata().observe(this, selectableSortableItems
                -> cntStudents.setText(getString(R.string.number_of_con_students, model.getLivedata().getValue().size())));
        tutorial();
    }


    /**
     * Call this method enable or disable lock state. In lock state, all documents on student devices,
     * which did not pass the checks are disabled. Also, exits and pulling the notification bar will be monitored.
     *
     * @param enable / disable the lock
     */
    private void lock(boolean enable) {
        if (enable) {
            quitService();
            swStartService.setChecked(false);
            cbServiceRunning.setVisibility(View.GONE);
            hostProtocolService.sendSignal(SignalPacket.Signal.LOCK, HostProtocolService.TO_ALL);
            switchSetEnabled(swStartService, false);
            model.getLivedata().setSelected();
            hostProtocolService.locked = true;
        } else {
            model.getLivedata().clearDisconnected(false);
            switchSetEnabled(swUseHotspot, true);
            switchSetEnabled(swStartService, true);
            switchSetEnabled(swLock, false);
            hostProtocolService.locked = false;
        }
        hotspotCredentials.setVisibility(enable ? View.GONE : View.VISIBLE);
    }

    /**
     * Show a dialog containing a message, which executes a function on ok and another on cancel
     *
     * @param message message to display
     * @param ok      handle to execute on ok
     * @param cancel  handle to execute on cancel
     */
    private void showDialog(@StringRes int message, DialogInterface.OnClickListener ok, DialogInterface.OnCancelListener cancel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton(R.string.string_continue, ok)
                .setNegativeButton(android.R.string.cancel, (dialog, id) -> dialog.cancel())
                .setOnCancelListener(cancel)
                .show();
    }

    /**
     * Enable or disable hotspot connectivity. On Android 8+, use private hotspot, on 6+ classic hotspot
     *
     * @param enable / disable it
     */
    private void changeHotSpot(boolean enable) {
        quitProtocol();
        quitService();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (enable) {
                WIFIANDLOCATIONCHECKER.checkLocation(LockActivity.this, lm, true);
                switchSetEnabled(swStartService, false);
            } else {
                hotspotManager.turnOffHotspot();
            }
        } else {
            if (enable) {
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

    /**
     * In wifi mode, check if wifi is enabled, in hotspot mode use hotspot
     */
    private void prepareService() {
        if (!swUseHotspot.isChecked())
            WIFIANDLOCATIONCHECKER.checkWifi(LockActivity.this, cm, true);
        else
            onWifiEnabled();
    }

    /**
     * Enable/Disable a switch and make it opaque/solid
     *
     * @param sw     Switch
     * @param enable / disable it
     */
    private void switchSetEnabled(Switch sw, boolean enable) {
        sw.setEnabled(enable);
        if (sw == swUseHotspot)
            wifiText.setAlpha(enable ? 1.0f : 0.5f);
    }

    /**
     * Clean up
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            hotspotManager.turnOffHotspot();
        quitProtocol();
        quitService();
    }

    /**
     * Clean up TCP protocol
     */
    private void quitProtocol() {
        if (hostProtocolService != null) {
            hostProtocolService.quit();
            model.getLivedata().clean(true);
        }
    }

    /**
     * Clean up Bonjour service
     */
    private void quitService() {
        if (hostServiceManager != null) {
            hostServiceManager.quit();
        }
    }

    /**
     * Intercept pressing of the back button to prevent the teacher from accidentally terminating
     * the activity
     */
    @Override
    public void onBackPressed() {
        if (!model.getLivedata().isEmpty())
            showExitDialog(ProtocolTerminationReason.EXIT);
        else
            super.onBackPressed();
    }

    /**
     * Dialog shown when accidental exits are recognized or the connection type is about
     * to be changed
     *
     * @param reason from the reason enum
     */
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

    /**
     * If prepareService was successful, this method all set-up which require an active connection:
     * Start Bonjour and TCP Server
     */
    @Override
    public void onWifiEnabled() {
        try {
            ServerSocket serverSocket = new ServerSocket(0);
            hostServiceManager.init(serverSocket);
            cbServiceRunning.setVisibility(View.VISIBLE);
            cbServiceRunning.setChecked(false);
            switchSetEnabled(swUseHotspot, false);
            switchSetEnabled(swLock, true);
        } catch (IOException e) {
            e.printStackTrace();
            swStartService.setChecked(false);
        }
    }

    /**
     * Gets called when Bonjour fails to become ready
     *
     * @param serviceInfo Current service info
     * @param errorCode   Error on which bonjour failed
     */
    @Override
    public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
        hostServiceManager.quit();
        cbServiceRunning.setVisibility(View.GONE);
        swStartService.setChecked(false);
    }

    //Unused, just here to fulfill interface signature
    @Override
    public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {

    }

    /**
     * If bonjour gets enabled successfully, indicate to user
     *
     * @param serviceInfo registered service info
     */
    @Override
    public void onServiceRegistered(NsdServiceInfo serviceInfo) {
        handler.post(() -> cbServiceRunning.setChecked(true));
    }

    //Unused, just here to fulfill interface signature
    @Override
    public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
    }

    /**
     * Is only called if the private Hotspot is used. For this, one needs to enable location services
     * If the user does so, continue
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onLocationEnabled() {
        hotspotManager.turnOnHotspot();
    }

    /**
     * If location or wifi are not enabled by the user, reset switches
     */
    @Override
    public void onNotEnabled() {
        swUseHotspot.setChecked(false);
        swStartService.setChecked(false);
        swLock.setChecked(false);
        cbServiceRunning.setVisibility(View.GONE);
    }

    /**
     * If the private hotspot was successfully started, display credentials to host user
     *
     * @param enabled           hotspot was enabled
     * @param wifiConfiguration contains hotspots ssid and password. They are auto generated
     */
    @Override
    public void OnHotspotEnabled(boolean enabled, @Nullable WifiConfiguration wifiConfiguration) {
        if (enabled) {
            if (wifiConfiguration != null) {
                netName.setText(wifiConfiguration.SSID);
                netPw.setText(wifiConfiguration.preSharedKey);
                switchSetEnabled(swStartService, true);
            }
            hotspotCredentials.setVisibility(View.VISIBLE);
        } else {
            netName.setText(getString(R.string.blank));
            netPw.setText(getString(R.string.blank));
            hotspotCredentials.setVisibility(View.GONE);
        }
    }

    /**
     * Is called when user returns from accepting/ declining permissions
     *
     * @param requestCode  The permissions reques ID
     * @param permissions  permissions that where asked for
     * @param grantResults whether the user granted them
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == WIFIANDLOCATIONCHECKER.PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                WIFIANDLOCATIONCHECKER.checkLocation(this, lm, true);
            else onNotEnabled();
        }
    }

    /**
     * If the user returns from certain external applications, that where launched from intents from
     * this class, this is the target callback
     *
     * @param requestCode that was attached to the starting intent
     * @param resultCode  return value from external application
     * @param data        return data bundle
     */
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

    /**
     * Listener callback if an element in the recyclerview is pressed
     *
     * @param index which index is pressed
     */
    @Override
    public void onListFragmentPress(int index) {
        hostProtocolService.sendLightHouse(index);
    }

    /**
     * CHows a tutorial using a showcaseview
     */
    private void tutorial() {
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

    @Override
    public ItemViewModel getViewModel() {
        return model;
    }

    /**
     * Termination reasons for the accidental exit dialog
     */
    private enum ProtocolTerminationReason {
        EXIT, CHANGE_CONNECTION
    }

    /**
     * Handler that can be called from out-of-UI threads(TCP receivers) and modify UI elements
     */
    public class LockHandler extends Handler implements ProtocolHandler {
        private LockHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void putToast(int resId) {
            this.post(() -> Toast.makeText(LockActivity.this.getApplicationContext(), resId, Toast.LENGTH_SHORT).show());
        }
    }
}
