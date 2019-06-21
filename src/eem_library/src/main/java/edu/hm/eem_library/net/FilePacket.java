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

public class FilePacket extends DataPacket {
    /*  File Packet:
        [8 Bytes: Size]
        [Size Bytes: File]
     */
    public static final String FILENAME = "sendable_exam";
    public static final String EXAMDIR = "exams";
    private final File f;
    private static final ExamFactory factory = new ExamFactory(ExamFactory.ExamType.TEACHER);
    public FilePacket(File filesdir, String exam) {
        super(Type.EXAMFILE);
        File examFile = new File(filesdir.getPath() + File.separator + EXAMDIR + File.separator + exam);
        File sendableExamFile = new File(filesdir.getPath() + File.separator + FilePacket.FILENAME);
        factory.createSendableVersion(examFile, sendableExamFile);
        this.f = new File(filesdir.getPath() + File.separator + FILENAME);
    }

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
        } catch (FileNotFoundException e){
            e.printStackTrace();
            exit(1);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    @Nullable
    public static TeacherExam readData(InputStream is) {
        byte[] sizeBytes = new byte[LONG_BYTES];
        TeacherExam exam = null;
        try {
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
}
