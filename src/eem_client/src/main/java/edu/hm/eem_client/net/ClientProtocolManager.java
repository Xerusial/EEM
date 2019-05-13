package edu.hm.eem_client.net;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;

import edu.hm.eem_library.net.DataPacket;
import edu.hm.eem_library.net.LoginPacket;
import edu.hm.eem_library.net.ProtocolManager;
import edu.hm.eem_library.net.SignalPacket;

public class ClientProtocolManager extends ProtocolManager {
    private Socket socket;
    private final String name;

    public ClientProtocolManager(Activity context, InetAddress host, int port, String name) {
        super(context);
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
            }
            return ret;
        }

        @Override
        protected void onPostExecute(Socket socket) {
            super.onPostExecute(socket);
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

    @Override
    public void quit(){
        SignalPacket ternSig = new SignalPacket(SignalPacket.Signal.LOGOFF);
        DataPacket.SenderThread thread = new DataPacket.SenderThread(socket, ternSig);
        thread.start();
        super.quit();
    }

    private class ClientReceiverThread extends ReceiverThread {
        ClientReceiverThread(Socket inputSocket) {
            super(inputSocket);
        }

        @Override
        protected boolean handleMessage(DataPacket.Type type, InputStream is, Socket socket) {
            boolean ret = false;
            switch (type) {
                case EXAMFILE:
                    ret = true;
                    break;
                case SIGNAL:
                    SignalPacket.Signal signal = SignalPacket.readData(is);
                    switch (signal){
                        case VALID_LOGIN:
                            ret = true;
                            break;
                        case INVALID_LOGIN:
                            putToast(edu.hm.eem_library.R.string.toast_please_change_your_username);
                            context.finish();
                            ret = true;
                            break;
                    }
                    break;
            }
            return ret;
        }
    }
}
