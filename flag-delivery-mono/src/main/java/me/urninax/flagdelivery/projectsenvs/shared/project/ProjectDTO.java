package me.urninax.flagdelivery.projectsenvs.shared.project;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class ProjectDTO{
    private UUID id;
    private String key;
    private String name;
    private Set<String> tags;
//    private List<EnvironmentDTO> environments;
}
