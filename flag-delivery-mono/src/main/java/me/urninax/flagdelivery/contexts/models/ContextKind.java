package me.urninax.flagdelivery.contexts.models;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "context_kind")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ContextKind{
    @Id
    @UuidGenerator
    @Column(name = "id")
    private UUID id;

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "name")
    private String name;

    @Column(name = "key")
    private String key;

    @Column(name = "description")
    private String description;

    @Type(JsonType.class)
    @Column(name = "attributes", columnDefinition = "jsonb")
    private JsonNode attributes;

    @Column(name = "archived")
    private boolean archived;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "last_seen")
    private Instant lastSeen;
}
