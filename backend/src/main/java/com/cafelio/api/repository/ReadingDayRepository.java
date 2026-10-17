package com.cafelio.api.repository;

import com.cafelio.api.model.ReadingDay;
import com.cafelio.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ReadingDayRepository extends JpaRepository<ReadingDay, UUID> {
    boolean existsByUserAndDay(User user, LocalDate day);

    List<ReadingDay> findByUserOrderByDayDesc(User user);
}