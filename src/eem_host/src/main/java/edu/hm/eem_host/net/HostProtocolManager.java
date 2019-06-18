package edu.hm.eem_host.net;

import android.app.Activity;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import edu.hm.eem_host.R;
import edu.hm.eem_host.view.LockActivity;
import edu.hm.eem_library.model.SelectableSortableItem;
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
    private SelectableSortableMapLiveData<ClientDevice, SelectableSortableItem<ClientDevice>> liveData;
    private final String exam;

    public HostProtocolManager(Activity context, SelectableSortableMapLiveData<ClientDevice, SelectableSortableItem<ClientDevice>> liveData, LockActivity.LockHandler handler, String exam) {
        super(context, handler);
        this.liveData = liveData;
        this.exam = exam;
    }

    public void sendLightHouse(int index){
        ClientDevice device = liveData.getValue().get(index).item;
        SignalPacket lightHouseSig = new SignalPacket(device.lighthoused? SignalPacket.Signal.LIGHTHOUSE_ON: SignalPacket.Signal.LIGHTHOUSE_OFF);
        DataPacket.SenderThread thread = new DataPacket.SenderThread(device.socket, lightHouseSig);
        thread.start();
    }

    @Override
    public void quit() {
        for(SelectableSortableItem<ClientDevice> device : liveData.getValue()){
            SignalPacket termSig = new SignalPacket(SignalPacket.Signal.LOGOFF);
            DataPacket.SenderThread thread = new DataPacket.SenderThread(device.item.socket, termSig);
            thread.start();
        }
        super.quit();
    }

    public void genReceiverThread(Socket socket){
        ReceiverThread receiverThread = new HostProtocolManager.HostReceiverThread(socket);
        receiverThread.start();
    }

    class HostReceiverThread extends ProtocolManager.ReceiverThread {
        private String name;
        private boolean loggedIn = false;
        public HostReceiverThread(Socket inputSocket) {
            super(inputSocket);
        }

        @Override
        protected boolean handleMessage(DataPacket.Type type, InputStream is, Socket socket) {
            boolean terminate = false;
            if(!loggedIn) {
                if (type == DataPacket.Type.LOGIN) {
                    name = LoginPacket.readData(is);
                    if (name != null) {
                        DataPacket dataPacket;
                        if (liveData.contains(name)) {
                            dataPacket = new SignalPacket(SignalPacket.Signal.INVALID_LOGIN);
                        } else {
                            loggedIn = true;
                            liveData.add(new SelectableSortableItem<>(name, new ClientDevice(socket)), true);
                            dataPacket = new FilePacket(context.getFilesDir(), exam);
                        }
                        DataPacket.SenderThread sender = new DataPacket.SenderThread(socket, dataPacket);
                        sender.start();
                    } else {
                        terminate = true;
                    }
                }
            }else {
                switch (type) {
                    case SIGNAL:
                        SignalPacket.Signal signal = SignalPacket.readData(is);
                        switch (signal){
                            case LOGOFF:
                                liveData.remove(name, true);
                                ((LockActivity.LockHandler)handler).notifyStudentLeft(name);
                                terminate = true;
                                break;
                            case ALL_DOC_ACCEPTED:
                                liveData.toggleSelected(name);
                                break;
                        }
                        break;
                }
            }
            return terminate;
        }
    }
}
