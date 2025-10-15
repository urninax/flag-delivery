package me.urninax.flagdelivery.organisation.shared;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import me.urninax.flagdelivery.organisation.models.invitation.InvitationStatus;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class InvitationOrganisationDTO{
    @JsonProperty("email")
    private String email;

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

    @JsonProperty("accepted_at")
    private Instant acceptedAt;

    @JsonProperty("declined_at")
    private Instant declinedAt;

    @JsonProperty("revoked_at")
    private Instant revokedAt;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;
}
