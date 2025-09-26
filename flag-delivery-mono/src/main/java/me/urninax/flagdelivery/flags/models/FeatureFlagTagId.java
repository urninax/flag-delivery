package me.urninax.flagdelivery.flags.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FeatureFlagTagId{
    @Column(name = "flag_id")
    private UUID flagId;

    @Column(name = "tag")
    private String tag;
}
