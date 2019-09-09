package edu.hm.eem_library.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class SignalPacket extends DataPacket {
    private final Signal signal;

    public SignalPacket(Signal signal) {
        super(Type.SIGNAL);
        this.signal = signal;
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

    @Override
    protected void writeData(OutputStream os) {
        ByteBuffer bb = ByteBuffer.allocate(INT_BYTES);
        signal.insertInBytebuffer(bb);
        try {
            os.write(bb.array());
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public enum Signal {
        VALID_LOGIN, INVALID_LOGIN, LOGOFF, LIGHTHOUSE_ON, LIGHTHOUSE_OFF, ALL_DOC_ACCEPTED, LOCK, NOTIFICATIONDRAWER_PULLED;

        private static Signal[] values = null;

        public static Signal extractFromBytebuffer(ByteBuffer buf) {
            int idx = buf.getInt();
            if (Signal.values == null) {
                Signal.values = Signal.values();
            }
            return Signal.values[idx];
        }

        public ByteBuffer insertInBytebuffer(ByteBuffer buf) {
            return buf.putInt(this.ordinal());
        }

    }
}
