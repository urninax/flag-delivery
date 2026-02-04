package me.urninax.flagdelivery.shared.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.urninax.flagdelivery.organisation.models.membership.Membership;
import me.urninax.flagdelivery.organisation.shared.AccessTokenPrincipalDTO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;

@Configuration
@Slf4j
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class RedisConfig{

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper springObjectMapper){
        GenericJackson2JsonRedisSerializer genericSerializer = new GenericJackson2JsonRedisSerializer(springObjectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                genericSerializer
                        )
                );

        // accessTokens cache configuration
        Jackson2JsonRedisSerializer<AccessTokenPrincipalDTO> accessTokenSerializer =
                new Jackson2JsonRedisSerializer<>(springObjectMapper, AccessTokenPrincipalDTO.class);

        RedisCacheConfiguration accessTokenCache = defaultConfig.entryTtl(Duration.ofMinutes(10))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(accessTokenSerializer));


        // memberships cache configuration
        Jackson2JsonRedisSerializer<Membership> membershipSerializer =
                new Jackson2JsonRedisSerializer<>(springObjectMapper, Membership.class);

        RedisCacheConfiguration membershipsCache = defaultConfig.entryTtl(Duration.ofMinutes(15))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(membershipSerializer));


        long start = System.nanoTime();
        connectionFactory.getConnection().ping();
        long end = System.nanoTime();
        log.info("Redis warmup ping took {} ms", (end - start) / 1_000_000);

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .enableStatistics()
                .withCacheConfiguration("accessTokens", accessTokenCache)
                .withCacheConfiguration("memberships", membershipsCache)
                .build();
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(RedisSerializer.string());
        template.setValueSerializer(RedisSerializer.string());
        return template;
    }
}
