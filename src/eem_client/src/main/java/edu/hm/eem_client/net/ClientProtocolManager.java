package edu.hm.eem_client.net;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.view.View;
import android.widget.ProgressBar;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import edu.hm.eem_library.model.StringMapViewModel;
import edu.hm.eem_library.net.ProtocolManager;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ClientProtocolManager extends ProtocolManager {
    private NsdManager.ResolveListener resolveListener;
    private NsdManager.DiscoveryListener discoveryListener;
    private ProgressBar pb;
    private Socket socket;
    private final StringMapViewModel.StringMapLiveData stringMapLiveData;

    public ClientProtocolManager(NsdManager nsdm, StringMapViewModel.StringMapLiveData stringMapLiveData) {
        super(nsdm);
        this.stringMapLiveData = stringMapLiveData;
        initializeDiscoveryListener();
        initializeResolveListener();
    }

    public void discover(boolean on){
        if(on)
            nsdm.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
        else
            nsdm.stopServiceDiscovery(discoveryListener);
    }

    public void initializeResolveListener() {
        resolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails. Use the error code to debug.
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                ClientProtocolManager.this.serviceInfo = serviceInfo;
                int port = serviceInfo.getPort();
                InetAddress host = serviceInfo.getHost();
                try {
                    socket = new Socket(host, port);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                receiverThread = new ReceiverThread(socket);
                receiverThread.start();
                pb.setVisibility(View.GONE);
                nsdm.stopServiceDiscovery(discoveryListener);
            }
        };
    }


    public void initializeDiscoveryListener() {

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
                    stringMapLiveData.add(name);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                if (service.getServiceName().contains(SERVICE_NAME)){
                    byte[] value = service.getAttributes().get(PROF_ATTRIBUTE_NAME);
                    String name = new String(value, UTF_8);
                    stringMapLiveData.remove(name);
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
