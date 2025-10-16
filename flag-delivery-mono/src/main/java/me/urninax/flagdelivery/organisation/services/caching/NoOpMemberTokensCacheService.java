package me.urninax.flagdelivery.organisation.services.caching;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Set;
import java.util.UUID;

@Service
@ConditionalOnMissingBean(RestTemplate.class)
public class NoOpMemberTokensCacheService implements MemberTokensCacheService{
    @Override
    public void addToken(UUID memberId, String hashedToken) {

    }

    @Override
    public void removeToken(UUID memberId, String hashedToken) {

    }

    @Override
    public Set<String> getMemberTokens(UUID memberId) {
        return Set.of();
    }

    @Override
    public void evictAllMemberTokens(UUID memberId) {

    }
}
