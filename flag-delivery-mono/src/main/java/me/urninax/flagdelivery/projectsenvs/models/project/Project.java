package me.urninax.flagdelivery.projectsenvs.models.project;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import me.urninax.flagdelivery.flags.models.FeatureFlag;
import me.urninax.flagdelivery.projectsenvs.models.environment.Environment;
import me.urninax.flagdelivery.shared.utils.Taggable;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "project")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Project implements Taggable{
    @UuidGenerator
    @Id
    @Column(name = "id")
    private UUID id;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "key")
    private String key;

    @Column(name = "name")
    private String name;

    @Column(name = "organisation_id")
    private UUID organisationId;

    @Type(JsonType.class)
    @Column(name = "tags", columnDefinition = "jsonb")
    @Builder.Default
    private Set<String> tags = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Environment> environments;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<FeatureFlag> featureFlags;

    @Column(name = "casing_convention")
    @Enumerated(EnumType.STRING)
    private CasingConvention casingConvention;

    @Column(name = "prefix")
    private String prefix;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "archived")
    private boolean archived;
}
