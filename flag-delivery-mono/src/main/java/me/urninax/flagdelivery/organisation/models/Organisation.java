package me.urninax.flagdelivery.organisation.models;

import jakarta.persistence.*;
import lombok.*;
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
    private List<UserEntity> members;

    public void addMember(UserEntity userEntity){
        members.add(userEntity);
        userEntity.setOrganisation(this);
    }

    //projects
    //teams
    //roles
}
