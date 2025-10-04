package me.urninax.flagdelivery.organisation.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class InvitationTokenUtils{
    public static String generateToken(){
        byte[] bytes = new byte[32];
        try{
            SecureRandom sr = SecureRandom.getInstanceStrong();
            sr.nextBytes(bytes);
        }catch(NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static byte[] hashToken(String rawToken){
        MessageDigest md;
        try{
            md = MessageDigest.getInstance("SHA-256");
        }catch(NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
        return md.digest(rawToken.getBytes(StandardCharsets.US_ASCII));
    }
}
