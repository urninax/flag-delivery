package me.urninax.flagdelivery.organisation.ui.controllers.integration;

import io.jsonwebtoken.Claims;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.organisation.shared.AccessTokenDTO;
import me.urninax.flagdelivery.organisation.ui.models.requests.CreateAccessTokenRequest;
import me.urninax.flagdelivery.organisation.ui.models.responses.PageResponse;
import me.urninax.flagdelivery.user.ui.models.requests.SignupRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@Testcontainers
@DisplayName("GET /api/v1/organisation/access-tokens")
public class GetAccessTokensIT extends AbstractAccessTokensIT{
    @Container
    @ServiceConnection
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    private String jwt;
    private String secondUserJwt;
    private String organisationId;

    @BeforeAll
    void setup(){
        SignupRequest signupRequest = createUser();
        String jwt = signinUser(signupRequest.getEmail(), signupRequest.getPassword());
        this.jwt = jwt;

        String organisationLocation = createOrganisationForUser(jwt);

        if(organisationLocation != null){
            organisationId = Arrays.stream(organisationLocation.split("/")).toList().getLast();
        }

        secondUserJwt = createSecondUser();

        createTokensMatrix(jwt, List.of(OrgRole.ADMIN, OrgRole.READER), List.of(true, false));
        createTokensMatrix(secondUserJwt, List.of(OrgRole.READER), List.of(true, false));
    }

    @ParameterizedTest
    @MethodSource("getTokensCases")
    @DisplayName("With valid request -> 200 and Access tokens page")
    void getAccessTokens_withValidRequestWithoutPathParameters_shouldReturn200AndTokensPage(Boolean showAll, int expectedCount){
        HttpHeaders authHeaders = authHeaders(jwt);
        HttpEntity<HttpHeaders> entity = new HttpEntity<>(authHeaders);

        URI uri = (showAll == null)
                ? URI.create("/api/v1/organisation/access-tokens")
                : UriComponentsBuilder
                .fromPath("/api/v1/organisation/access-tokens")
                .queryParam("showAll", showAll)
                .build(true)
                .toUri();

        ResponseEntity<PageResponse<AccessTokenDTO>> response = template.exchange(uri, HttpMethod.GET, entity, new ParameterizedTypeReference<>(){});
        PageResponse<AccessTokenDTO> body = response.getBody();

        assertNotNull(body, "Response body should have not been null");
        assertNotNull(body.getContent(), "Body Content should have not been null");
        assertEquals(expectedCount, body.getContent().size(), String.format("Content section should have contained %s items", expectedCount));
        assertEquals(0, body.getPage(), "Response should have returned page 0");
        assertEquals(25, body.getSize(), "Page size should have been max 25 items");
        assertEquals(expectedCount, body.getTotalElements(), String.format("Total elements should be %s", expectedCount));
        assertEquals(1, body.getTotalPages(), "Total pages should be 1");
    }

    private static Stream<Arguments> getTokensCases(){
        return Stream.of(
                Arguments.of(false, 4),
                Arguments.of(true, 6),
                Arguments.of(null, 4)
        );
    }

    private void createTokensMatrix(String bearer, List<OrgRole> roles, List<Boolean> svcFlags){
        roles.forEach(role -> {
            svcFlags.forEach(flag -> {
                        sendCreateTokenRequest(
                                CreateAccessTokenRequest.builder()
                                        .name("CI TOKEN - " + UUID.randomUUID())
                                        .role(role)
                                        .isService(flag)
                                        .build(),
                                authHeaders(bearer));
                    }
            );
        });
    }

    private String createSecondUser(){
        SignupRequest signupRequest = createUser();
        String secondUserAuthToken = signinUser(signupRequest.getEmail(), signupRequest.getPassword());

        String secondUserJwt = secondUserAuthToken.substring(7);
        Claims claims = jwtUtils.parse(secondUserJwt);

        String secondUserId = claims.getSubject();

        membershipsService.addMembership(UUID.fromString(organisationId), UUID.fromString(secondUserId), OrgRole.WRITER, false);

        return secondUserAuthToken;
    }
}
