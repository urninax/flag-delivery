package me.urninax.flagdelivery.organisation.repositories;

import me.urninax.flagdelivery.organisation.models.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrganisationsRepository extends JpaRepository<Organisation, UUID>{
}
