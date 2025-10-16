package me.urninax.flagdelivery.organisation.services.caching;

import java.util.Set;
import java.util.UUID;

public interface MemberTokensCacheService {
    void addToken(UUID memberId, String hashedToken);

    void removeToken(UUID memberId, String hashedToken);

    Set<String> getMemberTokens(UUID memberId);

    void evictAllMemberTokens(UUID memberId);
}
