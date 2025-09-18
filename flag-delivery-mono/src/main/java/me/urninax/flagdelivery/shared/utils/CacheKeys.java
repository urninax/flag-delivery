package me.urninax.flagdelivery.shared.utils;

import java.util.UUID;

public class CacheKeys{
    private CacheKeys(){}

    public static final String ACCESS_TOKENS = "accessTokens";
    public static final String MEMBER_TOKENS = "memberTokens";

    public static String accessToken(String hashedToken){
        return ACCESS_TOKENS + "::" + hashedToken;
    }

    public static String memberTokens(UUID memberId){
        return MEMBER_TOKENS + "::" + memberId;
    }
}
