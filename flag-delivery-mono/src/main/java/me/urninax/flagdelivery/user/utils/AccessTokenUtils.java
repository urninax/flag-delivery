package me.urninax.flagdelivery.user.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class AccessTokenUtils{
    public static String toHint(String token){
        return String.format("api-****%s", token.substring(token.length() - 4));
    }

    public static String hashSha256(String token){
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
        }catch(NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
    }

}
