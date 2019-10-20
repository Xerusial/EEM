package edu.hm.eem_library.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.ByteBuffer;

/**
 * Subclass of {@link DataPacket}. For more info on the {@link DataPacket} family, check out
 * {@link DataPacket}.
 * <p>
 * Protocol specification: EEP - Login packet
 *     [4 Byte: Version]
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
    public static Object[] readData(InputStream is) {
        Object[] ret = new Object[2];
        String name = null;
        byte[] bytes = new byte[INT_BYTES];
        try {
            //noinspection ResultOfMethodCallIgnored
            is.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        ret[0] = bb.getInt();
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        try {
            name = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ret[1] = name;
        return ret;
    }

    /**
     * Write a students name to the socket
     *
     * @param os given output stream
     */
    @Override
    protected void writeData(OutputStream os) {
        ByteBuffer bb = ByteBuffer.allocate(INT_BYTES);
        bb.putInt(PROTOCOL_VERSION);
        try {
            os.write(bb.array());
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(os)), true);
        out.println(name);
        out.flush();
    }
}
