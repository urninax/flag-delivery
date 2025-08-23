package me.urninax.flagdelivery.organisation.ui.controllers;

import me.urninax.flagdelivery.organisation.services.InvitationsService;
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

    @Autowired
    public InvitationsController(CurrentUser currentUser, InvitationsService invitationsService){
        this.currentUser = currentUser;
        this.invitationsService = invitationsService;
    }

    @GetMapping("/{token}")
    public ResponseEntity<?> getInvitationInfo(@PathVariable String token){
        InvitationDTO invitationDTO = invitationsService.getInvitationDTO(token);
        return new ResponseEntity<>(invitationDTO, HttpStatus.OK);
    }

    @PostMapping("/{token}/accept")
    public ResponseEntity<?> acceptInvitation(@PathVariable String token){
        UUID userId = currentUser.getUserId();
        return null;
    }

    @PostMapping("/{token}/decline")
    public ResponseEntity<?> declineInvitation(@PathVariable String token){
        UUID userId = currentUser.getUserId();
        return null;
    }
}
