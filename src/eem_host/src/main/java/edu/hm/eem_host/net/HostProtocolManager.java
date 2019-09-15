package edu.hm.eem_host.net;

import android.app.Activity;

import java.io.InputStream;
import java.net.Socket;
import java.util.Objects;

import edu.hm.eem_host.view.LockActivity;
import edu.hm.eem_library.model.ClientItemViewModel;
import edu.hm.eem_library.model.SelectableSortableItem;
import edu.hm.eem_library.net.ClientItem;
import edu.hm.eem_library.net.DataPacket;
import edu.hm.eem_library.net.FilePacket;
import edu.hm.eem_library.net.LoginPacket;
import edu.hm.eem_library.net.ProtocolManager;
import edu.hm.eem_library.net.SignalPacket;

/**
 * Host side {@link ProtocolManager}
 */
public class HostProtocolManager extends ProtocolManager {
    public static final int TO_ALL = -1;
    private final String exam;
    private final ClientItemViewModel.ClientItemLiveData liveData;

    public HostProtocolManager(Activity context, ClientItemViewModel.ClientItemLiveData liveData, LockActivity.LockHandler handler, String exam) {
        super(context, handler);
        this.liveData = liveData;
        this.exam = exam;
    }

    /**
     * Sending a {@link SignalPacket} using TCP to one or all clients using TO_ALL
     *
     * @param signal Signal code to be sent
     * @param index  receiving client: index in the livedata list
     */
    public void sendSignal(SignalPacket.Signal signal, int index) {
        if (index >= 0) {
            ClientItem device = Objects.requireNonNull(liveData.getValue()).get(index).item;
            sendSignal(signal, device.socket);
        } else {
            for (SelectableSortableItem<ClientItem> device : Objects.requireNonNull(liveData.getValue())) {
                sendSignal(signal, device.item.socket);
            }
        }
    }

    /**
     * Send the lighthouse signal
     *
     * @param index receiving client
     */
    public void sendLightHouse(int index) {
        ClientItem device = Objects.requireNonNull(liveData.getValue()).get(index).item;
        sendSignal(device.lighthoused ? SignalPacket.Signal.LIGHTHOUSE_ON : SignalPacket.Signal.LIGHTHOUSE_OFF, device.socket);
    }

    @Override
    public void quit() {
        sendSignal(SignalPacket.Signal.LOGOFF, TO_ALL);
        super.quit();
    }

    /**
     * If a client connects, generate a receiver thread for it
     *
     * @param socket the socket for the client
     */
    void genReceiverThread(Socket socket) {
        ReceiverThread receiverThread = new HostProtocolManager.HostReceiverThread(socket);
        receiverThread.start();
    }

    /**
     * The TCP receiver thread. Used to handle incoming messages.
     */
    class HostReceiverThread extends ProtocolManager.ReceiverThread {
        private String name;
        private boolean loggedIn = false;

        HostReceiverThread(Socket inputSocket) {
            super(inputSocket);
        }

        @Override
        protected boolean handleMessage(DataPacket.Type type, InputStream is, Socket socket) {
            boolean terminate = false;
            if (!loggedIn) {
                if (type == DataPacket.Type.LOGIN) {
                    name = LoginPacket.readData(is);
                    if (name != null) {
                        DataPacket dataPacket;
                        if (liveData.contains(name)) {
                            dataPacket = new SignalPacket(SignalPacket.Signal.INVALID_LOGIN);
                        } else {
                            loggedIn = true;
                            liveData.add(new SelectableSortableItem<>(name, new ClientItem(socket)), true);
                            dataPacket = new FilePacket(context.getFilesDir(), exam);
                        }
                        DataPacket.SenderThread sender = new DataPacket.SenderThread(socket, dataPacket);
                        sender.start();
                        setName("HostReceiverThread@" + name);
                    } else {
                        terminate = true;
                    }
                }
            } else {
                if (type == DataPacket.Type.SIGNAL) {
                    SignalPacket.Signal signal = SignalPacket.readData(is);
                    switch (signal) {
                        case LOGOFF:
                            ((LockActivity.LockHandler) handler).notifyStudentLeft(name);
                            terminate = true;
                            break;
                        case ALL_DOC_ACCEPTED:
                            liveData.setSelected(name, true);
                            break;
                        case NOTIFICATIONDRAWER_PULLED:
                            liveData.incrCountNotificationDrawer(name);
                            break;
                    }
                }
            }
            return terminate;
        }
    }
}
