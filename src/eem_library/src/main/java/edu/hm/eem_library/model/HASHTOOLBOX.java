package edu.hm.eem_library.model;

import android.net.Uri;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public final class HASHTOOLBOX {
    private HASHTOOLBOX(){}

    public static byte[] genMD5(InputStream is) throws IOException {
        try {
            // Create MessageDigest instance for SHA256
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = new byte[4096];
            int read = 0;
            while ((is.read(bytes)) != -1) {
                md.update(bytes, 0, read);
            }
            return md.digest();
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
            System.exit(1);
        }
        return null;
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
