package edu.hm.eem_library.net;

import androidx.annotation.Nullable;

import org.apache.commons.io.input.BoundedInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import edu.hm.eem_library.model.ExamFactory;
import edu.hm.eem_library.model.TeacherExam;

import static java.lang.System.exit;

/**
 * Subclass of {@link DataPacket}. For more info on the {@link DataPacket} family, check out
 * {@link DataPacket}.
 * <p>
 * Protocol specification: EEP - File packet
 *     [8 Bytes: Size]
 *     [Size Bytes: File]
 */
public class FilePacket extends DataPacket {
    public static final String EXAMDIR = "exams";
    private static final String FILENAME = "sendable_exam";
    private static final ExamFactory factory = new ExamFactory(ExamFactory.ExamType.TEACHER);
    private final File f;

    /**
     * Constructor
     *
     * @param filesdir root data dir. For storage purposes of the temporary sendable examfile
     * @param exam     name of exam to be sent
     */
    public FilePacket(File filesdir, String exam) {
        super(Type.EXAMFILE);
        File examFile = new File(filesdir.getPath() + File.separator + EXAMDIR + File.separator + exam);
        File sendableExamFile = new File(filesdir.getPath() + File.separator + FilePacket.FILENAME);
        factory.createSendableVersion(examFile, sendableExamFile);
        this.f = new File(filesdir.getPath() + File.separator + FILENAME);
    }

    /**
     * Read YAML file from a sockets incoming file packet
     *
     * @param is incoming data
     * @return an exam object constructed from the YAML file
     */
    @Nullable
    public static TeacherExam readData(InputStream is) {
        byte[] sizeBytes = new byte[LONG_BYTES];
        TeacherExam exam = null;
        try {
            //noinspection ResultOfMethodCallIgnored
            is.read(sizeBytes);
            long size = ByteBuffer.wrap(sizeBytes).getLong();
            BoundedInputStream bis = new BoundedInputStream(is, size);
            exam = (TeacherExam) factory.extract(bis);
            //Do not close bis, as this will close the socket!
        } catch (IOException e) {
            e.printStackTrace();
        }
        return exam;
    }

    /**
     * Write a file packet to a TCP socket output stream
     *
     * @param os given output stream
     */
    @Override
    protected void writeData(OutputStream os) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(new byte[LONG_BYTES]);
            os.write(buffer.putLong(f.length()).array());
            FileInputStream fis = new FileInputStream(f);
            byte[] buf = new byte[4096];
            int read;
            while ((read = fis.read(buf, 0, buf.length)) != -1) {
                os.write(buf, 0, read);
                os.flush();
            }
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            exit(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
