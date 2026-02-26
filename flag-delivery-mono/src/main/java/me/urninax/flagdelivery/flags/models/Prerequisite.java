package me.urninax.flagdelivery.flags.models;

import lombok.*;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Prerequisite{
    private String key;
    private UUID variation;

    @Override
    public boolean equals(Object o){
        if(!(o instanceof Prerequisite that)) return false;
        return Objects.equals(getKey(), that.getKey());
    }

    @Override
    public int hashCode(){
        return Objects.hashCode(getKey());
    }
}
