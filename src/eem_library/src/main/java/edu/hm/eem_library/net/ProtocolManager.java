package edu.hm.eem_library.net;

import android.app.Activity;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Objects;

import edu.hm.eem_library.R;

/**
 * Abstract base for the client- and host version protocol manager.
 * Everything concerning the TCP protocol is implemented here.
 */
public abstract class ProtocolManager {
    protected final Activity context;
    protected final ProtocolHandler handler;
    private final LinkedList<ReceiverThread> threads;

    /**
     * Constructor
     *
     * @param context calling activity
     * @param handler a protocol handler to sync callback to UI thread
     */
    protected ProtocolManager(Activity context, ProtocolHandler handler) {
        this.context = context;
        this.handler = handler;
        threads = new LinkedList<>();
    }

    /**
     * Cleanup
     */
    public void quit() {
        for (ReceiverThread thread : threads) {
            thread.interrupt();
        }
        threads.clear();
    }

    /**
     * Method to send a signal packet to the specified socket
     *
     * @param signal type
     * @param socket target socket
     */
    protected final void sendSignal(SignalPacket.Signal signal, Socket socket) {
        SignalPacket signalPacket = new SignalPacket(signal);
        DataPacket.SenderThread thread = new DataPacket.SenderThread(socket, signalPacket);
        thread.start();
    }

    /**
     * Protocol Receiver Thread
     * The server opens one thread for each socket, the client has only got one thread.
     */
    public abstract class ReceiverThread extends Thread {
        private final Socket socket;

        public ReceiverThread(Socket Socket) {
            this.socket = Socket;
            threads.add(this);
        }

        /**
         * Core runnable for the receiver thread
         */
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
                Object[] header = DataPacket.readHeader(Objects.requireNonNull(is));
                if ((int) header[0] != DataPacket.PROTOCOL_VERSION) {
                    handler.putToast(R.string.toast_protocol_too_new);
                }
                if (handleMessage((DataPacket.Type) header[1], is, socket))
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

        /**
         * Template call for managing incoming packets. Implemented in the respective host and client
         * side receiver threads.
         *
         * @param type   type of {@link DataPacket}
         * @param is     the inputstream from the respective socket
         * @param socket the respective socket
         * @return whether to terminate the receiverthread
         */
        protected abstract boolean handleMessage(DataPacket.Type type, InputStream is, Socket socket);
    }
}
