package me.urninax.flagdelivery.projectsenvs.shared.project;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.urninax.flagdelivery.projectsenvs.shared.environment.EnvironmentDTO;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class ProjectDTO{
    private UUID id;
    private String key;
    private String name;

    @JsonProperty("naming_convention")
    private NamingConventionDTO namingConvention;
    private Set<String> tags;

    @JsonProperty("created_by")
    private UUID createdBy;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Set<EnvironmentDTO> environments;
}
