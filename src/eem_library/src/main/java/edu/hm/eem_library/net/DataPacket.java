package edu.hm.eem_library.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * The base class for all TCP packets in this project. This is its hierarchy:
 * <p>
 *                .----------------.
 *                | << abstract >> |
 *           .--->|   DataPacket   |<-----.
 *           |    '----------------'      |
 *           |             ^              |
 *           |             |              |
 * .-------------------.   |   .---------------------.
 * |   SignalPacket    |   |   |     FilePacket      |
 * '-------------------'   |   '---------------------'
 *                         |
 *            .-------------------------.
 *            |       LoginPacket       |
 *            '-------------------------'
 *
 * A {@link DataPacket} consist of a header with the following fields:
 * <p>
 * Protocol specification: EEP - E-Reader TeacherExam Protocol
 *     [4 Byte: Version]
 *     [4 Byte: Type]
 *     [following Bytes: Data]
 * Data Types:
 *     0: Login: carrying the name of the student
 *     1: File: carrying a YAML file containing information, which documents are allowed in the exam
 *              and also a password hash for the teacher to manually accept exams being rejected by
 *              the algorithm
 *     2: Signal: Various signals. {@link SignalPacket} for more info
 * <p>
 * Data specification can be found in the respective child-classes.
 */
public abstract class DataPacket {
    // incremental protocol version
    static final int PROTOCOL_VERSION = 0;
    static final int INT_BYTES = 4;
    static final int LONG_BYTES = 8;
    private static final int HEADER_FIELDS = 2;
    private static final int HEADER_SIZE = HEADER_FIELDS * INT_BYTES;
    private final Type type;

    DataPacket(Type type) {
        this.type = type;
    }

    /**
     * Read the header data from an input stream (Socket input stream)
     *
     * @param is given inputstream
     * @return the read data
     */
    static Object[] readHeader(InputStream is) {
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
        return ret;
    }

    /**
     * Write this objects header data in output stream (Socket output stream)
     *
     * @param os given output stream
     */
    private void writeHeader(OutputStream os) {
        ByteBuffer bb = ByteBuffer.allocate(HEADER_SIZE);
        bb.putInt(PROTOCOL_VERSION);
        type.insertInBytebuffer(bb);
        try {
            os.write(bb.array());
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Template for a method writing packet type specific data to output stream
     *
     * @param os given output stream
     */
    protected abstract void writeData(OutputStream os);

    private void sendData(OutputStream os) {
        writeHeader(os);
        writeData(os);
    }

    /**
     * Enum for the packet type
     */
    public enum Type {
        LOGIN, EXAMFILE, SIGNAL;

        private static Type[] values = null;

        static Type extractFromBytebuffer(ByteBuffer buf) {
            int idx = buf.getInt();
            if (Type.values == null) {
                Type.values = Type.values();
            }
            return Type.values[idx];
        }

        ByteBuffer insertInBytebuffer(ByteBuffer buf) {
            return buf.putInt(this.ordinal());
        }

    }

    /**
     * A thread being created every time a packet has to be sent.
     */
    public static final class SenderThread extends Thread {
        private final Socket socket;
        private final DataPacket dp;

        public SenderThread(Socket socket, DataPacket dp) {
            this.socket = socket;
            this.dp = dp;
        }

        /**
         * Runnable sending the data of the packet object out
         */
        @Override
        public void run() {
            super.run();
            try {
                if (!socket.isClosed()) {
                    OutputStream os = socket.getOutputStream();
                    dp.sendData(os);
                }
                //Do not close outputstream, as it cannot be opened again
                //Socket will be closed by the receiverThread
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
