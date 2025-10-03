package me.urninax.flagdelivery.projectsenvs.models.environment;

import jakarta.persistence.*;
import lombok.*;
import me.urninax.flagdelivery.flags.models.EnvironmentFlagConfig;
import me.urninax.flagdelivery.projectsenvs.models.project.Project;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
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

    @OneToMany(mappedBy = "environment", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EnvironmentFlagConfig> flagConfigs = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Override
    public final boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof Environment that)) return false;
        return Objects.equals(key, that.key) &&
                Objects.equals(project != null ? project.getId() : null,
                        that.project != null ? that.project.getId() : null);
    }

    @Override
    public int hashCode(){
        return Objects.hash(key, project != null ? project.getId() : null);
    }
}
