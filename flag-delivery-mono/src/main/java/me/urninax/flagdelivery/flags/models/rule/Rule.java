package me.urninax.flagdelivery.flags.models.rule;

import jakarta.persistence.*;
import lombok.*;
import me.urninax.flagdelivery.flags.models.EnvironmentFlagConfig;
import me.urninax.flagdelivery.flags.models.FlagVariation;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
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
    private List<RuleClause> clauses = new ArrayList<>();

    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 0;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variation_id")
    private FlagVariation variation;

    public void addClause(RuleClause clause) {
        this.clauses.add(clause);
        clause.setRule(this);
    }
}
