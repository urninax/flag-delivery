package me.urninax.flagdelivery.organisation.events.invitation;

import lombok.*;
import me.urninax.flagdelivery.organisation.shared.InvitationMailDTO;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class InvitationCreatedEvent{
    private InvitationMailDTO dto;
}
