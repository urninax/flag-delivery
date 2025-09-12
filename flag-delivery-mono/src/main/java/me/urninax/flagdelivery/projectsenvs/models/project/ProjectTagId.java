package me.urninax.flagdelivery.projectsenvs.models.project;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTagId{
    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "tag")
    private String tag;
}
