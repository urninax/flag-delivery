package me.urninax.flagdelivery.organisation.repositories;

import me.urninax.flagdelivery.organisation.models.membership.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MembershipsRepository extends JpaRepository<Membership, UUID>{
}
