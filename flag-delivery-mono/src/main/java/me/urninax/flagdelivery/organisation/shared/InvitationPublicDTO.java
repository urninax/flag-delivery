package me.urninax.flagdelivery.organisation.shared;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.urninax.flagdelivery.organisation.models.invitation.InvitationStatus;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;

import java.time.Instant;

@NoArgsConstructor
@Getter
@Setter
public class InvitationPublicDTO{
    @JsonProperty("organisation_name")
    private String organisationName;

    @JsonProperty("invited_by")
    private String invitedBy;

    @JsonProperty("role")
    private OrgRole role;

    @JsonProperty("status")
    private InvitationStatus status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("expires_at")
    private Instant expiresAt;
}
