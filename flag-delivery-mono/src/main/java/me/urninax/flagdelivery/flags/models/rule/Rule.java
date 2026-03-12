package me.urninax.flagdelivery.flags.models.rule;

import jakarta.persistence.*;
import lombok.*;
import me.urninax.flagdelivery.flags.models.EnvironmentFlagConfig;
import me.urninax.flagdelivery.flags.models.FlagVariation;
import org.hibernate.annotations.UuidGenerator;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "rule")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Rule{
    @Id
    @UuidGenerator
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "environment_flag_config_id", nullable = false)
    private EnvironmentFlagConfig environmentFlagConfig;

    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<RuleClause> clauses = new LinkedHashSet<>();

    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 0;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variation_id")
    private FlagVariation variation;

    @Version
    @Column(name = "version")
    private Long version;

    public void addClause(RuleClause clause) {
        this.clauses.add(clause);
        clause.setRule(this);
    }
}
