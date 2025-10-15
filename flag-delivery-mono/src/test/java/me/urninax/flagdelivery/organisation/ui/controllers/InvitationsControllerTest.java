package me.urninax.flagdelivery.organisation.ui.controllers;

import me.urninax.flagdelivery.organisation.models.invitation.InvitationStatus;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.organisation.services.InvitationsService;
import me.urninax.flagdelivery.organisation.shared.InvitationPublicDTO;
import me.urninax.flagdelivery.organisation.utils.InvitationTokenUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InvitationsController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
public class InvitationsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InvitationsService invitationsService;

    private final String validToken = InvitationTokenUtils.generateToken();
    private final String invalidToken = "short_token";
    private final UUID invitationId = UUID.randomUUID();

    @Nested
    @DisplayName("GET /api/v1/invitations/{uuid}.{token}")
    class GetInvitationInfoCases{
        @Test
        @DisplayName("With valid request -> 200 and InvitationDTO")
        void getInvitationInfo_withValidRequest_shouldReturn200AndInvitationDTO() throws Exception {
            OrgRole invitationRole = OrgRole.ADMIN;
            String invitedBy = "Test admin";
            Instant expiresAt = Instant.now().plus(1, ChronoUnit.DAYS);
            String organisationName = "Test organisation";
            String invitationMessage = "Test invitation message";
            InvitationStatus status = InvitationStatus.PENDING;

            InvitationPublicDTO responseDTO = InvitationPublicDTO.builder()
                    .role(invitationRole)
                    .invitedBy(invitedBy)
                    .expiresAt(expiresAt)
                    .organisationName(organisationName)
                    .message(invitationMessage)
                    .status(status)
                    .build();

            when(invitationsService.getInvitationDTO(any(), any())).thenReturn(responseDTO);

            mockMvc.perform(get("/api/v1/invitations/{uuid}.{token}", invitationId, validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").value(invitationRole.name()))
                    .andExpect(jsonPath("$.invited_by").value(invitedBy))
                    .andExpect(jsonPath("$.expires_at").value(expiresAt.toString()))
                    .andExpect(jsonPath("$.organisation_name").value(organisationName))
                    .andExpect(jsonPath("$.message").value(invitationMessage))
                    .andExpect(jsonPath("$.status").value(status.name()));

            verify(invitationsService, times(1)).getInvitationDTO(any(), any());
        }

        @Test
        @DisplayName("With invalid token -> 400")
        void getInvitationInfo_withInvalidToken_shouldReturn400() throws Exception {
            mockMvc.perform(get("/api/v1/invitations/{uuid}.{token}", invitationId, invalidToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/invitations/{uuid}.{token}/accept")
    class AcceptInvitationCases{
        @Test
        @DisplayName("With valid request -> 204")
        void acceptInvitation_withValidRequest_shouldReturn201() throws Exception {
            mockMvc.perform(post("/api/v1/invitations/{uuid}.{token}/accept", invitationId, validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(invitationsService, times(1)).acceptInvitation(invitationId, validToken, false);
        }

        @Test
        @DisplayName("With transfer == True -> 204")
        void acceptInvitation_withTransferTrue_shouldReturn201() throws Exception {
            mockMvc.perform(post("/api/v1/invitations/{uuid}.{token}/accept", invitationId, validToken)
                            .param("transfer", "true")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(invitationsService, times(1)).acceptInvitation(invitationId, validToken, true);
        }

        @Test
        @DisplayName("With invalid token -> 400")
        void acceptInvitation_withInvalidToken_shouldReturn400() throws Exception {
            mockMvc.perform(post("/api/v1/invitations/{uuid}.{token}/accept", invitationId, invalidToken)
                            .param("transfer", "true")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(invitationsService, times(0)).acceptInvitation(invitationId, validToken, true);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/invitations/{uuid}.{token}/decline")
    class DeclineInvitationCases{
        @Test
        @DisplayName("With valid request -> 204")
        void declineInvitation_withValidRequest_shouldReturn201() throws Exception {
            mockMvc.perform(post("/api/v1/invitations/{uuid}.{token}/decline", invitationId, validToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(invitationsService, times(1)).declineInvitation(invitationId, validToken);
        }

        @Test
        @DisplayName("With invalid token -> 400")
        void declineInvitation_withInvalidToken_shouldReturn400() throws Exception{
            mockMvc.perform(post("/api/v1/invitations/{uuid}.{token}/decline", invitationId, invalidToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(invitationsService, times(0)).declineInvitation(invitationId, invalidToken);
        }
    }
}
