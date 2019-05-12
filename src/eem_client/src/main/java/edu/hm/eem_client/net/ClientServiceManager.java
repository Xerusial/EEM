package edu.hm.eem_client.net;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import edu.hm.eem_library.model.SelectableSortableMapLiveData;
import edu.hm.eem_library.net.ServiceManager;

public class ClientServiceManager extends ServiceManager {
    private final SelectableSortableMapLiveData<String, NsdServiceInfo> selectableSortableMapLiveData;
    private final NsdManager nsdm;
    private boolean discovering = false;
    private DiscoveryListener discoveryListener = null;

    public ClientServiceManager(NsdManager nsdm, SelectableSortableMapLiveData<String, NsdServiceInfo> selectableSortableMapLiveData) {
        this.nsdm = nsdm;
        this.selectableSortableMapLiveData = selectableSortableMapLiveData;
    }

    public void discover(boolean on) {
        if(on) {
            discoveryListener = new DiscoveryListener();
            nsdm.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
            discovering = true;
        } else if (discovering) {
            nsdm.stopServiceDiscovery(discoveryListener);
            discovering = false;
        }
    }

    public void resolve(NsdServiceInfo serviceInfo, NsdManager.ResolveListener listener){
        nsdm.resolveService(serviceInfo, listener);
    }

    @Override
    public void quit() {
        discover(false);
    }

    private class DiscoveryListener implements NsdManager.DiscoveryListener {
        @Override
        public void onStartDiscoveryFailed(String serviceType, int errorCode) {

        }

        @Override
        public void onStopDiscoveryFailed(String serviceType, int errorCode) {

        }

        @Override
        public void onDiscoveryStarted(String serviceType) {

        }

        @Override
        public void onDiscoveryStopped(String serviceType) {

        }

        @Override
        public void onServiceFound(NsdServiceInfo serviceInfo) {
            selectableSortableMapLiveData.add(serviceInfo.getServiceName(), serviceInfo);
        }

        @Override
        public void onServiceLost(NsdServiceInfo serviceInfo) {

        }
    }
}
