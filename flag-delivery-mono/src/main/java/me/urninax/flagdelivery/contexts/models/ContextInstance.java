package me.urninax.flagdelivery.contexts.models;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "context_instance",
        indexes = {@Index(name = "idx_context_instance_hash", columnList = "hash")})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ContextInstance{
    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @NotNull
    @Column(name = "hash", nullable = false)
    @EqualsAndHashCode.Include
    private String hash;

    @NotNull
    @Type(JsonType.class)
    @Column(name = "body", nullable = false, columnDefinition = "jsonb")
    private JsonNode body;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 0;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @ManyToMany(mappedBy = "instances", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Context> contexts = new LinkedHashSet<>();
}
