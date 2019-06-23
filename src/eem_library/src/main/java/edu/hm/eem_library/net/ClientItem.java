package edu.hm.eem_library.net;

import java.net.Socket;

public class ClientItem {
    public final Socket socket;
    public boolean lighthoused;
    public int countNotificationDrawer;

    public ClientItem(Socket socket) {
        this.socket = socket;
        this.lighthoused = false;
        this.countNotificationDrawer = 0;
    }
}
