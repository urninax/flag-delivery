package me.urninax.flagdelivery.organisation.events.invitation;

import me.urninax.flagdelivery.organisation.models.membership.OrgRole;

import java.util.UUID;

public record MemberRoleChangedEvent(UUID memberId, OrgRole role){}
