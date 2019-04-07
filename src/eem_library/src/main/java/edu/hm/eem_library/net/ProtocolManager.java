package edu.hm.eem_library.net;

import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Stack;

import static java.lang.System.exit;

public abstract class ProtocolManager {
    public enum PacketId{
        // only 256 values available!!!
        REQUEST_PORT, SEND_PORT;

        private static PacketId[] values = null;

        public byte toByte(){
            return (byte) ordinal();
        }

        public static PacketId fromByte(byte b){
            return fromInt(b);
        }

        public static PacketId fromInt(int i) {
            if(PacketId.values == null) {
                PacketId.values = PacketId.values();
            }
            return PacketId.values[i];
        }
    }

    public static final int SETUP_PORT = 31041;

    protected final Stack<byte[]> udpStack = new Stack<>();
    private DatagramSocket datagramSocket;
    private UdpReceiverThread udpReceiverThread;
    private Observer stackObserver;

    public ProtocolManager(){
        try {
            datagramSocket = new DatagramSocket();
            udpReceiverThread = new UdpReceiverThread();
            stackObserver = new Observer() {
                @Override
                public void onChanged(@Nullable Object o) {
                    onUdpReceive();
                }
            };
            udpReceiverThread.run();
        } catch (SocketException e) {
            e.printStackTrace();
            exit(1);
        }
    }

    protected abstract void onUdpReceive();

    public boolean sendMessage(PacketId id, ByteBuffer message, InetAddress address){
        boolean ret = true;
        message.flip(); // from here, limit represents the actual data size of the buffer
        int msgLength = message.limit() + 1;
        ByteBuffer messageBuffer = ByteBuffer.allocate(msgLength).put(id.toByte()).put(message);
        DatagramPacket p = new DatagramPacket(messageBuffer.array(), msgLength, address, SETUP_PORT);
        try {
            datagramSocket.send(p);
        } catch (IOException e){
            e.printStackTrace();
            ret = false;
        }
        return ret;
    }

    private class UdpReceiverThread implements Runnable {
        private DatagramPacket p;
        @Override
        public void run() {
            try {
                datagramSocket.receive(p);
                udpStack.push(p.getData());
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
