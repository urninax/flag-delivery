package me.urninax.flagdelivery.shared.security.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import me.urninax.flagdelivery.organisation.services.AccessTokenActivityService;
import me.urninax.flagdelivery.shared.security.CurrentUser;
import me.urninax.flagdelivery.shared.security.principals.AccessTokenPrincipal;
import me.urninax.flagdelivery.user.services.UserActivityService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Slf4j
public class ActivityTrackerFilter extends OncePerRequestFilter{
    private final UserActivityService userActivityService;
    private final AccessTokenActivityService accessTokenActivityService;
    private final CurrentUser currentUser;

    public ActivityTrackerFilter(UserActivityService userActivityService, AccessTokenActivityService accessTokenActivityService, CurrentUser currentUser){
        this.userActivityService = userActivityService;
        this.accessTokenActivityService = accessTokenActivityService;
        this.currentUser = currentUser;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){
        String uri = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        return uri.startsWith("/actuator") ||
                uri.startsWith("/assets") ||
                uri.startsWith("/error") ||
                uri.startsWith("/api/v1/auth");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if(auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)){
                handleUserActivity(request);

                if(auth.getPrincipal() instanceof AccessTokenPrincipal principal){
                    accessTokenActivityService.updateRecentlyUsed(principal.getHashedToken());
                }
            }
        }catch(Exception e){
            log.warn("Activity tracking skipped: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    private void handleUserActivity(HttpServletRequest request){
        UUID userId = currentUser.getUserId();
        String ip = request.getRemoteHost();
        String ua = request.getHeader("User-Agent");

        userActivityService.touch(userId, Instant.now(), ip, ua);
    }
}
