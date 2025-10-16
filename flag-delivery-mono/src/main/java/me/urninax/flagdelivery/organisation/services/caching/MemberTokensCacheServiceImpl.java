package me.urninax.flagdelivery.organisation.services.caching;

import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.shared.utils.CacheKeys;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@ConditionalOnBean(RestTemplate.class)
public class MemberTokensCacheServiceImpl implements MemberTokensCacheService{
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void addToken(UUID memberId, String hashedToken){
        redisTemplate.opsForSet().add(CacheKeys.memberTokens(memberId), hashedToken);
    }

    @Override
    public void removeToken(UUID memberId, String hashedToken){
        redisTemplate.opsForSet().remove(CacheKeys.memberTokens(memberId), hashedToken);
    }

    @Override
    public Set<String> getMemberTokens(UUID memberId){
        return redisTemplate.opsForSet().members(CacheKeys.memberTokens(memberId));
    }

    @Override
    public void evictAllMemberTokens(UUID memberId){
        Set<String> tokens = getMemberTokens(memberId);

        if(tokens != null){
            tokens.forEach(token -> redisTemplate.delete(CacheKeys.accessToken(token)));
        }

        redisTemplate.delete(CacheKeys.memberTokens(memberId));
    }
}
