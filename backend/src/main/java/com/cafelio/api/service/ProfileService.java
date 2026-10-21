package com.cafelio.api.service;

import com.cafelio.api.model.User;
import com.cafelio.api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final ImageService imageService;

    public ProfileService(UserRepository userRepository, ImageService imageService) {
        this.userRepository = userRepository;
        this.imageService = imageService;
    }

    @Transactional
    public User atualizarFoto(UUID userId, MultipartFile arquivo) {
        User user = buscar(userId);

        String anterior = user.getProfilePicturePublicId();
        String nova = imageService.enviarFotoDePerfil(arquivo);

        user.setProfilePicturePublicId(nova);
        userRepository.save(user);

        if (anterior != null && !anterior.equals(nova)) {
            imageService.apagar(anterior);
        }

        return user;
    }

    @Transactional
    public User removerFoto(UUID userId) {
        User user = buscar(userId);

        String anterior = user.getProfilePicturePublicId();
        user.setProfilePicturePublicId(null);
        userRepository.save(user);

        imageService.apagar(anterior);

        return user;
    }

    @Transactional
    public User atualizarDados(UUID userId, String name, String username) {
        User user = buscar(userId);
        String novoUsername = username.trim();

        if (!user.getUsername().equals(novoUsername) && userRepository.existsByUsername(novoUsername)) {
            throw new IllegalArgumentException("Esse nome de usuário já está em uso");
        }

        user.setName(name.trim());
        user.setUsername(novoUsername);

        return userRepository.save(user);
    }

    @Transactional
    public void excluirConta(UUID userId) {
        User user = buscar(userId);
        String foto = user.getProfilePicturePublicId();

        // A foto mora no Cloudinary, fora do banco. Apagar a usuária some com
        // o apontador e deixaria a imagem órfã lá para sempre — então ela sai
        // primeiro, enquanto ainda se sabe de quem é.
        userRepository.delete(user);

        if (foto != null) {
            imageService.apagar(foto);
        }
    }

    public String urlDaFoto(User user) {
        return imageService.urlDaFoto(user.getProfilePicturePublicId());
    }

    private User buscar(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
    }
}