package me.urninax.flagdelivery.organisation.models.mailoutbox;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "mail_outbox", indexes = {@Index(name = "idx_mail_outbox_status_created_at", columnList = "status, created_at")})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class MailOutboxEvent{
    @Id
    @UuidGenerator
    @Column(name = "id")
    private UUID id;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "type")
    private String type;

    @Type(JsonType.class)
    @Column(name = "payload", columnDefinition = "jsonb")
    private JsonNode payload;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status")
    private MailStatus status;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "created_at")
    private Instant createdAt;
}
