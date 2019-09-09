package edu.hm.eem_library.model;

import android.content.Context;

import com.tom_roush.pdfbox.cos.COSBase;
import com.tom_roush.pdfbox.pdfwriter.COSWriter;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import org.apache.commons.io.output.NullOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Iterator;

public final class HASHTOOLBOX {
    private HASHTOOLBOX() {
    }

    static ExamDocument.Identifiers genDocMD5s(Context context, InputStream is, WhichHash which) throws IOException {
        try {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            DigestInputStream digis = new DigestInputStream(is, md);
            ExamDocument.Identifiers ids = new ExamDocument.Identifiers();
            switch (which) {
                case NORMAL:
                    //noinspection StatementWithEmptyBody
                    while ((digis.read()) != -1) ;
                    break;
                case NON_ANNOTATED:
                    digis.on(false); //disable hashing
                    // fallthrough
                case BOTH:
                    genMD5WithoutAnnotations(ids, context, digis);
                    break;
            }
            if (which != WhichHash.NON_ANNOTATED)
                ids.hash = digis.getMessageDigest().digest();
            digis.close();
            return ids;
        } catch (NoSuchAlgorithmException e) {
            // Android does always feature MD5
            e.printStackTrace();
        }
        return null;
    }

    private static void stripDocument(PDDocument document, COSWriter writer) throws IOException {
        Iterator<PDPage> it = document.getDocumentCatalog().getPages().iterator();
        for (; it.hasNext(); ) {
            PDPage page = it.next();
            page.setAnnotations(null);
            writer.doWriteObject(page.getCOSObject());
            for (COSBase base : page.getCOSObject().getValues()) {
                writer.doWriteObject(base.getCOSObject());
            }
        }
        writer.close();
    }

    private static void genMD5WithoutAnnotations(ExamDocument.Identifiers ids, Context context, InputStream is) throws IOException {
        //For performance
        PDFBoxResourceLoader.init(context);
        PDDocument document = PDDocument.load(is);
        //Write to null. If you want to take a look at the output,
        //write this to a tmp file.
        NullOutputStream nos = new NullOutputStream();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            DigestOutputStream digos = new DigestOutputStream(nos, md);
            stripDocument(document, new COSWriter(digos));
            ids.nonAnnotatedHash = digos.getMessageDigest().digest();
            digos.close();
            nos.close();
            document.close();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    static byte[] genSha256(String pw, byte[] salt) {
        try {
            // Create MessageDigest instance for SHA256
            MessageDigest md = MessageDigest.getInstance("SHA256");
            //Add salt bytes to digest
            md.update(salt);
            //Get the password hash
            return md.digest(pw.getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    static byte[] genSalt() {
        try {
            //SecureRandom generator
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            //Create array for salt
            byte[] salt = new byte[16];
            //Get a random salt
            sr.nextBytes(salt);
            //return salt
            return salt;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public enum WhichHash {
        NORMAL, NON_ANNOTATED, BOTH;

        static WhichHash fromDoc(ExamDocument doc) {
            if (doc.getHash() == null && doc.getNonAnnotatedHash() != null) {
                return NON_ANNOTATED;
            } else if (doc.getHash() != null && doc.getNonAnnotatedHash() == null) {
                return NORMAL;
            } else {
                return BOTH;
            }
        }
    }
}
