package me.urninax.flagdelivery.organisation.shared;

import me.urninax.flagdelivery.organisation.models.membership.OrgRole;

import java.util.UUID;

public record AccessTokenPrincipalDTO(UUID ownerId, UUID organisationId, OrgRole role){}
