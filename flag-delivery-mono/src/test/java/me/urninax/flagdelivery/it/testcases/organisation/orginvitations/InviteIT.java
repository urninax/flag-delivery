package me.urninax.flagdelivery.it.testcases.organisation.orginvitations;

import me.urninax.flagdelivery.it.AbstractIntegrationTest;
import me.urninax.flagdelivery.organisation.models.invitation.Invitation;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.organisation.ui.models.requests.CreateInvitationRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("POST /api/v1/organisation/invitations")
public class InviteIT extends AbstractIntegrationTest {
    private String ownerToken;
    private String adminToken;
    private String writerToken;
    private UUID organisationId;
    private UUID adminId;

    @MockitoBean
    private JavaMailSender mailSender;

    @BeforeAll
    void setup(){
        String ownerJwt = helper.createUser();
        organisationId = helper.createOrganisationForUser(ownerJwt);
        ownerToken = helper.createAccessToken(ownerJwt, false, OrgRole.OWNER);

        String adminJwt = helper.createUser();
        adminId = helper.extractUserId(adminJwt);
        helper.addUserToOrganisation(adminId, organisationId, OrgRole.ADMIN);
        adminToken = helper.createAccessToken(adminJwt, false, OrgRole.ADMIN);

        String writerJwt = helper.createUser();
        UUID writerId = helper.extractUserId(writerJwt);
        helper.addUserToOrganisation(writerId, organisationId, OrgRole.WRITER);
        writerToken = helper.createAccessToken(writerJwt, false, OrgRole.WRITER);
    }

    @Test
    @DisplayName("OWNER invites ADMIN -> 201 and mail sent")
    void invite_ownerInvitesAdmin_shouldReturn201AndMailSent(){
        CreateInvitationRequest request = CreateInvitationRequest.builder()
                .email("admin_inv@example.com")
                .role(OrgRole.ADMIN)
                .message("Admin invitation")
                .build();

        client.post()
                .uri("/api/v1/organisation/invitations")
                .header(HttpHeaders.AUTHORIZATION, ownerToken)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();

        verify(mailSender, timeout(500)).send(any(SimpleMailMessage.class));

        Optional<Invitation> invitationOptional = helper.findInvitationByEmailAndOrgId(request.getEmail(), organisationId);

        assertThat(invitationOptional)
                .hasValueSatisfying(inv -> {
                    assertThat(inv.getRole()).isEqualTo(OrgRole.ADMIN);
                    assertThat(inv.getStatus().isActive()).isTrue();
                });
    }

    @Test
    @DisplayName("ADMIN invites READER -> 201 and mail sent")
    void invite_adminInvitesReader_shouldReturn201AndMailSent(){
        CreateInvitationRequest request = CreateInvitationRequest.builder()
                .email("reader_inv@example.com")
                .role(OrgRole.READER)
                .message("Reader invitation")
                .build();

        client.post()
                .uri("/api/v1/organisation/invitations")
                .header(HttpHeaders.AUTHORIZATION, adminToken)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();

        Optional<Invitation> invitationOptional = helper.findInvitationByEmailAndOrgId(request.getEmail(), organisationId);

        assertThat(invitationOptional)
                .hasValueSatisfying(inv -> {
                    assertThat(inv.getRole()).isEqualTo(OrgRole.READER);
                    assertThat(inv.getStatus().isActive()).isTrue();
                });
    }

    @Test
    @DisplayName("Without auth token -> 403")
    void invite_withoutAuthToken_shouldReturn403(){
        CreateInvitationRequest request = CreateInvitationRequest.builder()
                .email("fail@example.com")
                .role(OrgRole.WRITER)
                .message("Test invitation")
                .build();

        client.post()
                .uri("/api/v1/organisation/invitations")
                .bodyValue(request)
                .exchange()
                .expectStatus().isForbidden();

        Optional<Invitation> invitationOptional = helper.findInvitationByEmailAndOrgId(request.getEmail(), organisationId);

        assertThat(invitationOptional).isEmpty();
    }

