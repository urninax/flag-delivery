package me.urninax.flagdelivery.flags.models;

import lombok.*;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContextTarget{
    public Set<String> values;
    public String contextKind;
    public UUID variation;
}
