package edu.hm.eem_host.net;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import java.net.ServerSocket;

import edu.hm.eem_library.net.ServiceManager;

public class HostServiceManager extends ServiceManager {
    private NsdManager.RegistrationListener registrationListener;
    private final String profName;
    private NsdServiceInfo serviceInfo;
    private final NsdManager nsdm;

    public HostServiceManager(ServerSocket serverSocket, String profName, NsdManager nsdm) {
        this.profName = profName;
        this.nsdm = nsdm;
        createService(serverSocket.getLocalPort());
    }

    private void createService(int port) {
        // Create the NsdServiceInfo object, and populate it.
        serviceInfo = new NsdServiceInfo();

        // The name is subject to change based on conflicts
        // with other services advertised on the same network.
        serviceInfo.setServiceName(SERVICE_NAME);
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setAttribute(PROF_ATTRIBUTE_NAME, profName);
        serviceInfo.setPort(port);

        initializeRegistrationListener();
        nsdm.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);

    }

    private void initializeRegistrationListener() {
        registrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo nsdServiceInfo) {
                // Save the service name. Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                serviceInfo = nsdServiceInfo;
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Registration failed! Put debugging code here to determine why.
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                // Service has been unregistered. This only happens when you call
                // NsdManager.unregisterService() and pass in this listener.
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Unregistration failed. Put debugging code here to determine why.
            }
        };
    }
}
