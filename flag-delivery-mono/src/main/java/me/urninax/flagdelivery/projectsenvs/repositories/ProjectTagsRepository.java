package me.urninax.flagdelivery.projectsenvs.repositories;

import me.urninax.flagdelivery.projectsenvs.models.project.ProjectTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ProjectTagsRepository extends JpaRepository<ProjectTag, UUID>{
    @Query("select pt from ProjectTag pt where pt.project.id in :ids")
    List<ProjectTag> findAllByProjectIdIn(Collection<UUID> ids);
}
