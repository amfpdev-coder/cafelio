package com.cafelio.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.cafelio.api.model.Book;
import com.cafelio.api.model.BookSource;

public interface BookRepository extends JpaRepository<Book, UUID> {

    // Primeira rede contra duplicata
    Optional<Book> findByIsbn13(String isbn13);

    // Segunda rede: quem cadastra sem ISBN
    List<Book> findByTitleNormalized(String titleNormalized);

    // Evita importar o mesmo registro de origem duas vezes
    Optional<Book> findBySourceAndExternalId(BookSource source, String externalId);

    // Busca no acervo
    Page<Book> findByTitleNormalizedContaining(String termo, Pageable pageable);
}