    @Test
    @DisplayName("WRITER tries to invite READER -> 403")
    void invite_writerInvitesReader_shouldReturn403(){
        CreateInvitationRequest request = CreateInvitationRequest.builder()
                .email("reader_123@example.com")
                .role(OrgRole.READER)
                .message("Reader invitation")
                .build();

        client.post()
                .uri("/api/v1/organisation/invitations")
                .header(HttpHeaders.AUTHORIZATION, writerToken)
                .bodyValue(request)
                .exchange()
                .expectStatus().isForbidden();

        Optional<Invitation> invOpt = helper.findInvitationByEmailAndOrgId(request.getEmail(), organisationId);

        assertThat(invOpt).isEmpty();
    }

    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("negativeInviteCases")
    void invite_withNegativeCases_shouldBehaveAsExpected(String description, String authToken,
                                                OrgRole targetRole, int expectedStatus){
        String randomSuffix = UUID.randomUUID().toString().substring(0, 8);
        String email = String.format("%s_inv_%s@example.com", targetRole.toString().toLowerCase(), randomSuffix);

        CreateInvitationRequest request = CreateInvitationRequest.builder()
                .email(email)
                .role(targetRole)
                .message(randomSuffix)
                .build();

        client.post()
                .uri("/api/v1/organisation/invitations")
                .header(HttpHeaders.AUTHORIZATION, authToken)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);

        Optional<Invitation> invOpt = helper.findInvitationByEmailAndOrgId(request.getEmail(), organisationId);

