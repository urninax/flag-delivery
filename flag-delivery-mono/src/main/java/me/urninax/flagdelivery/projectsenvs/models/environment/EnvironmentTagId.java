package me.urninax.flagdelivery.projectsenvs.models.environment;

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
@AllArgsConstructor
@NoArgsConstructor
public class EnvironmentTagId{
    @Column(name = "environment_id")
    private UUID environmentId;

    @Column(name = "tag")
    private String tag;
}
