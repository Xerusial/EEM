package edu.hm.eem_library.net;

import android.app.Activity;
import android.support.annotation.StringRes;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import edu.hm.eem_library.R;

public abstract class ProtocolManager {
    protected final Activity context;
    private final Toast toast;

    public ProtocolManager(Activity context) {
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
            OutputStream os = null;
            try {
                is = inputSocket.getInputStream();
                os = inputSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //noinspection InfiniteLoopStatement
            while (true) {
                Object[] header = DataPacket.readHeader(is);
                if ((int) header[0] != DataPacket.PROTOCOL_VERSION) {
                    putToast(R.string.toast_protocol_too_new);
                }
                handleMessage((DataPacket.Type) header[1], is, os);
            }
        }
    }

    protected abstract boolean handleMessage(DataPacket.Type type, InputStream is, OutputStream os);

    /* This method prevents the receiverThreads from flooding the application with toasts.
     * Only one toast is shown at a time.
     */
    protected void putToast(@StringRes int resId){
        if(toast.getView() == null) {
            toast.setText(context.getString(resId));
            toast.show();
        }
    }
}
