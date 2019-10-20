package edu.hm.eem_library.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Subclass of {@link DataPacket}. For more info on the {@link DataPacket} family, check out
 * {@link DataPacket}
 * <p>
 * Protocol specification: EEP - Signal packet
 *     [4 byte: signal code]
 * Signal Types:
 *     0: Invalid login: A student with the same name has already logged into the exam
 *     1: Invalid login: The students protocol version is too high
 *     2: Invalid login: The students protocol version is too low
 *     3: Logoff: Student terminated application
 *     4: Lighthouse on: Show lighthouse symbol on students device to identify it
 *     5: Lighthouse off: Turn lighthouse off
 *     6: All doc accepted: Signal returned by the students device if all documents are ok
 *     7: Lock: Remove all not accepted documents from students device and start monitoring them
 *     8: Notificationdrawer pulled: Student has opened his notification drawer (which he should not)
 */
public class SignalPacket extends DataPacket {
    private final Signal signal;

    /**
     * Constructor
     *
     * @param signal to be sent
     */
    public SignalPacket(Signal signal) {
        super(Type.SIGNAL);
        this.signal = signal;
    }

    /**
     * Read a signal from a sockets input stream
     *
     * @param is sockets input stream
     * @return the captured signal
     */
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

    /**
     * Write a signal to a sockets output stream
     *
     * @param os given output stream
     */
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

    /**
     * The signal enum defining the different kinds of signals
     */
    public enum Signal {
        INVALID_LOGIN_NAME, INVALID_LOGIN_VERS_HIGH, INVALID_LOGIN_VERS_LOW, LOGOFF, LIGHTHOUSE_ON, LIGHTHOUSE_OFF, ALL_DOC_ACCEPTED, LOCK, NOTIFICATIONDRAWER_PULLED;

        private static Signal[] values = null;

        static Signal extractFromBytebuffer(ByteBuffer buf) {
            int idx = buf.getInt();
            if (Signal.values == null) {
                Signal.values = Signal.values();
            }
            return Signal.values[idx];
        }

        /**
         * Insert the signal ordinal into a bytebuffer
         *
         * @param buf the byte buffer
         * @return the byte buffer
         */
        ByteBuffer insertInBytebuffer(ByteBuffer buf) {
            return buf.putInt(this.ordinal());
        }

    }
}
