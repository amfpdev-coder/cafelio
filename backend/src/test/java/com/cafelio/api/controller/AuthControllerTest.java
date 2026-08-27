package com.cafelio.api.controller;

import com.cafelio.api.model.User;
import com.cafelio.api.security.GoogleTokenVerifier;
import com.cafelio.api.service.AuthService;
import com.cafelio.api.service.JwtService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("local")
class AuthControllerTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private GoogleTokenVerifier googleTokenVerifier;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void tokenValido_retornaJwt() throws Exception {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setSubject("google-123");
        payload.setEmail("ana@example.com");
        payload.set("name", "Ana");

        User user = new User();
        user.setId(UUID.randomUUID());

        when(googleTokenVerifier.verify("token-falso")).thenReturn(payload);
        when(authService.loginOrRegisterWithGoogle("google-123", "ana@example.com", "Ana")).thenReturn(user);
        when(jwtService.generateToken(user.getId())).thenReturn("jwt-fake");

        mockMvc.perform(post("/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"token-falso\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-fake"));
    }

    @Test
    void semIdToken_retorna400() throws Exception {
        mockMvc.perform(post("/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tokenInvalido_retorna400() throws Exception {
        when(googleTokenVerifier.verify(any())).thenThrow(new IllegalArgumentException("Token do Google inválido"));

        mockMvc.perform(post("/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"token-invalido\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Token do Google inválido"));
    }
}
