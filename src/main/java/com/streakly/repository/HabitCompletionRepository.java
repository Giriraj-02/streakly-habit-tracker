package com.streakly.repository;

import com.streakly.model.HabitCompletion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HabitCompletionRepository extends JpaRepository<HabitCompletion, Long> {

    boolean existsByHabitIdAndCompletedOn(Long habitId, LocalDate completedOn);

    Optional<HabitCompletion> findByHabitIdAndCompletedOn(Long habitId, LocalDate completedOn);

    List<HabitCompletion> findAllByHabitIdOrderByCompletedOnAsc(Long habitId);

    void deleteByHabitIdAndCompletedOn(Long habitId, LocalDate completedOn);
}
