package com.cafelio.api.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "E-mail ou nome de usuário é obrigatório")
    private String identifier;

    @NotBlank(message = "Senha é obrigatória")
    private String password;

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}