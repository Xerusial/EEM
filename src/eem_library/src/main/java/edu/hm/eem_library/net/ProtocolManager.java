package edu.hm.eem_library.net;

import android.app.Application;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.support.annotation.StringRes;
import android.widget.Toast;

import java.io.InputStream;
import java.net.Socket;

import edu.hm.eem_library.R;

public abstract class ProtocolManager {
    protected final String SERVICE_NAME = "ExamMode";
    protected final String SERVICE_TYPE = "_exammode._tcp";
    protected NsdServiceInfo serviceInfo;
    protected NsdManager nsdm;
    protected ReceiverThread receiverThread;
    private final Application apl;
    private final Toast toast;

    public ProtocolManager(NsdManager nsdm, Application apl) {
        this.nsdm = nsdm;
        this.apl = apl;
        toast = new Toast(apl);
        toast.setDuration(Toast.LENGTH_SHORT);
    }

    /* Protocol Receiver Thread
     * The server opens one thread for each socket, the client has only got one thread.
     */
    public class ReceiverThread extends Thread {
        private Socket inputSocket;

        public ReceiverThread(Socket inputSocket) {
            this.inputSocket = inputSocket;
        }

        @Override
        public void run() {
            InputStream is = inputSocket.getInputStream();
            while (true) {
                Object[] header = DataPacket.readHeader(is);
                if ((int) header[0] != DataPacket.PROTOCOL_VERSION) {
                    putToast(R.string.toast_protocol_too_new);
                }
                switch ((DataPacket.Type) header[1]) {
                    case NAME:
                        login(LoginPacket.readData(is));
                        break;
                    case EXAMFILE:
                        break;
                }
            }
        }
    }

    protected abstract void login(String name);

    /* This method prevents the receiverThreads from flooding the application with toasts.
     * Only one toast is shown at a time.
     */
    private void putToast(@StringRes int resId){
        if(toast.getView() == null) {
            toast.setText(apl.getString(resId));
            toast.show();
        }
    }
}
