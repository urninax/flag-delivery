package me.urninax.flagdelivery.flags.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "feature_flag_tags")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FeatureFlagTag{
    @EmbeddedId
    private FeatureFlagTagId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("flagId")
    @JoinColumn(name = "flag_id", nullable = false)
    private FeatureFlag featureFlag;

    public static FeatureFlagTag of(FeatureFlag flag, String tag) {
        FeatureFlagTagId id = new FeatureFlagTagId(flag.getId(), tag);
        return new FeatureFlagTag(id, flag);
    }
}
