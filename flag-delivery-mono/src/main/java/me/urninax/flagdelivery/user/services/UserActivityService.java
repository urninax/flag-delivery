package me.urninax.flagdelivery.user.services;

import jakarta.transaction.Transactional;
import me.urninax.flagdelivery.user.repositories.UserActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class UserActivityService{

    private final UserActivityRepository userActivityRepository;

    @Autowired
    public UserActivityService(UserActivityRepository userActivityRepository){
        this.userActivityRepository = userActivityRepository;
    }

    @Transactional
    @Async("userActivityExecutor")
    public void touch(UUID userId, Instant lastSeen, String ip, String ua, Duration window){
        userActivityRepository.upsertLastSeen(userId, lastSeen, ip, ua, window.toSecondsPart());
    }
}
