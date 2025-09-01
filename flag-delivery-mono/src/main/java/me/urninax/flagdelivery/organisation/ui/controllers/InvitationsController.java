package me.urninax.flagdelivery.organisation.ui.controllers;

import jakarta.validation.constraints.Pattern;
import me.urninax.flagdelivery.organisation.services.InvitationsService;
import me.urninax.flagdelivery.organisation.shared.InvitationPublicDTO;
import me.urninax.flagdelivery.shared.security.CurrentUser;
import me.urninax.flagdelivery.shared.utils.annotations.JwtOnly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invitations")
@JwtOnly
public class InvitationsController{

    private final CurrentUser currentUser;
    private final InvitationsService invitationsService;

    @Autowired
    public InvitationsController(CurrentUser currentUser, InvitationsService invitationsService){
        this.currentUser = currentUser;
        this.invitationsService = invitationsService;
    }

    @GetMapping("/{uuid}.{token}")
    public ResponseEntity<?> getInvitationInfo(@PathVariable UUID uuid,
                                               @PathVariable @Pattern(regexp = "^[A-Za-z0-9_-]{43}$") String token){
        InvitationPublicDTO invitationDTO = invitationsService.getInvitationDTO(uuid, token);
        return new ResponseEntity<>(invitationDTO, HttpStatus.OK);
    }

    @PostMapping("/{uuid}.{token}/accept")
    public ResponseEntity<?> acceptInvitation(@PathVariable UUID uuid,
                                              @PathVariable @Pattern(regexp = "^[A-Za-z0-9_-]{43}$") String token,
                                              @RequestParam(name = "transfer", defaultValue = "false") boolean transfer){
        UUID userId = currentUser.getUserId();
        invitationsService.acceptInvitation(uuid, token, userId, transfer);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{uuid}.{token}/decline")
    public ResponseEntity<?> declineInvitation(@PathVariable UUID uuid,
                                               @PathVariable @Pattern(regexp = "^[A-Za-z0-9_-]{43}$") String token){
        UUID userId = currentUser.getUserId();
        invitationsService.declineInvitation(uuid, token, userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
