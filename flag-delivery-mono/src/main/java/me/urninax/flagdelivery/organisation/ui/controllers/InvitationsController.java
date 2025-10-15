package me.urninax.flagdelivery.organisation.ui.controllers;

import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.organisation.services.InvitationsService;
import me.urninax.flagdelivery.organisation.shared.InvitationPublicDTO;
import me.urninax.flagdelivery.shared.security.enums.AuthMethod;
import me.urninax.flagdelivery.shared.utils.annotations.RequiresAuthMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/invitations")
@RequiresAuthMethod(AuthMethod.JWT)
public class InvitationsController{
    private final InvitationsService invitationsService;

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
        invitationsService.acceptInvitation(uuid, token, transfer);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{uuid}.{token}/decline")
    public ResponseEntity<?> declineInvitation(@PathVariable UUID uuid,
                                               @PathVariable @Pattern(regexp = "^[A-Za-z0-9_-]{43}$") String token){
        invitationsService.declineInvitation(uuid, token);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
