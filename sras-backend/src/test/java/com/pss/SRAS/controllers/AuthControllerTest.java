package com.pss.SRAS.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pss.SRAS.dto.AuthResponse;
import com.pss.SRAS.dto.ForgotPasswordRequest;
import com.pss.SRAS.dto.LoginRequest;
import com.pss.SRAS.dto.ResetPasswordRequest;
import com.pss.SRAS.dto.SignupRequest;
import com.pss.SRAS.exception.GlobalExceptionHandler;
import com.pss.SRAS.models.enums.UserRole;
import com.pss.SRAS.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMVC tests for AuthController.
 * Uses standalone setup — no database, no running server, no Spring Security filter.
 * AuthService is mocked so tests run purely in-memory.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(
                        new ObjectMapper().registerModule(new JavaTimeModule())
                                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)))
                .build();
    }

    // ── Signup ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /auth/signup - success: returns 200 with token and user info")
    void signup_success() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setEmail("alice@example.com");
        req.setPassword("Password@123");
        req.setRole(UserRole.EMPLOYEE);

        AuthResponse resp = new AuthResponse("jwt-token-abc", "alice@example.com", UserRole.EMPLOYEE, 1L);
        when(authService.signup(any(SignupRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-abc"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    @DisplayName("POST /auth/signup - duplicate email: returns 400 with error message")
    void signup_duplicateEmail_returns400() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setEmail("existing@example.com");
        req.setPassword("Password@123");
        req.setRole(UserRole.EMPLOYEE);

        when(authService.signup(any(SignupRequest.class)))
                .thenThrow(new IllegalArgumentException("Email already registered: existing@example.com"));

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already registered: existing@example.com"));
    }

    @Test
    @DisplayName("POST /auth/signup - blank email: returns 400 without calling service")
    void signup_blankEmail_returns400() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setEmail("");
        req.setPassword("Password@123");
        req.setRole(UserRole.EMPLOYEE);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("POST /auth/signup - password too short: returns 400 without calling service")
    void signup_shortPassword_returns400() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setEmail("bob@example.com");
        req.setPassword("abc");          // min 6 chars required
        req.setRole(UserRole.PROJECT_MANAGER);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    // ── Login ───────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /auth/login - valid credentials: returns 200 with token")
    void login_success() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("alice@example.com");
        req.setPassword("Password@123");

        AuthResponse resp = new AuthResponse("jwt-xyz", "alice@example.com", UserRole.EMPLOYEE, 1L);
        when(authService.login(any(LoginRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-xyz"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"));
    }

    @Test
    @DisplayName("POST /auth/login - wrong password: returns 400")
    void login_badCredentials_returns400() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("alice@example.com");
        req.setPassword("WrongPass");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid email or password"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/login - blank password: returns 400 without calling service")
    void login_blankPassword_returns400() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("alice@example.com");
        req.setPassword("");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    // ── Forgot Password ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /auth/forgot-password - known email: returns 200 with reset token")
    void forgotPassword_success() throws Exception {
        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setEmail("alice@example.com");

        when(authService.forgotPassword("alice@example.com")).thenReturn("reset-token-456");

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reset token generated"))
                .andExpect(jsonPath("$.token").value("reset-token-456"));
    }

    @Test
    @DisplayName("POST /auth/forgot-password - unknown email: returns 400")
    void forgotPassword_unknownEmail_returns400() throws Exception {
        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setEmail("nobody@example.com");

        when(authService.forgotPassword("nobody@example.com"))
                .thenThrow(new IllegalArgumentException("No account with that email"));

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No account with that email"));
    }

    // ── Reset Password ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /auth/reset-password - valid token: returns 200 with success message")
    void resetPassword_success() throws Exception {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("valid-token-789");
        req.setNewPassword("NewPass@456");

        doNothing().when(authService).resetPassword("valid-token-789", "NewPass@456");

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset successful"));
    }

    @Test
    @DisplayName("POST /auth/reset-password - expired token: returns 400")
    void resetPassword_expiredToken_returns400() throws Exception {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("expired-token");
        req.setNewPassword("NewPass@456");

        doThrow(new IllegalArgumentException("Reset token has expired"))
                .when(authService).resetPassword("expired-token", "NewPass@456");

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Reset token has expired"));
    }

    @Test
    @DisplayName("POST /auth/reset-password - short new password: returns 400 without calling service")
    void resetPassword_shortPassword_returns400() throws Exception {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("some-token");
        req.setNewPassword("abc");      // min 8 chars required

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }
}
