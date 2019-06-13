package edu.hm.eem_library.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class SignalPacket extends DataPacket {
    public enum Signal {
        VALID_LOGIN, INVALID_LOGIN, LOGOFF, LIGHTHOUSE_ON, LIGHTHOUSE_OFF;

        private static Signal[] values = null;

        public ByteBuffer insertInBytebuffer(ByteBuffer buf) {
            return buf.putInt(this.ordinal());
        }

        public static Signal extractFromBytebuffer(ByteBuffer buf) {
            int idx = buf.getInt();
            if (Signal.values == null) {
                Signal.values = Signal.values();
            }
            return Signal.values[idx];
        }

    }
    private final Signal signal;

    public SignalPacket(Signal signal) {
        super(Type.SIGNAL);
        this.signal = signal;
    }

    @Override
    protected void writeData(OutputStream os) {
        ByteBuffer bb = ByteBuffer.allocate((int)getSize());
        signal.insertInBytebuffer(bb);
        try {
            os.write(bb.array());
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected long getSize() {
        return INT_BYTES;
    }

    public static Signal readData(InputStream is) {
        byte[] bytes = new byte[INT_BYTES];
        try {
            //noinspection ResultOfMethodCallIgnored
            is.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        return Signal.extractFromBytebuffer(bb);
    }
}
