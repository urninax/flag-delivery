package me.urninax.flagdelivery.user.services;

import me.urninax.flagdelivery.user.ui.models.requests.SignupRequest;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UsersService extends UserDetailsService{
    void createUser(SignupRequest signupRequest);
}
