package com.cafelio.api.repository;

import com.cafelio.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByGoogleId(String googleID);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
