package me.urninax.flagdelivery.organisation.ui.controllers;

import jakarta.validation.constraints.Pattern;
import me.urninax.flagdelivery.organisation.models.invitation.Invitation;
import me.urninax.flagdelivery.organisation.services.InvitationsService;
import me.urninax.flagdelivery.organisation.services.MailService;
import me.urninax.flagdelivery.organisation.shared.InvitationDTO;
import me.urninax.flagdelivery.user.security.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invitations")
public class InvitationsController{

    private final CurrentUser currentUser;
    private final InvitationsService invitationsService;
    private final MailService mailService;

    @Autowired
    public InvitationsController(CurrentUser currentUser, InvitationsService invitationsService, MailService mailService){
        this.currentUser = currentUser;
        this.invitationsService = invitationsService;
        this.mailService = mailService;
    }

    @GetMapping("/{uuid}.{token}")
    public ResponseEntity<?> getInvitationInfo(@PathVariable UUID uuid,
                                               @PathVariable @Pattern(regexp = "^[A-Za-z0-9_-]{43}$") String token){
        InvitationDTO invitationDTO = invitationsService.getInvitationDTO(uuid, token);
        return new ResponseEntity<>(invitationDTO, HttpStatus.OK);
    }

    @PostMapping("/{uuid}.{token}/accept")
    public ResponseEntity<?> acceptInvitation(@PathVariable UUID uuid,
                                              @PathVariable @Pattern(regexp = "^[A-Za-z0-9_-]{43}$") String token,
                                              @RequestParam(name = "transfer", defaultValue = "false") boolean transfer){
        UUID userId = currentUser.getUserId();
        Invitation inv = invitationsService.acceptInvitation(uuid, token, userId, transfer);
        mailService.sendInviteAcceptedGreeting(inv);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{token}/decline")
    public ResponseEntity<?> declineInvitation(@PathVariable String token){
        UUID userId = currentUser.getUserId();
        return null;
    }
}
