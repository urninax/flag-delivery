package me.urninax.flagdelivery.it.helper;

import io.jsonwebtoken.Claims;
import me.urninax.flagdelivery.organisation.models.AccessToken;
import me.urninax.flagdelivery.organisation.models.invitation.Invitation;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.organisation.repositories.AccessTokenRepository;
import me.urninax.flagdelivery.organisation.repositories.InvitationsRepository;
import me.urninax.flagdelivery.organisation.services.MembershipsService;
import me.urninax.flagdelivery.organisation.ui.models.requests.CreateAccessTokenRequest;
import me.urninax.flagdelivery.organisation.ui.models.requests.CreateOrganisationRequest;
import me.urninax.flagdelivery.shared.utils.JwtUtils;
import me.urninax.flagdelivery.user.models.UserEntity;
import me.urninax.flagdelivery.user.repositories.UsersRepository;
import me.urninax.flagdelivery.user.ui.models.requests.SigninRequest;
import me.urninax.flagdelivery.user.ui.models.requests.SignupRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.*;

@Component
public class TestHelper {
    @Lazy
    @Autowired
    private WebTestClient client;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private MembershipsService membershipsService;

    @Autowired
    private AccessTokenRepository accessTokenRepository;
    @Autowired
    private InvitationsRepository invitationsRepository;
    @Autowired
    private UsersRepository usersRepository;

    public String createUser(){
        SignupRequest signupRequest = SignupRequest.builder()
                .firstName("CI First Name").lastName("CI Last Name")
                .email("ci." + UUID.randomUUID() + "@example.com")
                .password("CIPassword123!")
                .build();

        client.post()
                .uri("/api/v1/auth/signup")
                .bodyValue(signupRequest)
                .exchange()
                .expectStatus().isCreated();

        return signinUser(signupRequest.getEmail(), signupRequest.getPassword());
    }

    public String signinUser(String email, String password){
        SigninRequest signinRequest = SigninRequest.builder()
                .email(email)
                .password(password)
                .build();

        EntityExchangeResult<Void> result = client.post().uri("/api/v1/auth/signin")
                .bodyValue(signinRequest)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists("Authorization")
                .expectHeader().valueMatches("Authorization", "^Bearer\\s.+")
                .expectBody().isEmpty();

        return result.getResponseHeaders().getFirst("Authorization");
    }

    public UUID createOrganisationForUser(String jwt){
        CreateOrganisationRequest createOrganisationRequest = CreateOrganisationRequest.builder()
                .name("CI Organisation name " + UUID.randomUUID())
                .build();

        EntityExchangeResult<Void> result = client.post()
                .uri("/api/v1/organisation")
                .bodyValue(createOrganisationRequest)
                .header("Authorization", jwt)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("Location")
                .expectBody().isEmpty();

        String organisationLocation = Objects.requireNonNull(result.getResponseHeaders().getFirst("Location"));

        return UUID.fromString(Arrays.stream(organisationLocation.split("/")).toList().getLast());
    }

    public UUID extractUserId(String jwt){
        String userJwt = jwt.replace("Bearer ", "");
        Claims claims = jwtUtils.parse(userJwt);

        return UUID.fromString(claims.getSubject());
    }

    public void addUserToOrganisation(UUID userId, UUID organisationId, OrgRole role){
        membershipsService.addMembership(organisationId, userId, role);
    }

    public String createAccessToken(String authToken, boolean isService, OrgRole role){
        CreateAccessTokenRequest request = CreateAccessTokenRequest.builder()
                .name("CI TOKEN - " + UUID.randomUUID())
                .role(role)
                .isService(isService)
                .build();

        EntityExchangeResult<Void> response = client.post().uri("/api/v1/organisation/access-tokens")
                .header(HttpHeaders.AUTHORIZATION, authToken)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("Authorization")
                .expectBody().isEmpty();

        return response.getResponseHeaders().getFirst("Authorization");
    }

    public OrgRole getUserRoleInOrganisation(UUID userId, UUID organisationId){
        return membershipsService.findByIdAndOrg(userId, organisationId).getRole();
    }

    public boolean verifyTokensDowngraded(UUID userId, UUID organisationId, OrgRole expectedRole){
        List<AccessToken> tokens = accessTokenRepository.findAllByOwner_IdAndOrganisation_IdAndIsServiceFalse(userId, organisationId);

        return tokens.stream()
                .allMatch(token -> token.getRole().lowerOrEqual(expectedRole));
    }

    public Optional<Invitation> findInvitationByEmailAndOrgId(String email, UUID orgId){
        return invitationsRepository.findByEmailAndOrganisation_Id(email, orgId);
    }

    public String getUserEmailById(UUID userId){
        Optional<UserEntity> userOptional = usersRepository.findById(userId);

        if (userOptional.isEmpty()){
            throw new AssertionError("User doesnt exist");
        }

        return userOptional.get().getEmail();
    }
}
