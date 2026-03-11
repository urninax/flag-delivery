package me.urninax.flagdelivery.contexts.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import me.urninax.flagdelivery.projectsenvs.models.environment.Environment;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "context")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Context{
    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "context_kind_id", nullable = false)
    private ContextKind contextKind;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "environment_id", nullable = false)
    @EqualsAndHashCode.Include
    private Environment environment;

    @Size(max = 64)
    @NotNull
    @Column(name = "key", nullable = false, length = 64)
    @EqualsAndHashCode.Include
    private String key;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_seen")
    private Instant lastSeen;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "context_instance_mappings",
            joinColumns = @JoinColumn(name = "context_id"),
            inverseJoinColumns = @JoinColumn(name = "context_instance_id"))
    @Builder.Default
    private Set<ContextInstance> instances = new LinkedHashSet<>();

    public void addInstance(ContextInstance instance) {
        this.instances.add(instance);
        instance.getContexts().add(this);
    }

    public void removeInstance(ContextInstance instance) {
        this.instances.remove(instance);
        instance.getContexts().remove(this);
    }
}
