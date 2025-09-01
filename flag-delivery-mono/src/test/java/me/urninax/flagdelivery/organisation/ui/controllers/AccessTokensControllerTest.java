package me.urninax.flagdelivery.organisation.ui.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.urninax.flagdelivery.organisation.models.membership.OrgRole;
import me.urninax.flagdelivery.organisation.services.AccessTokenService;
import me.urninax.flagdelivery.organisation.shared.AccessTokenDTO;
import me.urninax.flagdelivery.organisation.ui.models.requests.CreateAccessTokenRequest;
import me.urninax.flagdelivery.shared.security.CurrentUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AccessTokensController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class})
public class AccessTokensControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccessTokenService accessTokenService;

    @MockitoBean
    private CurrentUser currentUser;

    private UUID userId;

    @BeforeEach
    void setUp(){
        userId = UUID.randomUUID();
        when(currentUser.getUserId()).thenReturn(userId);
    }

    @Nested
    @DisplayName("POST /api/v1/organisation/access-tokens")
    class CreateAccessTokenCases {
        private CreateAccessTokenRequest request;

        @BeforeEach
        void setUp(){
            request = CreateAccessTokenRequest.builder()
                    .name("CI token")
                    .role(OrgRole.READER)
                    .isService(true)
                    .build();
        }

        @Test
        @DisplayName("With valid request -> 201 and Authorization header")
        void createAccessToken_withValidRequest_shouldReturn201() throws Exception {
            when(accessTokenService.issueToken(eq(userId), any())).thenReturn("api-abc-123");

            mockMvc.perform(post("/api/v1/organisation/access-tokens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Authorization"));

            verify(currentUser, times(1)).getUserId();
            verify(accessTokenService, times(1)).issueToken(eq(userId), any());
        }

        @Test
        @DisplayName("With invalid request (blank name) -> 400")
        void createAccessToken_withBlankName_shouldReturn400() throws Exception {
            request.setName("");

            mockMvc.perform(post("/api/v1/organisation/access-tokens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("Name cannot be blank")));
        }

        @Test
        @DisplayName("With invalid request (missing role) -> 400")
        void createAccessToken_missingRole_shouldReturn400() throws Exception {
            request.setRole(null);

            mockMvc.perform(post("/api/v1/organisation/access-tokens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("Role must be present")));
        }

        @Test
        @DisplayName("With empty body -> 400")
        void createAccessToken_withEmptyBody_shouldReturn400() throws Exception {
            mockMvc.perform(post("/api/v1/organisation/access-tokens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Malformed request"));
        }

        @Test
        @DisplayName("With HTTP method not POST -> 405")
        void createAccessToken_methodNotAllowed_shouldReturn405() throws Exception {
            mockMvc.perform(delete("/api/v1/organisation/access-tokens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("With ContentType not application/json -> 415")
        void createAccessToken_withContentTypeNotJson_shouldReturn415() throws Exception {
            mockMvc.perform(post("/api/v1/organisation/access-tokens")
                            .contentType(MediaType.APPLICATION_XML)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        @DisplayName("With Accept not application/json -> 406")
        void createAccessToken_withAcceptNotJson_shouldReturn406() throws Exception {
            mockMvc.perform(post("/api/v1/organisation/access-tokens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_XML)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotAcceptable());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/organisation/access-tokens")
    class GetAccessTokensCases {
        @Test
        @DisplayName("With default pageable -> 200 and proper PageResponse")
        void getAccessTokens_shouldReturnPage() throws Exception {
            AccessTokenDTO dto1 = new AccessTokenDTO();
            dto1.setTokenHint("abc123");
            dto1.setName("Build");
            dto1.setRole(OrgRole.READER);
            dto1.setIssuedAt(Instant.now());
            dto1.setLastUsed(Instant.now());
            dto1.setService(false);
            dto1.setMemberId(UUID.randomUUID());

            List<AccessTokenDTO> content = List.of(dto1);
            Page<AccessTokenDTO> page = new PageImpl<>(content, PageRequest.of(0, 25), 1);

            when(accessTokenService.getTokensForUserInOrg(eq(userId), any(), any())).thenReturn(page);

            mockMvc.perform(get("/api/v1/organisation/access-tokens")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(25))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.content[0].name").value("Build"))
                    .andExpect(jsonPath("$.content[0].role").value("READER"))
                    .andExpect(jsonPath("$.content[0].token_hint").value("abc123"));

            verify(accessTokenService, times(1)).getTokensForUserInOrg(eq(userId), any(), any());
        }
    }
}
