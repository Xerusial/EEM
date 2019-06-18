package edu.hm.eem_client.net;

import android.app.Activity;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;

import edu.hm.eem_client.view.LockedActivity;
import edu.hm.eem_library.model.TeacherExam;
import edu.hm.eem_library.net.DataPacket;
import edu.hm.eem_library.net.FilePacket;
import edu.hm.eem_library.net.LoginPacket;
import edu.hm.eem_library.net.ProtocolManager;
import edu.hm.eem_library.net.SignalPacket;

/** The client side version of {@link ProtocolManager}
 *
 */
public class ClientProtocolManager extends ProtocolManager {
    private Socket socket = null;
    private final String name;

    public ClientProtocolManager(Activity context, InetAddress host, int port, String name, LockedActivity.LockedHandler handler) {
        super(context, handler);
        this.name = name;
        //Needs to be in new thread as networking is not allowed in UI thread
        Thread openSocketThread = new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    prep(new Socket(host, port));
                } catch (IOException e) {
                    e.printStackTrace();
                    handler.gracefulShutdown("Connection was refused!");
                }
            }
        };
        openSocketThread.start();
    }

    /** Tasks to be done after the socket is open
     *
     * @param openedSocket The socket that has been opened
     */
    private void prep(Socket openedSocket){
        socket = openedSocket;
        //Start the socket receiver thread
        ReceiverThread receiverThread = new ClientReceiverThread(socket);
        receiverThread.start();
        //Send the login packet to the host
        LoginPacket login = new LoginPacket(name);
        DataPacket.SenderThread senderThread = new DataPacket.SenderThread(socket, login);
        senderThread.start();
    }

    /** Method to be called, if rejected documents are post-allowed by the professors password
     *
     */
    public void allDocumentsAccepted(){
        SignalPacket successSig = new SignalPacket(SignalPacket.Signal.ALL_DOC_ACCEPTED);
        DataPacket.SenderThread thread = new DataPacket.SenderThread(socket, successSig);
        thread.start();
    }

    @Override
    public void quit(){
        //Order left 2 right
        if(socket!=null) {
            SignalPacket termSig = new SignalPacket(SignalPacket.Signal.LOGOFF);
            DataPacket.SenderThread thread = new DataPacket.SenderThread(socket, termSig);
            thread.start();
        }
        super.quit();
    }

    private class ClientReceiverThread extends ReceiverThread {
        ClientReceiverThread(Socket inputSocket) {
            super(inputSocket);
        }

        @Override
        protected boolean handleMessage(DataPacket.Type type, InputStream is, Socket socket) {
            boolean terminate = false;
            LockedActivity.LockedHandler handler = (LockedActivity.LockedHandler) ClientProtocolManager.this.handler;
            switch (type) {
                case EXAMFILE:
                    TeacherExam exam = FilePacket.readData(is);
                    if(handler.receiveExam(exam)){
                        allDocumentsAccepted();
                    }
                    break;
                case SIGNAL:
                    SignalPacket.Signal signal = SignalPacket.readData(is);
                    switch (signal){
                        case INVALID_LOGIN:
                            handler.putToast(edu.hm.eem_library.R.string.toast_please_change_your_username);
                            handler.gracefulShutdown(null);
                            break;
                        case LIGHTHOUSE_ON:
                            handler.postLighthouse(true);
                            break;
                        case LIGHTHOUSE_OFF:
                            handler.postLighthouse(false);
                            break;
                        case  LOGOFF:
                            handler.gracefulShutdown("Terminate from host!");
                            terminate = true;
                            break;
                    }
                    break;
            }
            return terminate;
        }
    }
}
