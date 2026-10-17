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

    public String urlDaFoto(User user) {
        return imageService.urlDaFoto(user.getProfilePicturePublicId());
    }

    private User buscar(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
    }
}