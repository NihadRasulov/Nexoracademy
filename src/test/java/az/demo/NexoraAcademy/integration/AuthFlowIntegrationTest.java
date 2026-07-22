package az.demo.NexoraAcademy.integration;

import az.demo.NexoraAcademy.service.notify.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the full auth flow (register -> verify email -> login -> refresh
 * -> logout) over real HTTP, through the real Spring Security filter chain,
 * against the live Postgres database.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoSpyBean
    private EmailService emailService;

    @Test
    void registerVerifyLoginRefreshLogout() throws Exception {
        String email = "flow-" + UUID.randomUUID() + "@example.com";
        String password = "s3cret-password";

        // 1. register
        String registerBody = "{\"email\":\"" + email + "\",\"fullName\":\"Flow Test User\",\"password\":\"" + password + "\"}";

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email));

        // 2. capture the verification email that was "sent" and pull the token out of its link
        org.mockito.ArgumentCaptor<String> bodyCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(emailService).send(eq(email), anyString(), bodyCaptor.capture());
        String verifyToken = extractToken(bodyCaptor.getValue());

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType("application/json")
                        .content("{\"token\":\"" + verifyToken + "\"}"))
                .andExpect(status().isNoContent());

        // 3. login
        String loginBody = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn().getResponse().getContentAsString();

        String refreshToken = objectMapper.readTree(loginResponse).get("refreshToken").asText();

        // 4. refresh — must succeed once, and the old token must not be reusable
        String refreshResponse = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType("application/json")
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String rotatedRefreshToken = objectMapper.readTree(refreshResponse).get("refreshToken").asText();
        org.assertj.core.api.Assertions.assertThat(rotatedRefreshToken).isNotEqualTo(refreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType("application/json")
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isUnauthorized());

        // 5. logout revokes the current refresh token
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType("application/json")
                        .content("{\"refreshToken\":\"" + rotatedRefreshToken + "\"}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType("application/json")
                        .content("{\"refreshToken\":\"" + rotatedRefreshToken + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registeringWithDuplicateEmailReturns409() throws Exception {
        doNothing().when(emailService).send(anyString(), anyString(), anyString());
        String email = "dup-" + UUID.randomUUID() + "@example.com";
        String body = "{\"email\":\"" + email + "\",\"fullName\":\"Dup User\",\"password\":\"s3cret-password\"}";

        mockMvc.perform(post("/api/v1/auth/register").contentType("application/json").content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register").contentType("application/json").content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void loginWithWrongPasswordReturns401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"nobody@example.com\",\"password\":\"whatever123\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerWithWeakPasswordReturns400WithFieldErrors() throws Exception {
        String body = "{\"email\":\"weak-" + UUID.randomUUID() + "@example.com\",\"fullName\":\"Weak\",\"password\":\"short\"}";

        mockMvc.perform(post("/api/v1/auth/register").contentType("application/json").content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    private String extractToken(String emailBody) {
        Matcher matcher = Pattern.compile("token=([A-Za-z0-9_-]+)").matcher(emailBody);
        if (!matcher.find()) {
            throw new IllegalStateException("No token found in email body: " + emailBody);
        }
        return matcher.group(1);
    }
}
