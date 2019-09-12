package edu.hm.eem_library.net;

import java.net.Socket;

/**
 * A struct containing a collection of attributes of a connected student, such as the associated
 * TCP socket, whether his device is "lighthoused" or disconnected and a counter of the times he
 * opened the notification drawer
 */
public class ClientItem {
    public final Socket socket;
    public boolean lighthoused = false;
    public boolean disconnected = false;
    public int countNotificationDrawer = 0;

    public ClientItem(Socket socket) {
        this.socket = socket;
    }
}
