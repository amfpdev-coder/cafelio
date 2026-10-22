package com.cafelio.api.repository;

import com.cafelio.api.model.LibraryBook;
import com.cafelio.api.model.ReadingStatus;
import com.cafelio.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDate;


public interface LibraryBookRepository extends JpaRepository<LibraryBook, UUID> {

    int countByUserAndStatusAndFinishedAtBetween(
        User user,
        ReadingStatus status,
        LocalDate inicio,
        LocalDate fim
    );

    boolean existsByUserAndOpenLibraryId(User user, String openLibraryId);

    List<LibraryBook> findByUser(User user);

    Optional<LibraryBook> findByIdAndUser(UUID id, User user);

    List<LibraryBook> findByUserAndStatus(User user, ReadingStatus status);

    // coalesce porque sum() devolve null quando nenhuma linha bate, e
    // null viraria exceção ao desempacotar para long
    @Query("""
            select coalesce(sum(b.pages), 0)
              from LibraryBook b
             where b.user = :user
               and b.status = :status
               and b.finishedAt between :inicio and :fim
            """)
    long somarPaginas(
            @Param("user") User user,
            @Param("status") ReadingStatus status,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );
}
