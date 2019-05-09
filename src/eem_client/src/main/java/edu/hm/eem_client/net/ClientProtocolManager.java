package edu.hm.eem_client.net;

import android.app.Activity;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import edu.hm.eem_library.net.DataPacket;
import edu.hm.eem_library.net.LoginPacket;
import edu.hm.eem_library.net.ProtocolManager;
import edu.hm.eem_library.net.SignalPacket;

public class ClientProtocolManager extends ProtocolManager {
    private Socket socket;
    private OutputStream os;
    private final TextView nameView;

    public ClientProtocolManager(Activity context, InetAddress host, int port, String name, TextView nameView) {
        super(context);
        this.nameView = nameView;
        try {
            socket = new Socket(host, port);
            os = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ReceiverThread receiverThread = new ReceiverThread(socket);
        receiverThread.start();
        LoginPacket login = new LoginPacket(name);
        login.sendData(os);
    }

    @Override
    protected boolean handleMessage(DataPacket.Type type, InputStream is, OutputStream os) {
        boolean ret = false;
        switch (type) {
            case EXAMFILE:
                ret = true;
                break;
            case SIGNAL:
                SignalPacket.Signal signal = SignalPacket.readData(is);
                switch (signal){
                    case VALID_LOGIN:
                        nameView.setText("Connected!");
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
