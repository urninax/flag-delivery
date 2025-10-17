package me.urninax.flagdelivery.it.testcases.organisation.accesstokens;

import me.urninax.flagdelivery.it.AbstractIntegrationTest;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.organisation.ui.models.requests.CreateAccessTokenRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

@DisplayName("POST /api/v1/organisation/access-tokens")
public class PostAccessTokensIT extends AbstractIntegrationTest {
    private String jwt;

    private String organisationId;

    private CreateAccessTokenRequest request;

    @BeforeEach
    void setUp(){
        //create user
        String jwt = helper.createUser();
        this.jwt = jwt;

        organisationId = helper.createOrganisationForUser(jwt);

        request = CreateAccessTokenRequest.builder().name("CI Token").role(OrgRole.READER).isService(true).build();
    }

    @Test
    @DisplayName("With valid request and JWT -> 201 and Authorization header with api- prefix")
    void createAccessToken_withValidRequestAndJWT_shouldReturn201AndAuthorizationHeader(){
        client.post()
                .uri("/api/v1/organisation/access-tokens")
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("Authorization")
                .expectHeader().valueMatches("Authorization", "^Bearer\\sapi-.+");
    }

    @Test
    @DisplayName("With invalid request (empty name) and JWT -> 400")
    void createAccessToken_withInvalidRequestAndJWT_shouldReturn400(){
        request.setName("");

        client.post()
                .uri("/api/v1/organisation/access-tokens")
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("With access token role higher than user's one -> 403")
    void createAccessToken_whenTokenRoleIsHigherThanUsers_shouldReturn403(){
        String secondUserAuthToken = helper.createUser();
        helper.addUserToOrganisation(secondUserAuthToken, organisationId, OrgRole.READER);

        request.setRole(OrgRole.ADMIN);

        client.post().uri("/api/v1/organisation/access-tokens")
                .header(HttpHeaders.AUTHORIZATION, secondUserAuthToken)
                .bodyValue(request)
                .exchange()
                .expectStatus().isForbidden()
                .expectHeader().doesNotExist("Authorization");
    }


    @Test
    @DisplayName("When user has no organisation -> 403")
    void createAccessToken_whenUserHasNoOrganisation_shouldReturn403(){
        client.delete().uri("/api/v1/organisation")
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .exchange()
                .expectStatus().isOk();

        client.post().uri("/api/v1/organisation/access-tokens")
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().doesNotExist("Authorization");
    }
}
