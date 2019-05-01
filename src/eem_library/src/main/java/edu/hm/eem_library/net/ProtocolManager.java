package edu.hm.eem_library.net;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.support.annotation.StringRes;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import edu.hm.eem_library.R;

public abstract class ProtocolManager {
    public final static String PROF_ATTRIBUTE_NAME = "p";
    protected final String SERVICE_NAME = "ExamMode";
    protected final String SERVICE_TYPE = "_exammode._tcp";
    protected NsdServiceInfo serviceInfo;
    protected NsdManager nsdm;
    protected ReceiverThread receiverThread;
    private final Context context;
    private final Toast toast;

    public ProtocolManager(NsdManager nsdm, Context context) {
        this.nsdm = nsdm;
        this.context = context;
        toast = new Toast(context);
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
            InputStream is = null;
            try {
                is = inputSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (true) {
                Object[] header = DataPacket.readHeader(is);
                if ((int) header[0] != DataPacket.PROTOCOL_VERSION) {
                    putToast(R.string.toast_protocol_too_new);
                }
                switch ((DataPacket.Type) header[1]) {
                    case LOGIN:
                        login(LoginPacket.readData(is));
                        break;
                    case EXAMFILE:
                        break;
                    case INVALID_LOGIN:
                        putToast(R.string.toast_please_change_your_username);
                        break;
                }
            }
        }
    }

    protected abstract boolean login(String name);

    /* This method prevents the receiverThreads from flooding the application with toasts.
     * Only one toast is shown at a time.
     */
    private void putToast(@StringRes int resId){
        if(toast.getView() == null) {
            toast.setText(context.getString(resId));
            toast.show();
        }
    }
}
