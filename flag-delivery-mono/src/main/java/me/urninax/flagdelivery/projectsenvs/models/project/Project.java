package me.urninax.flagdelivery.projectsenvs.models.project;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "project")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Project{
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

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectTag> tags = new HashSet<>();

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
