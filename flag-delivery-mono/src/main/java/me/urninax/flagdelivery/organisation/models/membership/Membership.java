package me.urninax.flagdelivery.organisation.models.membership;

import jakarta.persistence.*;
import lombok.*;
import me.urninax.flagdelivery.organisation.models.Organisation;
import me.urninax.flagdelivery.user.models.UserEntity;

import java.util.UUID;

@Entity
@Table(name = "organisation_memberships")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Membership{
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id")
    private Organisation organisation;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private OrgRole role;
}
