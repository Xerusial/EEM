package edu.hm.eem_client.net;

import android.app.Activity;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

import edu.hm.eem_client.R;
import edu.hm.eem_client.view.LockedActivity;
import edu.hm.eem_library.model.TeacherExam;
import edu.hm.eem_library.net.DataPacket;
import edu.hm.eem_library.net.FilePacket;
import edu.hm.eem_library.net.LoginPacket;
import edu.hm.eem_library.net.ProtocolManager;
import edu.hm.eem_library.net.SignalPacket;

/**
 * The client side version of {@link ProtocolManager}
 * it hosts a receiver thread for the sockets inputstream as well as some convenience
 * functions for sending data on the outputstream.
 */
public class ClientProtocolManager extends ProtocolManager {
    private final String name;
    private Socket socket = null;
    private ClientReceiverThread thread;

    /**
     * constructor
     *
     * @param context The activity context where this protocol manager is used in
     * @param host    The teacher host ip address from the detected service
     * @param port    The teacher host tcp socket port from the detected service
     * @param name    The students name
     * @param handler A lockedhandler for various callbacks to the activity
     */
    public ClientProtocolManager(Activity context, InetAddress host, int port, String name, LockedActivity.LockedHandler handler) {
        super(context, handler);
        this.name = name;
        //Needs to be in new thread as networking is not allowed in UI thread
        Thread openSocketThread = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    prep(new Socket(host, port));
                } catch (ConnectException e) {
                    e.printStackTrace();
                    handler.gracefulShutdown(true, R.string.connection_refused);
                } catch (IOException e) {
                    e.printStackTrace();
                    handler.gracefulShutdown(false, 0);
                }
            }
        };
        openSocketThread.start();
    }

    /**
     * Tasks to be done after the socket is open
     *
     * @param openedSocket The socket that has been opened
     */
    private void prep(Socket openedSocket) {
        ((LockedActivity.LockedHandler) handler).loadDocuments();
        socket = openedSocket;
        //Start the socket receiver thread
        thread = new ClientReceiverThread(socket);
        thread.start();
        //Send the login packet to the host
        LoginPacket login = new LoginPacket(name);
        DataPacket.SenderThread senderThread = new DataPacket.SenderThread(socket, login);
        senderThread.start();
    }

    /**
     * Method to be called, if rejected documents are post-allowed by the professors password
     */
    public void allDocumentsAccepted() {
        sendSignal(SignalPacket.Signal.ALL_DOC_ACCEPTED, socket);
    }

    /**
     * Method to send the host a signal, that the notification drawer has been pulled on the device.
     */
    public void notificationDrawerPulled() {
        sendSignal(SignalPacket.Signal.NOTIFICATIONDRAWER_PULLED, socket);
    }

    /**
     * Method in to be called, when the communication needs to be terminated
     */
    public void quit() {
        if (socket != null) {
            SignalPacket termSig = new SignalPacket(SignalPacket.Signal.LOGOFF);
            DataPacket.SenderThread thread = new DataPacket.SenderThread(socket, termSig);
            thread.start();
        }
        thread.interrupt();
    }

    /**
     * The extension of the {@link ProtocolManager.ReceiverThread}. It handles the incoming
     * packets for the client app. Note: calls from this thread, that affect the UI have to be
     * posted to the UIs main looper. This is done using the {@link android.os.Handler} class.
     */
    private class ClientReceiverThread extends ReceiverThread {
        ClientReceiverThread(Socket inputSocket) {
            super(inputSocket);
            setName("ClientReceiverThread");
        }

        /**
         * The run method, which can be found in the {@link ProtocolManager.ReceiverThread} does
         * the basic preprocessing like stripping the headers from the packet and decoding its type.
         * This type is then handed to this method for further actions on the client.
         *
         * @param type   The type of {@link DataPacket}
         * @param is     the inputstream from the respective socket
         * @param socket the respective socket
         * @return whether to terminate the receiverthread
         */
        @Override
        protected boolean handleMessage(DataPacket.Type type, InputStream is, Socket socket) {
            boolean terminate = false;
            LockedActivity.LockedHandler handler = (LockedActivity.LockedHandler) ClientProtocolManager.this.handler;
            switch (type) {
                case EXAMFILE:
                    TeacherExam exam = FilePacket.readData(is);
                    handler.receiveExam(exam);
                    break;
                case SIGNAL:
                    SignalPacket.Signal signal = SignalPacket.readData(is);
                    switch (signal) {
                        case CHECK_CONNECTION:
                            SignalPacket ackSig = new SignalPacket(SignalPacket.Signal.CHECK_ACK);
                            DataPacket.SenderThread thread = new DataPacket.SenderThread(socket, ackSig);
                            thread.start();
                            break;
                        case INVALID_LOGIN_NAME:
                            handler.gracefulShutdown(true, R.string.toast_please_change_your_username);
                            break;
                        case INVALID_LOGIN_VERS_HIGH:
                            handler.gracefulShutdown(true, R.string.toast_protocol_too_new);
                            break;
                        case INVALID_LOGIN_VERS_LOW:
                            handler.gracefulShutdown(true, R.string.toast_protocol_too_old);
                            break;
                        case LIGHTHOUSE_ON:
                            handler.postLighthouse(true);
                            break;
                        case LIGHTHOUSE_OFF:
                            handler.postLighthouse(false);
                            break;
                        case LOGOFF:
                            handler.gracefulShutdown(true, R.string.teacher_closed);
                            terminate = true;
                            break;
                        case LOCK:
                            handler.lock();
                            break;
                    }
                    break;
            }
            return terminate;
        }
    }
}
