package edu.hm.eem_library.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class LoginPacket extends DataPacket {
    /*  Login Packet:
        [Newline terminated String: name]
     */
    private String name;

    public LoginPacket(String name) {
        super(Type.LOGIN);
        this.name = name;
    }

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

    @Override
    protected void writeData(OutputStream os) {
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(os)), true);
        out.println(name);
        out.flush();
    }
}
