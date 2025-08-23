package me.urninax.flagdelivery.organisation.shared;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.urninax.flagdelivery.organisation.models.invitation.InvitationStatus;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;

import java.time.Instant;

@NoArgsConstructor
@Getter
@Setter
public class InvitationDTO{
    private String organisationName;
    private String invitedBy;
    private OrgRole role;
    private InvitationStatus status;
    private String message;
    private Instant expiresAt;
}
