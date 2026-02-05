package me.urninax.flagdelivery.organisation.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.organisation.repositories.AccessTokenRepository;
import me.urninax.flagdelivery.shared.utils.CacheKeys;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AccessTokenActivityService{
    private final RedisTemplate<String, String> redisTemplate;
    private final AccessTokenRepository accessTokenRepository;

    @Transactional
    @Async("activityExecutor")
    public void updateRecentlyUsed(String hashedToken){
        String cacheKey = CacheKeys.accessTokenRecentlyUsed(hashedToken);

        Boolean wasSet = redisTemplate.opsForValue()
                .setIfAbsent(cacheKey, "tracking", Duration.ofMinutes(1));

        if(Boolean.TRUE.equals(wasSet)){
            accessTokenRepository.updateRecentlyUsed(hashedToken);
        }
    }
}
