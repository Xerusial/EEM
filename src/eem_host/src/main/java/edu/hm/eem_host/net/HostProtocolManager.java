package edu.hm.eem_host.net;

import android.app.Activity;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import edu.hm.eem_host.view.LockActivity;
import edu.hm.eem_library.model.SelectableSortableMapLiveData;
import edu.hm.eem_library.model.SortableItem;
import edu.hm.eem_library.model.SortableMapLiveData;
import edu.hm.eem_library.net.ClientDevice;
import edu.hm.eem_library.net.DataPacket;
import edu.hm.eem_library.net.FilePacket;
import edu.hm.eem_library.net.LoginPacket;
import edu.hm.eem_library.net.ProtocolManager;
import edu.hm.eem_library.net.SignalPacket;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class HostProtocolManager extends ProtocolManager {
    private final ServerSocket serverSocket;
    private SelectableSortableMapLiveData<Socket, SortableItem<Socket>> liveData;
    private Thread serverThread;
    private LockActivity.LockHandler handler;
    private final String exam;

    public HostProtocolManager(Activity context, ServerSocket serverSocket, SelectableSortableMapLiveData<Socket, SortableItem<Socket>> liveData, LockActivity.LockHandler handler, String exam) {
        super(context);
        this.serverSocket = serverSocket;
        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();
        this.liveData = liveData;
        this.handler = handler;
        this.exam = exam;
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

    public void sendLightHouse(int index){
        SignalPacket lightHouseSig = new SignalPacket(liveData.isSelected(index)? SignalPacket.Signal.LIGHTHOUSE_OFF: SignalPacket.Signal.LIGHTHOUSE_ON);
        DataPacket.SenderThread thread = new DataPacket.SenderThread(liveData.getValue().get(index).item, lightHouseSig);
        thread.start();
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
            boolean terminate = false;
            switch (type) {
                case LOGIN:
                    name = LoginPacket.readData(is);
                    DataPacket dataPacket;
                    if (liveData.contains(name)) {
                        dataPacket = new SignalPacket(SignalPacket.Signal.INVALID_LOGIN);
                    } else {
                        liveData.add(name, new SortableItem<>(name, socket), true);
                        dataPacket = new FilePacket(context.getFilesDir(), exam);
                    }
                    DataPacket.SenderThread sender = new DataPacket.SenderThread(socket, dataPacket);
                    sender.start();
                    break;
                case SIGNAL:
                    SignalPacket.Signal signal = SignalPacket.readData(is);
                    switch (signal){
                        case LOGOFF:
                            liveData.remove(name, true);
                            terminate = true;
                            break;
                    }
                    break;

            }
            return terminate;
        }
    }
}
