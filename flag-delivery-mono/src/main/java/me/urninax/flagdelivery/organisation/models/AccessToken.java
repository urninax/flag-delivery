package me.urninax.flagdelivery.organisation.models;

import jakarta.persistence.*;
import lombok.*;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.user.models.UserEntity;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "access_token")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccessToken{
    @Id
    @UuidGenerator
    @Column(name = "token")
    private UUID token;

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private OrgRole role;

    @CreationTimestamp
    @Column(name = "issued_at")
    private Instant issuedAt;

    @Column(name = "last_used")
    private Instant lastUsed;

    @Column(name = "is_service")
    private boolean isService;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private UserEntity owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id")
    private Organisation organisation;
}
