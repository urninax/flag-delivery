package me.urninax.flagdelivery.organisation.models.membership;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipId implements Serializable{
    private UUID userId;
    private UUID organisationId;
}
