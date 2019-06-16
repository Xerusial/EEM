package edu.hm.eem_library.net;

import java.net.Socket;

public class ClientDevice {
    public Socket socket;
    public boolean lighthoused;

    public ClientDevice(Socket socket) {
        this.socket = socket;
        this.lighthoused = false;
    }
}
