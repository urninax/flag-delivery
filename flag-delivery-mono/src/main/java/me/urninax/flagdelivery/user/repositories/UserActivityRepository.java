package me.urninax.flagdelivery.user.repositories;

import me.urninax.flagdelivery.user.models.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface UserActivityRepository extends JpaRepository<UserActivity, UUID>{
    @Modifying
    @Query(value = """
        insert into user_activity (user_id, last_seen, last_ip, last_ua)
        values (:uid, :ts, cast(:ip as inet), :ua)
        on conflict (user_id)
        do update set
            last_seen = greatest(user_activity.last_seen, excluded.last_seen),
            last_ip = excluded.last_ip,
            last_ua = excluded.last_ua;
    """, nativeQuery = true)
    void upsertLastSeen(@Param("uid") UUID uid,
                        @Param("ts") Instant ts,
                        @Param("ip") String ip,
                        @Param("ua") String ua);
}
