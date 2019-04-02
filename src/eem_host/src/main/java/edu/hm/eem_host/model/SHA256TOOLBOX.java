package edu.hm.eem_host.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

final class SHA256TOOLBOX {
    private SHA256TOOLBOX(){}

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
