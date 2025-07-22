package me.urninax.flagdelivery.organisation.models;

import jakarta.persistence.*;
import lombok.*;
import me.urninax.flagdelivery.organisation.models.membership.Membership;
import me.urninax.flagdelivery.user.models.UserEntity;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "organisation")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Organisation{
    @Id
    @UuidGenerator
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", unique = true)
    private UserEntity owner;

    @OneToMany(
            mappedBy = "organisation",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Membership> memberships;

//    public void addMembership(Membership membership){
//        memberships.add(membership);
//        membership.setOrganisation(this);
//    }
//
//    public void removeMembership(Membership membership){
//        memberships.remove(membership);
//        membership.setOrganisation(null);
//    }

    //projects
    //teams
    //roles
}
