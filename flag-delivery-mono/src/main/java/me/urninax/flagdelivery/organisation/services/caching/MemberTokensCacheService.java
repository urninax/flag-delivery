package me.urninax.flagdelivery.organisation.services.caching;

import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.shared.utils.CacheKeys;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberTokensCacheService{
    private final RedisTemplate<String, String> redisTemplate;

    public void addToken(UUID memberId, String hashedToken){
        redisTemplate.opsForSet().add(CacheKeys.memberTokens(memberId), hashedToken);
    }

    public void removeToken(UUID memberId, String hashedToken){
        redisTemplate.opsForSet().remove(CacheKeys.memberTokens(memberId), hashedToken);
    }

    public Set<String> getMemberTokens(UUID memberId){
        return redisTemplate.opsForSet().members(CacheKeys.memberTokens(memberId));
    }

    public void evictAllMemberTokens(UUID memberId){
        Set<String> tokens = getMemberTokens(memberId);

        if(tokens != null){
            tokens.forEach(token -> redisTemplate.delete(CacheKeys.accessToken(token)));
        }

        redisTemplate.delete(CacheKeys.memberTokens(memberId));
    }
}
