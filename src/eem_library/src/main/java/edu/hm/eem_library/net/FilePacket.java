package edu.hm.eem_library.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static java.lang.System.exit;

public class FilePacket extends DataPacket {
    /*  File Packet: TODO!!!
        [4 Bytes: Size]
        [Size Bytes: File]
     */
    private File f;
    public FilePacket(File f) {
        super(Type.EXAMFILE);
        this.f = f;
    }

    @Override
    protected void writeData(OutputStream os) {
        try {
            FileInputStream fis = new FileInputStream(f);
            byte[] buf = new byte[1024];
            int read;
            while ((read = fis.read(buf, 0, buf.length)) != -1) {
                os.write(buf, 0, read);
                os.flush();
            }
            fis.close();
        } catch (FileNotFoundException e){
            e.printStackTrace();
            exit(1);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    protected long getSize() {
        return f.length();
    }

    static void readData(InputStream is, long size) {
    }
}
