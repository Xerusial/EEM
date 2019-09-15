package edu.hm.eem_library.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Subclass of {@link DataPacket}. For more info on the {@link DataPacket} family, check out
 * {@link DataPacket}.
 * <p>
 * Protocol specification: EEP - Login packet
 *     [Newline terminated String: name]
 */
public class LoginPacket extends DataPacket {
    private final String name;

    /**
     * Constructor
     *
     * @param name The students name
     */
    public LoginPacket(String name) {
        super(Type.LOGIN);
        this.name = name;
    }

    /**
     * Read a students name from socket stream
     *
     * @param is given input stream
     * @return the name of the student trying to log in
     */
    public static String readData(InputStream is) {
        String ret = null;
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        try {
            ret = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * Write a students name to the socket
     *
     * @param os given output stream
     */
    @Override
    protected void writeData(OutputStream os) {
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(os)), true);
        out.println(name);
        out.flush();
    }
}
