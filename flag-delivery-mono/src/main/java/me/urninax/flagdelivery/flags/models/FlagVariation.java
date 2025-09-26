package me.urninax.flagdelivery.flags.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "flag_variation")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FlagVariation{
    @Id
    @UuidGenerator
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flag_id")
    private FeatureFlag flag;

    @Column(name = "value", columnDefinition = "jsonb")
    private String value;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;
}
