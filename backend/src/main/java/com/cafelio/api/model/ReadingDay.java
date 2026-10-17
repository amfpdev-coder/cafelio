package com.cafelio.api.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "reading_days",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reading_day_user",
                        columnNames = {"user_id", "day"}
                )
        }
)
public class ReadingDay {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "day", nullable = false)
    private LocalDate day;

    public ReadingDay() {
    }

    public ReadingDay(User user, LocalDate day) {
        this.user = user;
        this.day = day;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public LocalDate getDay() {
        return day;
    }
}