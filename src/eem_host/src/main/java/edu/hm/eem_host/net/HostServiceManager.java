package edu.hm.eem_host.net;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import edu.hm.eem_host.view.LockActivity;
import edu.hm.eem_library.net.ServiceManager;

/**
 * Host side {@link ServiceManager}. This sets up Bonjour/Zeroconf using the built-in {@link NsdManager}
 * and a server thread to assign incoming connection requests to TCP sockets
 */
public class HostServiceManager extends ServiceManager {
    private final NsdManager nsdm;
    private final NsdServiceInfo serviceInfo;
    private ServerThread serverThread;
    private final HostProtocolService receiverService;
    private final LockActivity act;
    private NsdManager.RegistrationListener currentListener = null;

    /**
     * Initialize service type ("_exammode._tcp") and name,
     *
     * @param act             hosting activity
     * @param profName        is used as service name
     * @param receiverService the receiverservice hosting the receiver threads for all connections
     */
    public HostServiceManager(LockActivity act, String profName, HostProtocolService receiverService) {
        this.act = act;
        serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(profName);
        serviceInfo.setServiceType(SERVICE_TYPE);
        this.receiverService = receiverService;
        nsdm = (NsdManager) act.getSystemService(Context.NSD_SERVICE);
    }

    /**
     * Start service and TCP server
     *
     * @param serverSocket server will be initialized on this socket
     */
    public void init(ServerSocket serverSocket) {
        this.currentListener = act;
        this.serverThread = new ServerThread(serverSocket);
        this.serverThread.start();
        serviceInfo.setPort(serverSocket.getLocalPort());
        nsdm.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, currentListener);
    }

    /**
     * Clean up
     */
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

    /**
     * The server thread used to assign the incoming connection requests to TCP sockets
     */
    private class ServerThread extends Thread {
        private final ServerSocket serverSocket;

        private ServerThread(ServerSocket serverSocket) {
            setName("ServerThread");
            this.serverSocket = serverSocket;
        }

        public void run() {
            Socket socket;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    socket = serverSocket.accept();
                    receiverService.genReceiverThread(socket);
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
