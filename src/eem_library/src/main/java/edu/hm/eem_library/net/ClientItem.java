package edu.hm.eem_library.net;

import java.net.Socket;

public class ClientItem {
    public Socket socket;
    public boolean lighthoused;

    public ClientItem(Socket socket) {
        this.socket = socket;
        this.lighthoused = false;
    }
}
