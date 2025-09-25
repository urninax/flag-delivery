package me.urninax.flagdelivery.projectsenvs.repositories;

import me.urninax.flagdelivery.projectsenvs.models.environment.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EnvironmentsRepository extends JpaRepository<Environment, UUID>{
}
