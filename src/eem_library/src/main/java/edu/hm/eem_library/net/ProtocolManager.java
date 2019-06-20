package edu.hm.eem_library.net;

import android.app.Activity;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.LinkedList;

import edu.hm.eem_library.R;
import edu.hm.eem_library.model.ProtocolHandler;

public abstract class ProtocolManager{
    protected final Activity context;
    private final LinkedList<ReceiverThread> threads;
    protected final ProtocolHandler handler;

    public ProtocolManager(Activity context, ProtocolHandler handler) {
        this.context = context;
        this.handler = handler;
        threads = new LinkedList<>();
    }

    public void quit(){
        for(ReceiverThread thread : threads){
            thread.interrupt();
        }
    }

    /* Protocol Receiver Thread
     * The server opens one thread for each socket, the client has only got one thread.
     */
    public abstract class ReceiverThread extends Thread {
        private Socket socket;

        public ReceiverThread(Socket Socket) {
            this.socket = Socket;
            threads.add(this);
        }

        @Override
        public void run() {
            InputStream is = null;
            try {
                is = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
                interrupt();
            }
            while (!Thread.currentThread().isInterrupted()) {
                Object[] header = DataPacket.readHeader(is);
                if ((int) header[0] != DataPacket.PROTOCOL_VERSION) {
                    handler.putToast(R.string.toast_protocol_too_new);
                }
                if(handleMessage((DataPacket.Type) header[1], is, socket))
                    interrupt();
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        protected abstract boolean handleMessage(DataPacket.Type type, InputStream is, Socket socket);
    }
}
