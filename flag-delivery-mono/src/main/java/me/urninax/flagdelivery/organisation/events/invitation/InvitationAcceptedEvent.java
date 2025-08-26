package me.urninax.flagdelivery.organisation.events.invitation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.urninax.flagdelivery.organisation.shared.InvitationMailDTO;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class InvitationAcceptedEvent{
    private InvitationMailDTO dto;
}
