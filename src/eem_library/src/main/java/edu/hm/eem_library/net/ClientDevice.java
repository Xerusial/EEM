package edu.hm.eem_library.net;

import java.io.OutputStream;

public class ClientDevice {
    String clientName;
    OutputStream outputStream;

    public ClientDevice(String clientName, OutputStream outputStream) {
        this.clientName = clientName;
        this.outputStream = outputStream;
    }
}
