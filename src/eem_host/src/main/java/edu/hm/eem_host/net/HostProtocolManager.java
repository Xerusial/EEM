package edu.hm.eem_host.net;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import edu.hm.eem_library.net.ProtocolManager;

public class HostProtocolManager extends ProtocolManager {
    private ServerSocket serverSocket;
    private NsdManager.RegistrationListener registrationListener;
    private Map<Socket, String> socketMap = new HashMap<>();
    private String serviceName = SERVICE_NAME;
    private String profName;

    Thread serverThread = null;

    public HostProtocolManager(NsdManager nsdm, String profName) {
        super(nsdm);
        this.profName = profName;
        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();
    }

    void finalize() {
        nsdm.unregisterService(registrationListener);
        try {
            if(serverSocket!=null)
                serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ServerThread implements Runnable {

        public void run() {
            Socket socket = null;
            try {
                serverSocket = new ServerSocket(0);
                createService(serverSocket.getLocalPort());
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {

                try {
                    socket = serverSocket.accept();
                    socketMap.put(socket, null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void createService(int port) {
        // Create the NsdServiceInfo object, and populate it.
        serviceInfo = new NsdServiceInfo();

        // The name is subject to change based on conflicts
        // with other services advertised on the same network.
        serviceInfo.setServiceName(SERVICE_NAME);
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setAttribute("prof", profName);
        serviceInfo.setPort(port);

        initializeRegistrationListener();
        nsdm.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);

    }

    public void initializeRegistrationListener() {
        registrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name. Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                serviceName = NsdServiceInfo.getServiceName();
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
