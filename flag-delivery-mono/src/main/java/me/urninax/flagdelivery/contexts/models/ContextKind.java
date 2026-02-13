package me.urninax.flagdelivery.contexts.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "context_kind")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ContextKind{
    @Id
    @UuidGenerator
    @Column(name = "id")
    private UUID id;

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "name")
    private String name;

    @Column(name = "key")
    private String key;

    @Column(name = "description")
    private String description;
}
