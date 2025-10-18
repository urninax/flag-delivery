package me.urninax.flagdelivery.organisation.listeners;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.organisation.events.invitation.MemberRoleChangedEvent;
import me.urninax.flagdelivery.organisation.services.AccessTokenService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MembershipEventsListener{
    private final AccessTokenService accessTokenService;

    @EventListener
    @Transactional
    public void onMemberRoleChanged(MemberRoleChangedEvent event){
        accessTokenService.downgradeMemberTokens(event.memberId(), event.role());
    }
}
