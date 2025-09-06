package me.urninax.flagdelivery.shared.security.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import me.urninax.flagdelivery.shared.security.CurrentUser;
import me.urninax.flagdelivery.user.services.UserActivityService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ActivityTrackerFilter extends OncePerRequestFilter{
    private final UserActivityService userActivityService;
    private final CurrentUser currentUser;
    private final ConcurrentHashMap<UUID, Instant> lastTouched = new ConcurrentHashMap<>();
    private static final Duration MIN_INTERVAL = Duration.ofSeconds(60);

    public ActivityTrackerFilter(UserActivityService userActivityService, CurrentUser currentUser){
        this.userActivityService = userActivityService;
        this.currentUser = currentUser;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){
        String uri = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        return uri.startsWith("/actuator") || uri.startsWith("/assets") || uri.startsWith("/error") || uri.startsWith("/api/v1/auth");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            log.info("INVOKED");
            if(auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)){
                UUID userId = currentUser.getUserId();
                Instant now = Instant.now();
                Instant prev = lastTouched.get(userId);

                if(prev == null || Duration.between(prev, now).compareTo(MIN_INTERVAL) >= 0){
                    lastTouched.put(userId, now);

                    String ip = request.getRemoteHost();
                    String ua = request.getHeader("User-Agent");

                    userActivityService.touch(userId, now, ip, ua, MIN_INTERVAL);
                }
            }
        }catch(Exception e){
            log.warn("lastSeen update skipped: {}", e.toString());
        }
        filterChain.doFilter(request, response);
    }
}
