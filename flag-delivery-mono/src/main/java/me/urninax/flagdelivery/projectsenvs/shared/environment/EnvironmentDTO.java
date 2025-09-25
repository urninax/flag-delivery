package me.urninax.flagdelivery.projectsenvs.shared.environment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;


@NoArgsConstructor
@Getter
@Setter
public class EnvironmentDTO{
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("key")
    private String key;

    @JsonProperty("confirm_changes")
    private boolean confirmChanges;

    @JsonProperty("require_comments")
    private boolean requireComments;

    @JsonProperty("critical")
    private boolean critical;

    @JsonProperty("tags")
    private Set<String> tags;
}
