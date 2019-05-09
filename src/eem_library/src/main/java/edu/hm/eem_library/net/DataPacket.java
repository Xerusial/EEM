package edu.hm.eem_library.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public abstract class DataPacket {
    /* Protocol specification: EEP - E-Reader Exam Protocol
       [4 Byte: Version]
       [4 Byte: Type]
       [8 Bytes: Size]
       [Size Bytes: Data]
       Data specification can be found in the respective child-classes.
     */
    public enum Type {
        LOGIN, EXAMFILE, SIGNAL;

        private static Type[] values = null;

        public ByteBuffer insertInBytebuffer(ByteBuffer buf) {
            return buf.putInt(this.ordinal());
        }

        public static Type extractFromBytebuffer(ByteBuffer buf) {
            int idx = buf.getInt();
            if (Type.values == null) {
                Type.values = Type.values();
            }
            return Type.values[idx];
        }

    }

    private final Type type;
    // incremental protocol version
    static final int PROTOCOL_VERSION = 0;
    private static final int HEADER_FIELDS = 3;
    static final int INT_BYTES = 4;
    private static final int LONG_BYTES = 8;
    private static final int HEADER_SIZE = 2*INT_BYTES + LONG_BYTES;

    DataPacket(Type type) {
        this.type = type;
    }

    private void writeHeader(OutputStream os){
        ByteBuffer bb = ByteBuffer.allocate(HEADER_SIZE);
        bb.putInt(PROTOCOL_VERSION);
        type.insertInBytebuffer(bb);
        bb.putLong(getSize());
        try {
            os.write(bb.array());
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Object[] readHeader(InputStream is){
        Object[] ret = new Object[HEADER_FIELDS];
        byte[] bytes = new byte[HEADER_SIZE];
        try {
            //noinspection ResultOfMethodCallIgnored
            is.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        ret[0] = bb.getInt();
        ret[1] = Type.extractFromBytebuffer(bb);
        ret[2] = bb.getLong();
        return ret;
    }

    protected abstract void writeData(OutputStream os);
    protected abstract long getSize();

    public final void sendData(OutputStream os) {
        writeHeader(os);
        writeData(os);
    }
}
