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
import java.util.UUID;

@Entity
@Table(name = "context")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Context{
    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false)
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
    private Environment environment;

    @Size(max = 64)
    @NotNull
    @Column(name = "key", nullable = false, length = 64)
    private String key;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_seen")
    private Instant lastSeen;
}
