package me.urninax.flagdelivery.shared.security.principals;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AccessTokenPrincipal{
    private UUID ownerId;
    private UUID organisationId;
    private Collection<? extends GrantedAuthority> authorities;
}
