package me.urninax.flagdelivery.shared.utils;

import java.util.UUID;

public class CacheKeys{
    private CacheKeys(){}

    public static final String ACCESS_TOKENS = "accessTokens";
    public static final String MEMBER_TOKENS = "memberTokens";
    public static final String ACCESS_TOKEN_RECENTLY_USED = "access_token_recently_used";
    public static final String USER_RECENTLY_SEEN = "user_recently_seen";

    public static String accessToken(String hashedToken){
        return ACCESS_TOKENS + "::" + hashedToken;
    }

    public static String memberTokens(UUID memberId){
        return MEMBER_TOKENS + "::" + memberId;
    }

    public static String accessTokenRecentlyUsed(String hashedToken){
        return ACCESS_TOKEN_RECENTLY_USED + "::" + hashedToken;
    }

    public static String userRecentlySeen(UUID userId){
        return USER_RECENTLY_SEEN + "::" + userId;
    }
}