        assertThat(invOpt).isEmpty();
    }

    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("positiveInviteCases")
    void invite_withPositiveCases_shouldBehaveAsExpected(String description, String authToken,
                                                         OrgRole targetRole, int expectedStatus){
        String randomSuffix = UUID.randomUUID().toString().substring(0, 8);
        String email = String.format("%s_inv_%s@example.com", targetRole.toString().toLowerCase(), randomSuffix);

        CreateInvitationRequest request = CreateInvitationRequest.builder()
                .email(email)
                .role(targetRole)
                .message(randomSuffix)
                .build();

        client.post()
                .uri("/api/v1/organisation/invitations")
                .header(HttpHeaders.AUTHORIZATION, authToken)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);

        verify(mailSender, timeout(1000)).send(any(SimpleMailMessage.class));

        Optional<Invitation> invOpt = helper.findInvitationByEmailAndOrgId(request.getEmail(), organisationId);

        assertThat(invOpt)
                .hasValueSatisfying(inv -> {
                    assertThat(inv.getRole()).isEqualTo(targetRole);
                    assertThat(inv.getStatus().isActive()).isTrue();
                });
    }

    private Stream<Arguments> negativeInviteCases(){
        return Stream.of(
                Arguments.of("Without auth token -> 403", "", OrgRole.WRITER, 403),
                Arguments.of("WRITER tries to invite READER -> 403", writerToken, OrgRole.READER, 403),
                Arguments.of("ADMIN tries to invite OWNER -> 403", adminToken, OrgRole.OWNER, 403),
                Arguments.of("ADMIN invites same role -> 403", adminToken, OrgRole.ADMIN, 403)
        );
    }

    private Stream<Arguments> positiveInviteCases(){
        return Stream.of(
                Arguments.of("OWNER invites ADMIN -> 201 and mail sent", ownerToken, OrgRole.ADMIN, 201),
                Arguments.of("ADMIN invites READER -> 201 and mail sent", adminToken, OrgRole.READER, 201)
        );
    }

    @Test
    @DisplayName("Invalid email format -> 400")
    void invite_withInvalidEmailFormat_shouldReturn400(){
        CreateInvitationRequest request = CreateInvitationRequest.builder()
                .email("not-an-email")
                .role(OrgRole.READER)
                .message("Invitation message")
                .build();

        client.post()
                .uri("/api/v1/organisation/invitations")
                .header(HttpHeaders.AUTHORIZATION, adminToken)
                .exchange()
                .expectStatus().isBadRequest();

        Optional<Invitation> invOpt = helper.findInvitationByEmailAndOrgId(request.getEmail(), organisationId);

        assertThat(invOpt).isEmpty();
    }

    @Test
    @DisplayName("With null role -> 400")
    void invite_withNullRole_shouldReturn400(){
        CreateInvitationRequest request = CreateInvitationRequest.builder()
                .email("fail@example.com")
                .message("Invitation message")
                .build();

        client.post()
                .uri("/api/v1/organisation/invitations")
                .header(HttpHeaders.AUTHORIZATION, adminToken)
                .exchange()
                .expectStatus().isBadRequest();

        Optional<Invitation> invOpt = helper.findInvitationByEmailAndOrgId(request.getEmail(), organisationId);

        assertThat(invOpt).isEmpty();
    }

    @Test
    @DisplayName("Email already invited and PENDING -> 409")
    void invite_whenEmailAlreadyInvited_shouldReturn409(){
        String randomSuffix = UUID.randomUUID().toString().substring(0, 8);
        String email = String.format("inv_%s@example.com", randomSuffix);

        CreateInvitationRequest request = CreateInvitationRequest.builder()
                .email(email)
                .role(OrgRole.WRITER)
                .message("Invitation Message")
                .build();

        client.post()
                .uri("/api/v1/organisation/invitations")
                .header(HttpHeaders.AUTHORIZATION, adminToken)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();

        client.post()
                .uri("/api/v1/organisation/invitations")
                .header(HttpHeaders.AUTHORIZATION, adminToken)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    @DisplayName("User with same email already exists in organisation -> 409")
    void invite_whenUserAlreadyInSameOrg_shouldReturn409(){
        String existingEmail = helper.getUserEmailById(adminId);

        CreateInvitationRequest request = CreateInvitationRequest.builder()
                .email(existingEmail)
                .role(OrgRole.READER)
                .message("Already member")
                .build();

        client.post()
                .uri("/api/v1/organisation/invitations")
                .header(HttpHeaders.AUTHORIZATION, ownerToken)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(409);

        Optional<Invitation> invOpt = helper.findInvitationByEmailAndOrgId(request.getEmail(), organisationId);

        assertThat(invOpt).isEmpty();
    }

    @Test
    @DisplayName("ADMIN invites READER but mailSender throws exception -> still 201")
    void invite_whenMailSenderThrowsException_shouldReturn201(){
        doThrow(new RuntimeException("Simulated mail failure")).when(mailSender).send(any(SimpleMailMessage.class));

        CreateInvitationRequest request = CreateInvitationRequest.builder()
                .email("mailfail@example.com")
                .role(OrgRole.READER)
                .message("fail test")
                .build();

        client.post()
                .uri("/api/v1/organisation/invitations")
                .header(HttpHeaders.AUTHORIZATION, adminToken)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();

        Optional<Invitation> invOpt = helper.findInvitationByEmailAndOrgId(request.getEmail(), organisationId);

        assertThat(invOpt)
                .hasValueSatisfying(inv -> {
                    assertThat(inv.getRole()).isEqualTo(OrgRole.READER);
                    assertThat(inv.getStatus().isActive()).isTrue();
                });
    }

    @Test
    @DisplayName("Message exceeds max length -> 400")
    void invite_whenMessageExceedsMaxLength_shouldReturn400(){
        String longMessage = "x".repeat(1000);

        CreateInvitationRequest request = CreateInvitationRequest.builder()
                .email("mailfail@example.com")
                .role(OrgRole.READER)
                .message(longMessage)
                .build();

        client.post()
                .uri("/api/v1/organisation/invitations")
                .header(HttpHeaders.AUTHORIZATION, adminToken)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();

        Optional<Invitation> invOpt = helper.findInvitationByEmailAndOrgId(request.getEmail(), organisationId);

        assertThat(invOpt).isEmpty();
    }

    @Test
    @DisplayName("Email case-insensitivity check -> 201")
    void invite_emailCaseInsensitive_shouldReturn201(){
        CreateInvitationRequest request = CreateInvitationRequest.builder()
                .email("CaseTest@example.com")
                .role(OrgRole.READER)
                .message("Test")
                .build();

        client.post()
                .uri("/api/v1/organisation/invitations")
                .header(HttpHeaders.AUTHORIZATION, adminToken)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();

        Optional<Invitation> invOpt = helper.findInvitationByEmailAndOrgId(request.getEmail(), organisationId);

        assertThat(invOpt)
                .hasValueSatisfying(inv -> {
                    assertThat(inv.getRole()).isEqualTo(OrgRole.READER);
                    assertThat(inv.getStatus().isActive()).isTrue();
                });
    }
}
