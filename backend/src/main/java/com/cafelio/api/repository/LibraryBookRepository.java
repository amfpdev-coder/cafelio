package com.cafelio.api.repository;

import com.cafelio.api.model.LibraryBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LibraryBookRepository extends JpaRepository<LibraryBook, UUID> {
}
