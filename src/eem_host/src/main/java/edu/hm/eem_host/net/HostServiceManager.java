package edu.hm.eem_host.net;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import edu.hm.eem_host.view.LockActivity;
import edu.hm.eem_library.net.ServiceManager;

public class HostServiceManager extends ServiceManager {
    private NsdManager nsdm;
    private NsdServiceInfo serviceInfo;
    private ServerThread serverThread;
    private HostProtocolManager protocolManager;
    private LockActivity apl;
    private NsdManager.RegistrationListener currentListener = null;

    public HostServiceManager(LockActivity apl, String profName, HostProtocolManager protocolManager) {
        this.apl = apl;
        serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(profName);
        serviceInfo.setServiceType(SERVICE_TYPE);
        this.protocolManager = protocolManager;
        nsdm = (NsdManager) apl.getSystemService(Context.NSD_SERVICE);
    }

    public void init(ServerSocket serverSocket) {
        this.currentListener = apl;
        this.serverThread = new ServerThread(serverSocket);
        this.serverThread.start();
        serviceInfo.setPort(serverSocket.getLocalPort());
        nsdm.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, currentListener);
    }

    @Override
    public void quit() {
        if (currentListener != null) {
            nsdm.unregisterService(currentListener);
            currentListener = null;
        }
        if (serverThread != null) {
            serverThread.interrupt();
            serverThread = null;
        }
    }

    private class ServerThread extends Thread {
        private ServerSocket serverSocket;

        private ServerThread(ServerSocket serverSocket) {
            setName("ServerThread");
            this.serverSocket = serverSocket;
        }

        public void run() {
            Socket socket;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    socket = serverSocket.accept();
                    protocolManager.genReceiverThread(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
