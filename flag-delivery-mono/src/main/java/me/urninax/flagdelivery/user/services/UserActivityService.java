package me.urninax.flagdelivery.user.services;


import me.urninax.flagdelivery.shared.utils.CacheKeys;
import me.urninax.flagdelivery.user.repositories.UserActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class UserActivityService{

    private final UserActivityRepository userActivityRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public UserActivityService(UserActivityRepository userActivityRepository, RedisTemplate<String, String> redisTemplate){
        this.userActivityRepository = userActivityRepository;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    @Async("activityExecutor")
    public void touch(UUID userId, Instant lastSeen, String ip, String ua){
        String cacheKey = CacheKeys.userRecentlySeen(userId);
        Boolean wasSet = redisTemplate.opsForValue().setIfAbsent(cacheKey, "tracking", Duration.ofMinutes(1));

        if(Boolean.TRUE.equals(wasSet)){
            userActivityRepository.upsertLastSeen(userId, lastSeen, ip, ua);
        }
    }
}
