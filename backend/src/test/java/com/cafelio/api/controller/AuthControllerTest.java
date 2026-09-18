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
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
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
        when(authService.loginOrRegisterWithGoogle(
                "google-123",
                "ana@example.com",
                "Ana"
        )).thenReturn(user);

        when(jwtService.generateToken(user.getId()))
                .thenReturn("jwt-fake");

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
        when(googleTokenVerifier.verify(any()))
                .thenThrow(new IllegalArgumentException(
                        "Token do Google inválido"
                ));

        mockMvc.perform(post("/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"token-invalido\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Token do Google inválido"));
    }

    @Test
    void credenciaisValidas_retornaJwt() throws Exception {
        User user = new User();
        user.setId(UUID.randomUUID());

        when(authService.login(any())).thenReturn(user);
        when(jwtService.generateToken(user.getId()))
                .thenReturn("jwt-fake");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identifier": "ana@example.com",
                                  "password": "Senha123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-fake"));
    }

    @Test
    void credenciaisInvalidas_retorna400() throws Exception {
        when(authService.login(any()))
                .thenThrow(new IllegalArgumentException(
                        "E-mail/usuário ou senha incorretos"
                ));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identifier": "ana@example.com",
                                  "password": "errada"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("E-mail/usuário ou senha incorretos"));
    }

    @Test
    void loginSemCampos_retorna400() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void meSemToken_retorna401() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error")
                        .value("Autenticação necessária"));
    }

    @Test
    void meComTokenInvalido_retorna401() throws Exception {
        when(jwtService.extractUserId(any()))
                .thenThrow(new RuntimeException("assinatura invalida"));

        mockMvc.perform(get("/auth/me")
                        .header(
                                "Authorization",
                                "Bearer token-falso"
                        ))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meComTokenValido_retornaDadosDaUsuaria() throws Exception {
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setUsername("angelica");
        user.setEmail("angelica@example.com");

        when(jwtService.extractUserId("token-valido"))
                .thenReturn(userId);

        when(authService.findById(userId))
                .thenReturn(user);

        mockMvc.perform(get("/auth/me")
                        .header(
                                "Authorization",
                                "Bearer token-valido"
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username")
                        .value("angelica"))
                .andExpect(jsonPath("$.email")
                        .value("angelica@example.com"));
    }
}