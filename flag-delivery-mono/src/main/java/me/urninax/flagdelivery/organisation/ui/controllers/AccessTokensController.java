package me.urninax.flagdelivery.organisation.ui.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.urninax.flagdelivery.organisation.services.AccessTokenService;
import me.urninax.flagdelivery.organisation.shared.AccessTokenDTO;
import me.urninax.flagdelivery.organisation.ui.models.requests.CreateAccessTokenRequest;
import me.urninax.flagdelivery.organisation.ui.models.responses.PageResponse;
import me.urninax.flagdelivery.shared.security.CurrentUser;
import me.urninax.flagdelivery.shared.utils.annotations.AccessTokenOnly;
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
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/organisation/access-tokens")
public class AccessTokensController{
    private final AccessTokenService accessTokenService;
    private final CurrentUser currentUser;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createAccessToken(@RequestBody @Valid CreateAccessTokenRequest request){
        UUID userId = currentUser.getUserId();
        String token = accessTokenService.issueToken(userId, request);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", String.format("Bearer %s", token));

        return ResponseEntity.status(HttpStatus.CREATED).headers(httpHeaders).build();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @AccessTokenOnly
    public ResponseEntity<PageResponse<AccessTokenDTO>> getAccessTokens(@PageableDefault(size = 25,
                                                                            sort = {"isService", "lastUsed"},
                                                                            direction = Sort.Direction.DESC) Pageable pageable,
                                                                        @RequestParam(name = "showAll") Optional<Boolean> showAllOptional){
        //TODO: add token type filter support
        UUID userId = currentUser.getUserId();
        Page<AccessTokenDTO> accessTokenDTOPage = accessTokenService.getTokensForUserInOrg(userId, pageable, showAllOptional);

        return new ResponseEntity<>(new PageResponse<>(accessTokenDTOPage), HttpStatus.OK);
    }
}
