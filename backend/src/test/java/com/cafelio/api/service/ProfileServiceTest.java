package com.cafelio.api.service;

import com.cafelio.api.model.User;
import com.cafelio.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageService imageService;

    private ProfileService service;

    private final UUID userId = UUID.randomUUID();

    private final MultipartFile arquivo =
            new MockMultipartFile("file", "foto.jpg", "image/jpeg", new byte[]{1, 2, 3});

    @BeforeEach
    void setUp() {
        service = new ProfileService(userRepository, imageService);
    }

    @Test
    void atualizarFoto_semFotoAnterior_salvaENaoApagaNada() {
        User user = new User();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(imageService.enviarFotoDePerfil(arquivo)).thenReturn("cafelio/perfil/nova");

        service.atualizarFoto(userId, arquivo);

        assertThat(user.getProfilePicturePublicId()).isEqualTo("cafelio/perfil/nova");
        verify(userRepository).save(user);
        verify(imageService, never()).apagar(any());
    }

    @Test
    void atualizarFoto_comFotoAnterior_salvaAntesDeApagarAAntiga() {
        User user = new User();
        user.setProfilePicturePublicId("cafelio/perfil/antiga");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(imageService.enviarFotoDePerfil(arquivo)).thenReturn("cafelio/perfil/nova");

        service.atualizarFoto(userId, arquivo);

        assertThat(user.getProfilePicturePublicId()).isEqualTo("cafelio/perfil/nova");

        InOrder ordem = inOrder(imageService, userRepository);
        ordem.verify(imageService).enviarFotoDePerfil(arquivo);
        ordem.verify(userRepository).save(user);
        ordem.verify(imageService).apagar("cafelio/perfil/antiga");
    }

    @Test
    void atualizarFoto_falhaNoEnvio_naoAlteraOBancoNemApagaAAntiga() {
        User user = new User();
        user.setProfilePicturePublicId("cafelio/perfil/antiga");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(imageService.enviarFotoDePerfil(arquivo))
                .thenThrow(new IllegalArgumentException("A imagem deve ter no máximo 2 MB"));

        assertThatThrownBy(() -> service.atualizarFoto(userId, arquivo))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(user.getProfilePicturePublicId()).isEqualTo("cafelio/perfil/antiga");
        verify(userRepository, never()).save(any());
        verify(imageService, never()).apagar(any());
    }

    @Test
    void removerFoto_limpaOCampoEApagaNoCloudinary() {
        User user = new User();
        user.setProfilePicturePublicId("cafelio/perfil/antiga");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        service.removerFoto(userId);

        assertThat(user.getProfilePicturePublicId()).isNull();
        verify(userRepository).save(user);
        verify(imageService).apagar("cafelio/perfil/antiga");
    }

    @Test
    void atualizarFoto_usuarioInexistente_naoEnviaNada() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizarFoto(userId, arquivo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Usuário não encontrado");

        verify(imageService, never()).enviarFotoDePerfil(any());
    }
}