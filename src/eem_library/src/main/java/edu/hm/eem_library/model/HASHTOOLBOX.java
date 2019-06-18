package edu.hm.eem_library.model;

import android.content.Context;
import android.util.Pair;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Iterator;

public final class HASHTOOLBOX {
    private HASHTOOLBOX(){}

    public static Pair<byte[], byte[]> genMD5s(Context context, InputStream is) throws IOException {
        try {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            DigestInputStream digis = new DigestInputStream(is, md);
            byte[] md5withoutAnn = genMD5WithoutAnnotations(context, digis);
            Pair<byte[], byte[]> ret = Pair.create(digis.getMessageDigest().digest(), md5withoutAnn);
            digis.close();
            return ret;
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    private static byte[] genMD5WithoutAnnotations(Context context, InputStream is) throws IOException {
        //For performance
        PDFBoxResourceLoader.init(context);
        byte[] ret;
        PDDocument document = PDDocument.load(is);
        Iterator<PDPage> it = document.getDocumentCatalog().getPages().iterator();
        for (; it.hasNext();) {
            it.next().setAnnotations(null);
        }
        File tmp = File.createTempFile("pdfchange", 0, context.getCacheDir());
        document.save(tmp);
        document.close();
        FileInputStream fis = new FileInputStream(tmp);
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] bytes = new byte[4096];
        int read = 0;
        while ((read = fis.read(bytes)) > 0) {
            md.update(bytes, 0, read);
        }
        byte[] md5 = md.digest();
        fis.close();
        return md5;
    }

    static byte[] genSha256(String pw, byte[] salt){
        try {
            // Create MessageDigest instance for SHA256
            MessageDigest md = MessageDigest.getInstance("SHA256");
            //Add salt bytes to digest
            md.update(salt);
            //Get the password hash
            return md.digest(pw.getBytes());
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    static byte[] genSalt(){
        try {
            //SecureRandom generator
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            //Create array for salt
            byte[] salt = new byte[16];
            //Get a random salt
            sr.nextBytes(salt);
            //return salt
            return salt;
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }
}
