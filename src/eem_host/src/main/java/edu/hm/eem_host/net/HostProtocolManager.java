package edu.hm.eem_host.net;

import android.app.Activity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import edu.hm.eem_library.model.SortableMapLiveData;
import edu.hm.eem_library.net.ClientDevice;
import edu.hm.eem_library.net.DataPacket;
import edu.hm.eem_library.net.LoginPacket;
import edu.hm.eem_library.net.ProtocolManager;
import edu.hm.eem_library.net.SignalPacket;

public class HostProtocolManager extends ProtocolManager {
    private final ServerSocket serverSocket;
    private SortableMapLiveData<String, ClientDevice> liveData;

    private Thread serverThread;

    public HostProtocolManager(Activity context, ServerSocket serverSocket, SortableMapLiveData<String, ClientDevice> liveData) {
        super(context);
        this.serverSocket = serverSocket;
        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();
        this.liveData = liveData;
    }

    class ServerThread implements Runnable {

        public void run() {
            Socket socket;
            ReceiverThread receiverThread;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    socket = serverSocket.accept();
                    receiverThread = new ReceiverThread(socket);
                    receiverThread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    protected boolean handleMessage(DataPacket.Type type, InputStream is, OutputStream os) {
        boolean ret = false;
        switch (type) {
            case LOGIN:
                String name = LoginPacket.readData(is);
                SignalPacket signalPacket;
                if (liveData.contains(name)) {
                    signalPacket = new SignalPacket(SignalPacket.Signal.INVALID_LOGIN);
                } else {
                    liveData.add(name, new ClientDevice(name, os));
                    signalPacket = new SignalPacket(SignalPacket.Signal.VALID_LOGIN);
                }
                signalPacket.sendData(os);
                ret = true;
                break;
        }
        return ret;
    }
}
