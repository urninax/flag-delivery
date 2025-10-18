package me.urninax.flagdelivery.it.testcases.organisation.members;

import me.urninax.flagdelivery.it.AbstractIntegrationTest;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.organisation.ui.models.requests.ChangeMembersRoleRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("PATCH /api/v1/organisation/members/{uuid}")
public class ChangeRoleIT extends AbstractIntegrationTest {
    private String adminToken;
    private String writerToken;
    private UUID organisationId;
    private UUID adminId;
    private UUID writerId;
    private UUID ownerId;

    @BeforeAll
    void setup(){
        String ownerJwt = helper.createUser();
        organisationId = helper.createOrganisationForUser(ownerJwt);
        ownerId = helper.extractUserId(ownerJwt);

        String adminJwt = helper.createUser();
        adminId = helper.extractUserId(adminJwt);
        helper.addUserToOrganisation(adminId, organisationId, OrgRole.ADMIN);

        String writerJwt = helper.createUser();
        writerId = helper.extractUserId(writerJwt);
        helper.addUserToOrganisation(writerId, organisationId, OrgRole.WRITER);

        adminToken = helper.createAccessToken(adminJwt, false, OrgRole.ADMIN);
        writerToken = helper.createAccessToken(writerJwt, false, OrgRole.WRITER);
        helper.createAccessToken(writerJwt, false, OrgRole.WRITER);
        helper.createAccessToken(writerJwt, false, OrgRole.WRITER);
    }

    @ParameterizedTest(name = "[{index}] {0} changes role of {1} to {2} → expected {4}")
    @MethodSource("changeRoleCases")
    @DisplayName("Change Member Role Scenarios")
    void changeRole_scenario_shouldBehaveAsExpected(String controlToken, UUID userId, OrgRole newRole, OrgRole expectedRole, int expectedStatus){
        ChangeMembersRoleRequest request = new ChangeMembersRoleRequest(newRole);

        client.patch()
                .uri("/api/v1/organisation/members/{uuid}", userId)
                .header(HttpHeaders.AUTHORIZATION, controlToken)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);

        OrgRole updatedRole = helper.getUserRoleInOrganisation(userId, organisationId);
        assertEquals(expectedRole, updatedRole);
    }

    @Test
    @DisplayName("As ADMIN -> try to change another admin role -> 403")
    void changeRole_asAdminChangeAnotherAdminRole_shouldReturn403(){
        String otherAdminJwt = helper.createUser();
        UUID otherAdminId = helper.extractUserId(otherAdminJwt);
        helper.addUserToOrganisation(otherAdminId, organisationId, OrgRole.ADMIN);

        ChangeMembersRoleRequest request = new ChangeMembersRoleRequest(OrgRole.WRITER);

        client.patch()
                .uri("/api/v1/organisation/members/{uuid}", otherAdminId)
                .header(HttpHeaders.AUTHORIZATION, adminToken)
                .bodyValue(request)
                .exchange()
                .expectStatus().isForbidden();

        OrgRole updatedRole = helper.getUserRoleInOrganisation(otherAdminId, organisationId);
        assertEquals(OrgRole.ADMIN, updatedRole, "Admin role should remain unchanged");
    }

    @Test
    @DisplayName("Without auth token -> 403")
    void changeRole_withoutAuthToken_shouldReturn403(){
        ChangeMembersRoleRequest request = new ChangeMembersRoleRequest(OrgRole.READER);

        client.patch()
                .uri("/api/v1/organisation/members/{uuid}", writerId)
                .bodyValue(request)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("With invalid request body -> 400")
    void changeMemberRole_withInvalidBody_shouldReturn400() {
        client.patch()
                .uri("/api/v1/organisation/members/{uuid}", writerId)
                .header(HttpHeaders.AUTHORIZATION, adminToken)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Should trigger token downgrade on successful role change")
    void changeMemberRole_shouldTriggerTokenDowngradeAndCacheEviction() {
        ChangeMembersRoleRequest request = new ChangeMembersRoleRequest(OrgRole.READER);

        client.patch()
                .uri("/api/v1/organisation/members/{uuid}", writerId)
                .header(HttpHeaders.AUTHORIZATION, adminToken)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();

        boolean tokensDowngraded = helper.verifyTokensDowngraded(writerId, organisationId, OrgRole.READER);

        assertTrue(tokensDowngraded, "Tokens should have been downgraded");
    }

    private Stream<Arguments> changeRoleCases(){
        return Stream.of(
                Arguments.of(adminToken, writerId, OrgRole.WRITER, OrgRole.WRITER, 200),
                Arguments.of(adminToken, adminId, OrgRole.WRITER, OrgRole.ADMIN, 400),
                Arguments.of(adminToken, ownerId, OrgRole.WRITER, OrgRole.OWNER, 403),
                Arguments.of(writerToken, adminId, OrgRole.WRITER, OrgRole.ADMIN, 403),
                Arguments.of(adminToken, writerId, OrgRole.READER, OrgRole.READER, 200)
        );
    }
}
