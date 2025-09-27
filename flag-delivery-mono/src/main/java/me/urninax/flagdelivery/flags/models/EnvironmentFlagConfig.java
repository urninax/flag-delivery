package me.urninax.flagdelivery.flags.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.urninax.flagdelivery.projectsenvs.models.environment.Environment;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "environment_flag_config")
@Getter
@Setter
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

    @Column(name = "is_on")
    private boolean on;

    @Column(name = "salt")
    private String salt;

    @Column(name = "sel")
    private String sel;

    @Column(name = "archived")
    private boolean archived;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "on_variation_id")
    private FlagVariation onVariation;

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
}
