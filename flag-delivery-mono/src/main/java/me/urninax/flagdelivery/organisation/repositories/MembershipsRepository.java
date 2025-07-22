package me.urninax.flagdelivery.organisation.repositories;

import me.urninax.flagdelivery.organisation.models.membership.Membership;
import me.urninax.flagdelivery.organisation.models.membership.MembershipId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipsRepository extends JpaRepository<Membership, MembershipId>{
}
