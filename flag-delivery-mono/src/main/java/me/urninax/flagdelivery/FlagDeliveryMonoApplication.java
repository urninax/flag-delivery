package me.urninax.flagdelivery;

import me.urninax.flagdelivery.projectsenvs.utils.ReservedWordsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;

@SpringBootApplication
@EnableConfigurationProperties(ReservedWordsProperties.class)
@EnableCaching
public class FlagDeliveryMonoApplication{

    public static void main(String[] args){
        SpringApplication.run(FlagDeliveryMonoApplication.class, args);
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public Clock clock(){
        return Clock.systemUTC();
    }
}
