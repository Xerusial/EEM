package edu.hm.eem_host.net;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientDevice {
    public String clientName;
    public InetAddress address;
    public ServerSocket serverSocket;
    public Socket clientSocket;

    public ClientDevice(String clientName, InetAddress address, ServerSocket serverSocket, Socket clientSocket) {
        this.clientName = clientName;
        this.address = address;
        this.serverSocket = serverSocket;
        this.clientSocket = clientSocket;
    }
}
