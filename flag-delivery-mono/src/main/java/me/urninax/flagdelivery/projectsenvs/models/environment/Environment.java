package me.urninax.flagdelivery.projectsenvs.models.environment;

import jakarta.persistence.*;
import lombok.*;
import me.urninax.flagdelivery.projectsenvs.models.project.Project;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "environment")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Environment{
    @Id
    @UuidGenerator
    private UUID id;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "name")
    private String name;

    @Column(name = "key")
    private String key;

    @Column(name = "confirm_changes")
    private boolean confirmChanges;

    @Column(name = "require_comments")
    private boolean requireComments;

    @Column(name = "critical")
    private boolean critical;

    @OneToMany(mappedBy = "environment", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EnvironmentTag> tags = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
