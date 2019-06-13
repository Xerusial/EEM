package edu.hm.eem_library.net;

import org.apache.commons.io.input.BoundedInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import edu.hm.eem_library.model.ExamFactory;
import edu.hm.eem_library.model.TeacherExam;

import static java.lang.System.exit;

public class FilePacket extends DataPacket {
    /*  File Packet: TODO!!!
        [4 Bytes: Size]
        [Size Bytes: File]
     */
    public static final String FILENAME = "sendable_exam";
    public static final String EXAMDIR = "exams";
    private final File f;
    private static final ExamFactory factory = new ExamFactory(ExamFactory.ExamType.TEACHER);
    public FilePacket(File filesdir, String exam) {
        super(Type.EXAMFILE);
        File examDir = new File(filesdir.getPath() + File.separator + EXAMDIR);
        factory.createSendableVersion(filesdir,examDir, exam);
        this.f = new File(filesdir.getPath() + File.separator + FILENAME);
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

    static TeacherExam readData(InputStream is, long size) {
        BoundedInputStream bis = new BoundedInputStream(is, size);
        TeacherExam exam = (TeacherExam) factory.extract(bis);
        try {
            bis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return exam;
    }
}
