package me.urninax.flagdelivery.organisation.shared;

import lombok.*;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;

import java.time.Instant;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InvitationMailDTO{
    private UUID invitationId;
    private String organisationName;
    private String message;
    private String token;
    private String email;
    private OrgRole role;
    private Instant expiresAt;
}
