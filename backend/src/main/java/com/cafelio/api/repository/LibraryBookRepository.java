package com.cafelio.api.repository;

import com.cafelio.api.model.LibraryBook;
import com.cafelio.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LibraryBookRepository extends JpaRepository<LibraryBook, UUID> {

   boolean existsByUserAndOpenLibraryId(User user, String openLibraryId);

   List<LibraryBook> findByUser(User user);
}
