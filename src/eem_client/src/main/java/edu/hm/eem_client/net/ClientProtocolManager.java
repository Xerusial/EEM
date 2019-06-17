package edu.hm.eem_client.net;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Pair;

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

public class ClientProtocolManager extends ProtocolManager {
    private Socket socket = null;
    private final String name;

    public ClientProtocolManager(Activity context, InetAddress host, int port, String name, LockedActivity.LockedHandler handler) {
        super(context, handler);
        this.name = name;
        PrepTask task = new PrepTask();
        task.execute(new Pair<>(host, port));
    }

    //use asynctask only to resolve the socket!!!
    @SuppressLint("StaticFieldLeak")
    private class PrepTask extends AsyncTask<Pair<InetAddress, Integer>, Void, Socket> {
        @Override
        protected Socket doInBackground(Pair<InetAddress, Integer>... pairs) {
            Socket ret = null;
            try {
                ret = new Socket(pairs[0].first, pairs[0].second);
            } catch (IOException e) {
                e.printStackTrace();
                ((LockedActivity.LockedHandler)handler).gracefulShutdown("Connection was refused!");
                ret = null;
            }
            return ret;
        }

        @Override
        protected void onPostExecute(Socket socket) {
            super.onPostExecute(socket);
            if(socket!=null)
                prep(socket);
        }
    }

    private void prep(Socket resolvedSocket){
        socket = resolvedSocket;
        ReceiverThread receiverThread = new ClientReceiverThread(socket);
        receiverThread.start();
        LoginPacket login = new LoginPacket(name);
        DataPacket.SenderThread senderThread = new DataPacket.SenderThread(socket, login);
        senderThread.start();
    }

    public void allDocumentsAccepted(){
        SignalPacket successSig = new SignalPacket(SignalPacket.Signal.ALL_DOC_ACCEPTED);
        DataPacket.SenderThread thread = new DataPacket.SenderThread(socket, successSig);
        thread.start();
    }

    @Override
    public void quit(){
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
                    }
                    break;
            }
            return terminate;
        }
    }
}
