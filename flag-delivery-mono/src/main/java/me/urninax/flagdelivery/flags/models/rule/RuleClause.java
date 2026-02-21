package me.urninax.flagdelivery.flags.models.rule;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rule_clause")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleClause{
    @Id
    @UuidGenerator
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private Rule rule;

    @Column(name = "context_kind_key")
    private String contextKindKey;

    @Column(name = "attribute")
    private String attribute;

    @Column(name = "op")
    @Enumerated(EnumType.STRING)
    private ClauseOp op;

    @Column(name = "negate")
    @Builder.Default
    private Boolean negate = false;

    @Type(JsonType.class)
    @Column(name = "values", columnDefinition = "jsonb")
    @Builder.Default
    private List<String> values = new ArrayList<>();
}
