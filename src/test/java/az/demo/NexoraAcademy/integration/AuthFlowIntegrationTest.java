package az.demo.NexoraAcademy.integration;

import az.demo.NexoraAcademy.service.notify.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the full auth flow (register -> verify email OTP -> login -> refresh -> logout)
 * over real HTTP, through the real Spring Security filter chain, against the live
 * Postgres database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AuthFlowIntegrationTest {

    /**
     * EmailService#send is {@code @Async} — the invocation is recorded on a background
     * thread, so a bare verify() races the request thread and intermittently reports
     * "zero interactions". Every verification below waits instead of asserting instantly.
     */
    private static final long EMAIL_TIMEOUT_MS = 10_000L;

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
        String registerBody = "{\"email\":\"" + email + "\",\"firstName\":\"Flow\",\"lastName\":\"Test User\",\"password\":\"" + password + "\"}";

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email));

        // 2. capture the verification email that was "sent" and pull the 6-digit OTP out of it
        org.mockito.ArgumentCaptor<String> bodyCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(emailService, timeout(EMAIL_TIMEOUT_MS)).send(eq(email), anyString(), bodyCaptor.capture());
        String verifyOtp = extractOtp(bodyCaptor.getValue());

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType("application/json")
                        .content("{\"email\":\"" + email + "\",\"otp\":\"" + verifyOtp + "\"}"))
                .andExpect(status().isNoContent());

        // 3. login (email+password) -> tokens issued directly
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
        // NOTE: do NOT stub the spy here (doNothing().when(emailService).send(...)).
        // send() is @Async, so the stubbing call is dispatched to a background thread and
        // never completes on this one — Mockito is left with an unfinished stubbing whose
        // dangling matchers then fail *every subsequent test class* in the same JVM with
        // InvalidUseOfMatchers/UnfinishedStubbing. Stubbing is unnecessary anyway:
        // EmailService swallows send failures by design, so an unreachable SMTP host is harmless.
        String email = "dup-" + UUID.randomUUID() + "@example.com";
        String body = "{\"email\":\"" + email + "\",\"firstName\":\"Dup\",\"lastName\":\"User\",\"password\":\"s3cret-password\"}";

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
        String body = "{\"email\":\"weak-" + UUID.randomUUID() + "@example.com\",\"firstName\":\"Weak\",\"lastName\":\"Password\",\"password\":\"short\"}";

        mockMvc.perform(post("/api/v1/auth/register").contentType("application/json").content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    private String extractOtp(String emailBody) {
        Matcher matcher = Pattern.compile("\\b(\\d{6})\\b").matcher(emailBody);
        if (!matcher.find()) {
            throw new IllegalStateException("No 6-digit OTP found in email body: " + emailBody);
        }
        return matcher.group(1);
    }
}
