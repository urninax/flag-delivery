package me.urninax.flagdelivery.user.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_activity")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserActivity{
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "last_seen")
    private Instant lastSeen;

    @Column(name = "last_ip")
    @JdbcTypeCode(SqlTypes.INET)
    private String lastIp;

    @Column(name = "last_ua")
    private String lastUa;
}
