package me.urninax.flagdelivery.user.security;

import jakarta.servlet.DispatcherType;
import me.urninax.flagdelivery.user.security.filters.AuthenticationFilter;
import me.urninax.flagdelivery.user.security.filters.BearerAuthenticationFilter;
import me.urninax.flagdelivery.user.services.UsersServiceImpl;
import me.urninax.flagdelivery.user.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig{
    private final UsersServiceImpl usersService;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;
    private final JwtUtils jwtUtils;

    @Autowired
    public SecurityConfig(UsersServiceImpl usersService, PasswordEncoder passwordEncoder, Environment environment, JwtUtils jwtUtils){
        this.usersService = usersService;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
        this.jwtUtils = jwtUtils;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationManager manager,
                                                   BearerTokenAuthenticationConverter converter) throws Exception{
        var authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);

        authenticationManagerBuilder
                .userDetailsService(usersService)
                .passwordEncoder(passwordEncoder);

        AuthenticationManager authManager = authenticationManagerBuilder.build();

        AuthenticationFilter authenticationFilter = new AuthenticationFilter(authManager, jwtUtils);
        authenticationFilter.setFilterProcessesUrl(environment.getProperty("login.url"));

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement((session)
                        -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((authorize) -> authorize
                        .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/signup").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/signin").permitAll()
                        .requestMatchers("/error**").permitAll()
                        .anyRequest().authenticated())
                .addFilter(authenticationFilter)
                .addFilterBefore(new BearerAuthenticationFilter(manager, converter), UsernamePasswordAuthenticationFilter.class)
                .authenticationManager(authManager);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationProvider... providers){
        return new ProviderManager(List.of(providers));
    }
}
