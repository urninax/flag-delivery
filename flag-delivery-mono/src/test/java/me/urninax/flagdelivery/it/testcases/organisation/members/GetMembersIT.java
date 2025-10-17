package me.urninax.flagdelivery.it.testcases.organisation.members;

import me.urninax.flagdelivery.it.AbstractIntegrationTest;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.organisation.shared.MemberWithActivityDTO;
import me.urninax.flagdelivery.organisation.ui.models.responses.PageResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
@DisplayName("GET /api/v1/organisation/members")
public class GetMembersIT extends AbstractIntegrationTest {
    private String accessToken;
    private int membersCount = 0;
    private final ConcurrentHashMap<OrgRole, Integer> roleMembersCount = new ConcurrentHashMap<>();

    @BeforeAll
    void setup(){
        String mainUserJwt = helper.createUser();

        String organisationId = helper.createOrganisationForUser(mainUserJwt);
        membersCount++;

        accessToken = helper.createAccessToken(mainUserJwt, false, OrgRole.ADMIN);
        addUsersToOrganisation(organisationId, OrgRole.READER, 3);
        addUsersToOrganisation(organisationId, OrgRole.WRITER, 2);
        addUsersToOrganisation(organisationId, OrgRole.NONE, 4);
    }

    @Test
    @DisplayName("With valid request without query parameters -> 200 and Members Page")
    void getMembers_withValidRequestWithoutQueryParameters_shouldReturn200AndMembersPage(){
        client.get()
                .uri("/api/v1/organisation/members")
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<MemberWithActivityDTO>>(){})
                .value(body -> {
                    assertNotNull(body, "Response body should have not been null");
                    assertNotNull(body.getContent(), "Body Content should have not been null");
                    assertEquals(membersCount, body.getContent().size(), String.format("Content section should have contained %s items", membersCount));
                    assertEquals(0, body.getPage(), "Response should have returned page 0");
                    assertEquals(10, body.getSize(), "Page size should have been max 10 items");
                    assertEquals(membersCount, body.getTotalElements(), String.format("Total elements should be %s", membersCount));
                    assertEquals(1, body.getTotalPages(), "Total pages should be 1");
                });
    }

    @ParameterizedTest
    @MethodSource("filterCases")
    @DisplayName("With valid role filter parameters -> 200 and filtered Members Page")
    void getMembers_withValidRoleFilterParameters_shouldReturn200AndFilteredMembersPage(String filter, List<OrgRole> roles, int expectedCount){
        EntityExchangeResult<PageResponse<MemberWithActivityDTO>> response =
                client.get()
                .uri(uriBuilder ->
                    uriBuilder.path("/api/v1/organisation/members")
                            .queryParam("roles", filter)
                            .build()
                )
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<MemberWithActivityDTO>>(){})
                .value(body -> {
                    assertNotNull(body, "Response body should have not been null");
                    assertNotNull(body.getContent(), "Body Content should have not been null");
                    assertEquals(expectedCount, body.getContent().size(), String.format("Content section should have contained %s items", expectedCount));
                    assertEquals(0, body.getPage(), "Response should have returned page 0");
                    assertEquals(10, body.getSize(), "Page size should have been max 10 items");
                    assertEquals(expectedCount, body.getTotalElements(), String.format("Total elements should be %s", expectedCount));
                    assertEquals(1, body.getTotalPages(), "Total pages should be 1");
                })
                .returnResult();

        PageResponse<MemberWithActivityDTO> page = Objects.requireNonNull(response.getResponseBody());
        List<MemberWithActivityDTO> content = page.getContent();

        content.forEach(member -> assertTrue(roles.contains(member.getRole())));
    }

    @ParameterizedTest
    @MethodSource("pageCases")
    @DisplayName("With valid page parameters -> 200 and Members Page")
    void getMembers_withValidPageParameters_shouldReturn200AndMembersPage(int pageSize, int pageNumber, String sort, OrgRole firstRole, OrgRole lastRole){
        EntityExchangeResult<PageResponse<MemberWithActivityDTO>> response =
                client.get()
                .uri(uriBuilder ->
                        uriBuilder.path("/api/v1/organisation/members")
                                .queryParam("size", pageSize)
                                .queryParam("page", pageNumber)
                                .queryParam("sort", sort)
                                .build()
                )
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<MemberWithActivityDTO>>() {})
                .value(body -> {
                    assertNotNull(body, "Response body should have not been null");
                    assertNotNull(body.getContent(), "Body Content should have not been null");
                    assertEquals(pageSize, body.getContent().size(), String.format("Content section should have contained %s items", pageSize));
                    assertEquals(pageNumber, body.getPage(), String.format("Response should have returned page %s", pageNumber));
                    assertEquals(pageSize, body.getSize(), String.format("Page size should have been max %s items", pageSize));
                    assertEquals(membersCount, body.getTotalElements(), String.format("Total elements should be %s", membersCount));

                    int totalPages = (int) Math.ceil((double) membersCount/pageSize);
                    assertEquals(totalPages, body.getTotalPages(), String.format("Total pages should be %s", totalPages));
                })
                .returnResult();

        PageResponse<MemberWithActivityDTO> page = Objects.requireNonNull(response.getResponseBody());
        List<MemberWithActivityDTO> content = page.getContent();

        assertEquals(firstRole, content.getFirst().getRole(), String.format("First role should have been %s", firstRole));
        assertEquals(lastRole, content.getLast().getRole(), String.format("Last role should have been %s", lastRole));
    }

    @Test
    @DisplayName("Without auth token -> 403")
    void getMembers_withoutAuthToken_shouldReturn403(){
        client.get()
                .uri("/api/v1/organisation/members")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("With invalid query parameter -> 200 and Members Page")
    void getMembers_withInvalidQueryParameter_shouldReturn200(){
        client.get()
                .uri(uriBuilder ->
                        uriBuilder.path("/api/v1/organisation/members")
                                .queryParam("invalid", "invalid")
                                .build()
                )
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("With invalid sorting parameter -> 200 and Members Page")
    void getMembers_withInvalidSortingParameter_shouldReturn200AndPageResponse(){
        client.get()
                .uri(uriBuilder ->
                        uriBuilder.path("/api/v1/organisation/members")
                                .queryParam("sort", "invalid,asc")
                                .build()
                )
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .exchange()
                .expectStatus().isOk();
    }

    private void addUsersToOrganisation(String organisationId, OrgRole role, int count){
        for (int i = 0; i < count; i++){
            String secondaryUserJwt = helper.createUser();
            helper.addUserToOrganisation(secondaryUserJwt, organisationId, role);
        }
        membersCount += count;
        roleMembersCount.put(role, count);
    }

    private Stream<Arguments> filterCases(){
        return Stream.of(
                Arguments.of("READER", List.of(OrgRole.READER), roleMembersCount.get(OrgRole.READER)),
                Arguments.of("WRITER", List.of(OrgRole.WRITER), roleMembersCount.get(OrgRole.WRITER)),
                Arguments.of("NONE", List.of(OrgRole.NONE), roleMembersCount.get(OrgRole.NONE)),
                Arguments.of("READER,WRITER", List.of(OrgRole.READER, OrgRole.WRITER), roleMembersCount.get(OrgRole.READER) + roleMembersCount.get(OrgRole.WRITER)),
                Arguments.of("NONE,WRITER", List.of(OrgRole.NONE, OrgRole.WRITER), roleMembersCount.get(OrgRole.NONE) + roleMembersCount.get(OrgRole.WRITER)),
                Arguments.of("NONE,READER", List.of(OrgRole.NONE, OrgRole.READER), roleMembersCount.get(OrgRole.NONE) + roleMembersCount.get(OrgRole.READER))
        );
    }

    private Stream<Arguments> pageCases(){
        return Stream.of(
                Arguments.of(10, 0, "role,asc", OrgRole.NONE, OrgRole.OWNER),
                Arguments.of(10, 0, "role,desc", OrgRole.OWNER, OrgRole.NONE),
                Arguments.of(3, 0, "role,asc", OrgRole.NONE, OrgRole.NONE),
                Arguments.of(5, 0, "role,asc", OrgRole.NONE, OrgRole.READER),
                Arguments.of(2, 2, "role,asc", OrgRole.READER, OrgRole.READER),
                Arguments.of(2, 1, "role,desc", OrgRole.WRITER, OrgRole.READER)
        );
    }
}
