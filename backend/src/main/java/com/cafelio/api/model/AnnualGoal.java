package com.cafelio.api.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(
        name = "annual_goals",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_annual_goal_user_year",
                        columnNames = {"user_id", "year"}
                )
        }
)
public class AnnualGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "year", nullable = false)
    private int year;

    @Column(name = "target", nullable = false)
    private int target;

    public AnnualGoal() {
    }

    public AnnualGoal(User user, int year, int target) {
        this.user = user;
        this.year = year;
        this.target = target;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public int getYear() {
        return year;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }
}
