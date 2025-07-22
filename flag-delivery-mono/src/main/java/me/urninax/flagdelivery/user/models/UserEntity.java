package me.urninax.flagdelivery.user.models;

import jakarta.persistence.*;
import lombok.*;
import me.urninax.flagdelivery.organisation.models.membership.Membership;
import me.urninax.flagdelivery.user.security.enums.InternalRole;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user_entity")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserEntity{
    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @OneToOne(mappedBy = "user")
    private Membership membership;

    @Column(name = "enabled")
    private boolean enabled;

    @ElementCollection(targetClass = InternalRole.class, fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_internal_roles",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "internal_role")
    @Enumerated(EnumType.STRING)
    private List<InternalRole> internalRoles;
}
