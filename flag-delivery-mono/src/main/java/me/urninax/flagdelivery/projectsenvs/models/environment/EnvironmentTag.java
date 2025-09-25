package me.urninax.flagdelivery.projectsenvs.models.environment;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "environment_tags")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EnvironmentTag{
    @EmbeddedId
    private EnvironmentTagId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("environmentId")
    private Environment environment;
}
