package me.urninax.flagdelivery.organisation.listeners;

import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.organisation.events.invitation.MemberRoleChangedEvent;
import me.urninax.flagdelivery.organisation.services.AccessTokenService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class MembershipEventsListener{
    private final AccessTokenService accessTokenService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMemberRoleChanged(MemberRoleChangedEvent event){
        accessTokenService.downgradeMemberTokens(event.memberId(), event.role());
    }
}
