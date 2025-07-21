package me.urninax.flagdelivery.user.security;

import lombok.*;

import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class UserPrincipal{
    private UUID id;
    private String email;
}
