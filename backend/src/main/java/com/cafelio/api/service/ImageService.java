package com.cafelio.api.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
public class ImageService {

    private static final long TAMANHO_MAXIMO = 2 * 1024 * 1024;
    private static final Set<String> TIPOS_PERMITIDOS = Set.of("image/jpeg", "image/png", "image/webp");

    private final Cloudinary cloudinary;

    public ImageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String enviarFotoDePerfil(MultipartFile arquivo) {
        validar(arquivo);

        try {
            Map<?, ?> resultado = cloudinary.uploader().upload(
                    arquivo.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "cafelio/perfil",
                            "resource_type", "image"
                    )
            );
            return resultado.get("public_id").toString();
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível enviar a imagem", e);
        }
    }

    public void apagar(String publicId) {
        if (publicId == null) {
            return;
        }

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            // a foto nova já foi salva; falhar aqui só deixaria lixo no Cloudinary
        }
    }

    public String urlDaFoto(String publicId) {
        if (publicId == null) {
            return null;
        }

        return cloudinary.url()
                .secure(true)
                .transformation(new Transformation<>()
                        .width(256).height(256).crop("fill").gravity("face"))
                .generate(publicId);
    }

    private void validar(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Selecione uma imagem");
        }

        if (arquivo.getSize() > TAMANHO_MAXIMO) {
            throw new IllegalArgumentException("A imagem deve ter no máximo 2 MB");
        }

        if (!TIPOS_PERMITIDOS.contains(arquivo.getContentType())) {
            throw new IllegalArgumentException("A imagem deve ser JPG, PNG ou WebP");
        }
    }
}