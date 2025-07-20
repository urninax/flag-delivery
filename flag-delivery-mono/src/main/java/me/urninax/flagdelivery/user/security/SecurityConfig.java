package me.urninax.flagdelivery.user.security;

import me.urninax.flagdelivery.user.services.UsersService;
import me.urninax.flagdelivery.user.services.UsersServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig{
    private final UsersServiceImpl usersService;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Autowired
    public SecurityConfig(UsersServiceImpl usersService, PasswordEncoder passwordEncoder, Environment environment){
        this.usersService = usersService;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        var authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);

        authenticationManagerBuilder
                .userDetailsService(usersService)
                .passwordEncoder(passwordEncoder);

        AuthenticationManager authManager = authenticationManagerBuilder.build();

        AuthenticationFilter authenticationFilter = new AuthenticationFilter(authManager, usersService, environment);
        authenticationFilter.setFilterProcessesUrl(environment.getProperty("login.url"));

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement((session)
                        -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(HttpMethod.POST, "/signup").permitAll()
                        .requestMatchers(HttpMethod.POST, "/signin").permitAll())
                .addFilter(authenticationFilter)
                .authenticationManager(authManager);

        return http.build();
    }
}
