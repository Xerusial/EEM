package edu.hm.eem_library.net;

import java.net.Socket;

public class ClientItem {
    public final Socket socket;
    public boolean lighthoused = false;
    public int countNotificationDrawer = 0;

    public ClientItem(Socket socket) {
        this.socket = socket;
    }
}
