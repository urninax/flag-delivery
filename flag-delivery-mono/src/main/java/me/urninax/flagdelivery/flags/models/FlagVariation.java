package me.urninax.flagdelivery.flags.models;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UuidGenerator;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "flag_variation")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FlagVariation{
    @Id
    @UuidGenerator
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flag_id", nullable = false)
    private FeatureFlag flag;

    @Type(JsonType.class)
    @Column(name = "value", columnDefinition = "jsonb")
    private JsonNode value;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Version
    @Column(name = "version")
    private Long version;

    @Override
    public final boolean equals(Object o){
        if(this == o) return true;
        if(!(o instanceof FlagVariation that)) return false;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode(){
        return getClass().hashCode();
    }
}
