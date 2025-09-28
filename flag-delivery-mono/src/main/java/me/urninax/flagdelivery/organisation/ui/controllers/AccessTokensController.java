package me.urninax.flagdelivery.organisation.ui.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.organisation.services.AccessTokenService;
import me.urninax.flagdelivery.organisation.shared.AccessTokenDTO;
import me.urninax.flagdelivery.organisation.ui.models.requests.CreateAccessTokenRequest;
import me.urninax.flagdelivery.organisation.ui.models.responses.PageResponse;
import me.urninax.flagdelivery.shared.security.enums.AuthMethod;
import me.urninax.flagdelivery.shared.utils.annotations.RequiresAuthMethod;
import me.urninax.flagdelivery.shared.utils.annotations.RequiresRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/organisation/access-tokens")
@RequiresRole(OrgRole.READER)
public class AccessTokensController{
    private final AccessTokenService accessTokenService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createAccessToken(@RequestBody @Valid CreateAccessTokenRequest request){
        String token = accessTokenService.issueToken(request);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", String.format("Bearer %s", token));

        return ResponseEntity.status(HttpStatus.CREATED).headers(httpHeaders).build();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @RequiresAuthMethod(AuthMethod.ACCESS_TOKEN)
    public ResponseEntity<PageResponse<AccessTokenDTO>> getAccessTokens(@PageableDefault(size = 25,
                                                                            sort = {"isService", "lastUsed"},
                                                                            direction = Sort.Direction.DESC) Pageable pageable,
                                                                        @RequestParam(name = "showAll") Optional<Boolean> showAllOptional){
        //TODO: add token type filter support
        Page<AccessTokenDTO> accessTokenDTOPage = accessTokenService.getTokensForUserInOrg(pageable, showAllOptional);

        return new ResponseEntity<>(new PageResponse<>(accessTokenDTOPage), HttpStatus.OK);
    }
}
