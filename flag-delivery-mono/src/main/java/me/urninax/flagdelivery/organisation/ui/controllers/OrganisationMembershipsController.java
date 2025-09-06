package me.urninax.flagdelivery.organisation.ui.controllers;

import jakarta.validation.Valid;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.organisation.services.MembershipsService;
import me.urninax.flagdelivery.organisation.ui.models.requests.ChangeMembersRoleRequest;
import me.urninax.flagdelivery.shared.security.enums.AuthMethod;
import me.urninax.flagdelivery.shared.utils.annotations.AuthenticatedWithRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organisation/members")
@AuthenticatedWithRole(method = AuthMethod.ACCESS_TOKEN, role = OrgRole.ADMIN)
public class OrganisationMembershipsController{

    private final MembershipsService membershipsService;

    @Autowired
    public OrganisationMembershipsController(MembershipsService membershipsService){
        this.membershipsService = membershipsService;
    }

    @GetMapping
    @AuthenticatedWithRole(method = AuthMethod.ACCESS_TOKEN)
    public ResponseEntity<?> getMembers(){
        return null;
    }

    @PatchMapping("/{uuid}")
    @AuthenticatedWithRole(method = AuthMethod.ACCESS_TOKEN, role = OrgRole.ADMIN)
    public ResponseEntity<?> changeRole(@PathVariable(name = "uuid") UUID memberId,
                                        @RequestBody @Valid ChangeMembersRoleRequest request){
        membershipsService.changeMembersRole(memberId, request);

        return new ResponseEntity<>(HttpStatus.OK);
    }




}
