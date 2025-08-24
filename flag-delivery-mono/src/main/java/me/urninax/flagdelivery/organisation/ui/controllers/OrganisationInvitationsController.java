package me.urninax.flagdelivery.organisation.ui.controllers;

import me.urninax.flagdelivery.organisation.models.invitation.Invitation;
import me.urninax.flagdelivery.organisation.services.InvitationsService;
import me.urninax.flagdelivery.organisation.services.MailService;
import me.urninax.flagdelivery.organisation.shared.InvitationOrganisationDTO;
import me.urninax.flagdelivery.organisation.ui.models.requests.CreateInvitationRequest;
import me.urninax.flagdelivery.organisation.ui.models.requests.InvitationFilter;
import me.urninax.flagdelivery.organisation.ui.models.responses.PageResponse;
import me.urninax.flagdelivery.user.security.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organisation/invitations")
public class OrganisationInvitationsController{
    private final CurrentUser currentUser;
    private final InvitationsService invitationsService;
    private final MailService mailService;

    @Autowired
    public OrganisationInvitationsController(CurrentUser currentUser, InvitationsService invitationsService, MailService mailService){
        this.currentUser = currentUser;
        this.invitationsService = invitationsService;
        this.mailService = mailService;
    }

    @PostMapping
    //TODO: has role admin
    public ResponseEntity<?> invite(@RequestBody CreateInvitationRequest request){
        UUID userId = currentUser.getUserId();
        Invitation invitation = invitationsService.createInvitation(request, userId);
        mailService.sendInvitation(invitation); //TODO: not good, should be sent to background queue for processing

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<PageResponse<InvitationOrganisationDTO>> listOrgInvitations(@PageableDefault(
                                                                                            direction = Sort.Direction.DESC,
                                                                                            sort = "updatedAt") Pageable pageable,
                                                                                      InvitationFilter filter){
        Page<InvitationOrganisationDTO> orgInvitations = invitationsService.listOrganisationInvitations(filter, pageable);
        return new ResponseEntity<>(new PageResponse<>(orgInvitations), HttpStatus.OK);
    }
}
