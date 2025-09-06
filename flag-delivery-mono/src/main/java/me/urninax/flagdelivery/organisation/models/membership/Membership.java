package me.urninax.flagdelivery.organisation.models.membership;

import jakarta.persistence.*;
import lombok.*;
import me.urninax.flagdelivery.organisation.models.Organisation;
import me.urninax.flagdelivery.user.models.UserEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @Version
    @Column(name = "version")
    private Long version;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id")
    private Organisation organisation;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private OrgRole role;
}
