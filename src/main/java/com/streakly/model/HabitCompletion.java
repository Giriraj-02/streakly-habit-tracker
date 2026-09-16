package com.streakly.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(
    name = "habit_completions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"habit_id", "completed_on"})
)
public class HabitCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "habit_id", nullable = false)
    private Habit habit;

    @Column(name = "completed_on", nullable = false)
    private LocalDate completedOn;

    protected HabitCompletion() {
    }

    public HabitCompletion(Habit habit, LocalDate completedOn) {
        this.habit = habit;
        this.completedOn = completedOn;
    }

    public Long getId() {
        return id;
    }

    public Habit getHabit() {
        return habit;
    }

    public LocalDate getCompletedOn() {
        return completedOn;
    }
}
