package me.urninax.flagdelivery.organisation.ui.models.requests;

import jakarta.validation.constraints.NotNull;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;

public record ChangeMembersRoleRequest(
    @NotNull(message = "Role cannot be null")
    OrgRole role
){}
