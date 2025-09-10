package me.urninax.flagdelivery.organisation.ui.controllers;

import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.organisation.services.InvitationsService;
import me.urninax.flagdelivery.organisation.shared.InvitationOrganisationDTO;
import me.urninax.flagdelivery.organisation.ui.models.requests.CreateInvitationRequest;
import me.urninax.flagdelivery.organisation.ui.models.requests.InvitationFilter;
import me.urninax.flagdelivery.organisation.ui.models.responses.PageResponse;
import me.urninax.flagdelivery.shared.security.CurrentUser;
import me.urninax.flagdelivery.shared.security.enums.AuthMethod;
import me.urninax.flagdelivery.shared.utils.annotations.RequiresAuthMethod;
import me.urninax.flagdelivery.shared.utils.annotations.RequiresRole;
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
@RequiresAuthMethod(AuthMethod.ACCESS_TOKEN)
@RequiresRole(OrgRole.ADMIN)
public class OrganisationInvitationsController{
    private final CurrentUser currentUser;
    private final InvitationsService invitationsService;

    @Autowired
    public OrganisationInvitationsController(CurrentUser currentUser, InvitationsService invitationsService){
        this.currentUser = currentUser;
        this.invitationsService = invitationsService;
    }

    @PostMapping
    public ResponseEntity<?> invite(@RequestBody CreateInvitationRequest request){
        UUID userId = currentUser.getUserId();
        invitationsService.createInvitation(request, userId);
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

    @PostMapping("/{uuid}/revoke")
    public ResponseEntity<?> revokeInvitation(@PathVariable(name = "uuid") UUID invitationId){
        UUID userId = currentUser.getUserId();
        invitationsService.revokeInvitation(invitationId, userId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
