package me.urninax.flagdelivery.it.testcases.organisation.accesstokens;

import me.urninax.flagdelivery.it.AbstractIntegrationTest;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.organisation.shared.AccessTokenDTO;
import me.urninax.flagdelivery.organisation.ui.models.responses.PageResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("GET /api/v1/organisation/access-tokens")
public class GetAccessTokensIT extends AbstractIntegrationTest {
    private String accessToken;

    @BeforeAll
    void setup(){
        String jwt = helper.createUser();

        String organisationLocation = helper.createOrganisationForUser(jwt);
        String organisationId = Arrays.stream(organisationLocation.split("/")).toList().getLast();

        String secondUserJwt = helper.createUser();
        helper.addUserToOrganisation(secondUserJwt, organisationId, OrgRole.WRITER);

        accessToken = helper.createAccessToken(jwt, false, OrgRole.ADMIN);

        createTokensMatrix(jwt, List.of(OrgRole.ADMIN, OrgRole.READER), List.of(true, false));
        createTokensMatrix(secondUserJwt, List.of(OrgRole.READER), List.of(true, false));
    }

    @ParameterizedTest
    @MethodSource("getTokensCases")
    @DisplayName("With valid request -> 200 and Access tokens page")
    void getAccessTokens_withValidRequestWithoutPathParameters_shouldReturn200AndTokensPage(Boolean showAll, int expectedCount){
         URI uri = (showAll == null)
                ? URI.create("/api/v1/organisation/access-tokens")
                : UriComponentsBuilder
                .fromPath("/api/v1/organisation/access-tokens")
                .queryParam("showAll", showAll)
                .build(true)
                .toUri();

        client.get()
                .uri(uri.toString())
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<AccessTokenDTO>>(){})
                .value(body -> {
                    assertNotNull(body, "Response body should have not been null");
                    assertNotNull(body.getContent(), "Body Content should have not been null");
                    assertEquals(expectedCount, body.getContent().size(), String.format("Content section should have contained %s items", expectedCount));
                    assertEquals(0, body.getPage(), "Response should have returned page 0");
                    assertEquals(25, body.getSize(), "Page size should have been max 25 items");
                    assertEquals(expectedCount, body.getTotalElements(), String.format("Total elements should be %s", expectedCount));
                    assertEquals(1, body.getTotalPages(), "Total pages should be 1");
                });
    }

    private static Stream<Arguments> getTokensCases(){
        return Stream.of(
                Arguments.of(false, 5),
                Arguments.of(true, 7),
                Arguments.of(null, 5)
        );
    }

    private void createTokensMatrix(String bearer, List<OrgRole> roles, List<Boolean> svcFlags){
        roles.forEach(role ->
                svcFlags.forEach(flag -> helper.createAccessToken(bearer, flag, role)
        ));
    }
}
