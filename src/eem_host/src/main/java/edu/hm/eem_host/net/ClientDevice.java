package edu.hm.eem_host.net;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientDevice {
    public String clientName;
    public ServerSocket serverSocket;

    public ClientDevice(String clientName, ServerSocket serverSocket) {
        this.clientName = clientName;
        this.serverSocket = serverSocket;
    }
}
