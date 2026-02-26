package me.urninax.flagdelivery.flags.models;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import me.urninax.flagdelivery.flags.models.rule.Rule;
import me.urninax.flagdelivery.projectsenvs.models.environment.Environment;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "environment_flag_config")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnvironmentFlagConfig{
    @Id
    @UuidGenerator
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "environment_id")
    private Environment environment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flag_id")
    private FeatureFlag flag;

    @OneToMany(mappedBy = "environmentFlagConfig", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<Rule> rules = new ArrayList<>();

    @Type(JsonType.class)
    @Column(name = "prerequisites", columnDefinition = "jsonb")
    @Builder.Default
    private Set<Prerequisite> prerequisites = new HashSet<>();

    @Column(name = "is_on")
    private boolean on;

    @Column(name = "salt")
    private String salt;

    @Column(name = "sel")
    private String sel;

    @Column(name = "archived")
    private boolean archived;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "off_variation_id")
    private FlagVariation offVariation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fallthrough_variation_id")
    private FlagVariation fallthroughVariation;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Version
    @Column(name = "version")
    private long version;

    public void addRule(Rule rule){
        this.rules.add(rule);
        rule.setEnvironmentFlagConfig(this);
    }
}
