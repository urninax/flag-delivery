package me.urninax.flagdelivery.user.shared;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.urninax.flagdelivery.organisation.models.membership.MembershipId;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MembershipDTO{
    private MembershipId id;

    private OrganisationDTO organisation;

    private boolean owner;

    private OrgRole role;
}
