package me.urninax.flagdelivery.shared.security.principals;

import lombok.*;
import me.urninax.flagdelivery.user.models.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

@Builder
@AllArgsConstructor
@Setter
@Getter
public class UserPrincipal implements UserDetails{
    private final UUID id;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;

    public UserPrincipal(UserEntity user){
        this.id = user.getId();
        this.username = user.getEmail();
        this.password = user.getPassword();
        this.authorities = user.getInternalRoles()
                .stream()
                .map(authority ->
                        new SimpleGrantedAuthority(String.format("ROLE_%s", authority.name())))
                .toList();
        this.enabled = user.isEnabled();
    }

    @Override
    public boolean isAccountNonExpired(){
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked(){
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired(){
        return UserDetails.super.isCredentialsNonExpired();
    }
}
