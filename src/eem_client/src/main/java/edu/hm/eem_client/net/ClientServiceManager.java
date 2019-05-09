package edu.hm.eem_client.net;

import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.view.View;
import android.widget.ProgressBar;

import java.net.InetAddress;

import edu.hm.eem_client.view.LockedActivity;
import edu.hm.eem_library.model.SelectableSortableMapLiveData;
import edu.hm.eem_library.net.ServiceManager;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ClientServiceManager extends ServiceManager {
    public static final String ADDRESS_FIELD = "Address";
    public static final String PORT_FIELD = "Port";
    private NsdManager.DiscoveryListener discoveryListener;
    private NsdManager.ResolveListener resolveListener;
    private final Context context;
    private final ProgressBar pb;
    private final NsdManager nsdm;
    private final SelectableSortableMapLiveData<String, NsdServiceInfo> selectableSortableMapLiveData;

    public ClientServiceManager(Context context, NsdManager nsdm, ProgressBar pb, SelectableSortableMapLiveData<String, NsdServiceInfo> selectableSortableMapLiveData) {
        this.pb = pb;
        this.nsdm = nsdm;
        this.context = context;
        this.selectableSortableMapLiveData = selectableSortableMapLiveData;
        initializeDiscoveryListener();
        initailizeResolverListener();
    }

    private void initailizeResolverListener() {
        resolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {

            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                nsdm.stopServiceDiscovery(discoveryListener);
                int port = serviceInfo.getPort();
                InetAddress host = serviceInfo.getHost();
                Intent intent = new Intent(context, LockedActivity.class);
                intent.putExtra(ADDRESS_FIELD, host);
                intent.putExtra(PORT_FIELD, port);
                context.startActivity(intent);
            }
        };
    }

    public void resolve(NsdServiceInfo service){
        nsdm.resolveService(service, resolveListener);
    }

    public void discover(boolean on){
        if(on)
            nsdm.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
        else
            nsdm.stopServiceDiscovery(discoveryListener);
    }


    private void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        discoveryListener = new NsdManager.DiscoveryListener() {

            // Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                pb.setVisibility(View.VISIBLE);
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found! Do something with it.
                if (service.getServiceName().contains(SERVICE_NAME)){
                    byte[] value = service.getAttributes().get(PROF_ATTRIBUTE_NAME);
                    String name = new String(value, UTF_8);
                    selectableSortableMapLiveData.add(name, service);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                if (service.getServiceName().contains(SERVICE_NAME)){
                    byte[] value = service.getAttributes().get(PROF_ATTRIBUTE_NAME);
                    String name = new String(value, UTF_8);
                    selectableSortableMapLiveData.remove(name);
                }
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                nsdm.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                nsdm.stopServiceDiscovery(this);
            }
        };
    }
}
