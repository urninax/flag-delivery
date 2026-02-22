package me.urninax.flagdelivery.flags.models;

import jakarta.persistence.*;
import lombok.*;
import me.urninax.flagdelivery.projectsenvs.models.project.Project;
import me.urninax.flagdelivery.user.models.UserEntity;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "feature_flag")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeatureFlag{
    @Id
    @UuidGenerator
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "key")
    private String key;

    @Column(name = "name")
    private String name;

    @Column(name = "kind")
    private FlagKind kind;

    @OneToMany(mappedBy = "flag", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FlagVariation> variations = new LinkedList<>();

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "default_on_variation_id")
    private FlagVariation defaultOnVariation;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "default_off_variation_id")
    private FlagVariation defaultOffVariation;

    @Column(name = "description")
    private String description;

    @Column(name = "flag_on")
    private boolean flagOn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maintainer_id")
    private UserEntity maintainer;

    @OneToMany(mappedBy = "flag", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EnvironmentFlagConfig> flagConfigs;

    @OneToMany(mappedBy = "featureFlag", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<FeatureFlagTag> tags = new HashSet<>();

    @Column(name = "temporary")
    private boolean temporary;

    @Column(name = "archived")
    private boolean archived;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    public void addVariation(FlagVariation variation){
        variations.add(variation);
        variation.setFlag(this);
    }

}
