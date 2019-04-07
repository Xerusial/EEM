package edu.hm.eem_host.net;

import android.net.wifi.WifiManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;

import edu.hm.eem_library.net.ProtocolManager;

public class HostProtocolManager extends ProtocolManager {
    private WifiManager wm;
    private final LinkedList<ClientDevice> clientList = new LinkedList<>();

    public HostProtocolManager(WifiManager wm){
        this.wm = wm;
    }

    @Override
    protected void onUdpReceive() {
        while (!udpStack.empty()) {
            ByteBuffer receiveBuffer = ByteBuffer.wrap(udpStack.pop());
            if (receiveBuffer.get() == PacketId.REQUEST_PORT.toByte()) {
                try {
                    byte[] addressBytes = new byte[4];
                    receiveBuffer.get(addressBytes);
                    InetAddress address = InetAddress.getByAddress(addressBytes);
                    ServerSocket serverSocket = new ServerSocket(0); //get a free port
                    String name = StandardCharsets.UTF_8.decode(receiveBuffer).toString();
                    ByteBuffer sendBuffer = ByteBuffer.allocate(4).putInt(serverSocket.getLocalPort());
                    sendMessage(PacketId.SEND_PORT, sendBuffer, address);
                    Socket clientSocket = serverSocket.accept();
                    clientList.add(new ClientDevice(name,address,serverSocket, clientSocket));
                } catch (UnknownHostException e){
                    //will never happen
                    e.printStackTrace();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public void broadcastFile(Path path){
        try {
            for (ClientDevice client : clientList) {
                Files.copy(path, client.clientSocket.getOutputStream());
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
