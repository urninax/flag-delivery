package me.urninax.flagdelivery.projectsenvs.models.project;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "project_tags")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectTag{
    @EmbeddedId
    private ProjectTagId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("projectId")
    private Project project;
}
