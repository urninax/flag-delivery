package me.urninax.flagdelivery.organisation.models.membership;

import jakarta.persistence.*;
import lombok.*;
import me.urninax.flagdelivery.organisation.models.Organisation;
import me.urninax.flagdelivery.user.models.UserEntity;

@Entity
@Table(name = "organisation_memberships")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Membership{
    @EmbeddedId
    private MembershipId id;

    @MapsId("userId")
    @OneToOne(fetch = FetchType.LAZY)
    private UserEntity user;

    @MapsId("organisationId")
    @ManyToOne(fetch = FetchType.LAZY)
    private Organisation organisation;

    @Column(name = "is_owner", nullable = false)
    private boolean owner;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private OrgRole role;
}
