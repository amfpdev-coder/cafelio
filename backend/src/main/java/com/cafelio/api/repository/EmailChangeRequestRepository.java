package com.cafelio.api.repository;

import com.cafelio.api.model.EmailChangeRequest;
import com.cafelio.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmailChangeRequestRepository extends JpaRepository<EmailChangeRequest, UUID> {

    Optional<EmailChangeRequest> findByUser(User user);

    void deleteByUser(User user);
}
