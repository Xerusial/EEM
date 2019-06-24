package edu.hm.eem_host.net;

import android.util.Log;

import com.github.druk.dnssd.DNSSDBindable;
import com.github.druk.dnssd.DNSSDEmbedded;
import com.github.druk.dnssd.DNSSDException;
import com.github.druk.dnssd.DNSSDService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import edu.hm.eem_host.view.LockActivity;
import edu.hm.eem_library.net.ServiceManager;

public class HostServiceManager extends ServiceManager {
    private final String profName;
    private DNSSDEmbedded dnssd;
    private DNSSDService service = null;
    private ServerThread serverThread;
    private HostProtocolManager protocolManager;
    private LockActivity apl;

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

    public HostServiceManager(LockActivity apl, String profName, HostProtocolManager protocolManager) {
        this.apl = apl;
        this.profName = profName;
        this.protocolManager = protocolManager;
        dnssd = new DNSSDEmbedded(apl);
    }

    public void init(ServerSocket serverSocket){
        this.serverThread = new ServerThread(serverSocket);
        this.serverThread.start();
        try {
            service = dnssd.register(profName, SERVICE_TYPE, serverSocket.getLocalPort(), apl);
        } catch (DNSSDException e) {
            Log.e("TAG", "error", e);
        }
    }

    @Override
    public void quit() {
        if(service!=null) {
            service.stop();
            service = null;
        }
        if(serverThread!=null) {
            serverThread.interrupt();
            serverThread = null;
        }
        dnssd.exit();
    }
}
