package edu.hm.eem_host.net;

import android.app.Activity;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import edu.hm.eem_library.model.SortableItem;
import edu.hm.eem_library.model.SortableMapLiveData;
import edu.hm.eem_library.net.ClientDevice;
import edu.hm.eem_library.net.DataPacket;
import edu.hm.eem_library.net.LoginPacket;
import edu.hm.eem_library.net.ProtocolManager;
import edu.hm.eem_library.net.SignalPacket;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class HostProtocolManager extends ProtocolManager {
    private final ServerSocket serverSocket;
    private SortableMapLiveData<String, Socket, SortableItem<String, Socket>> liveData;

    private Thread serverThread;

    public HostProtocolManager(Activity context, ServerSocket serverSocket, SortableMapLiveData<String, Socket, SortableItem<String, Socket>> liveData) {
        super(context);
        this.serverSocket = serverSocket;
        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();
        this.liveData = liveData;
    }

    private class ServerThread implements Runnable {

        public void run() {
            Socket socket;
            ReceiverThread receiverThread;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    socket = serverSocket.accept();
                    receiverThread = new HostReceiverThread(socket);
                    receiverThread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void quit() {
        serverThread.interrupt();
        super.quit();
    }

    private class HostReceiverThread extends ProtocolManager.ReceiverThread {
        private String name;
        public HostReceiverThread(Socket inputSocket) {
            super(inputSocket);
        }

        @Override
        protected boolean handleMessage(DataPacket.Type type, InputStream is, Socket socket) {
            boolean ret = false;
            switch (type) {
                case LOGIN:
                    name = LoginPacket.readData(is);
                    SignalPacket signalPacket;
                    if (liveData.contains(name)) {
                        signalPacket = new SignalPacket(SignalPacket.Signal.INVALID_LOGIN);
                    } else {
                        liveData.add(name, new SortableItem<>(name, socket), true);
                        signalPacket = new SignalPacket(SignalPacket.Signal.VALID_LOGIN);
                    }
                    DataPacket.SenderThread sender = new DataPacket.SenderThread(socket, signalPacket);
                    sender.start();
                    ret = true;
                    break;
                case SIGNAL:
                    SignalPacket.Signal signal = SignalPacket.readData(is);
                    switch (signal){
                        case LOGOFF:
                            liveData.remove(name, true);
                            ret = false;
                            break;
                    }
                    break;

            }
            return ret;
        }
    }
}
