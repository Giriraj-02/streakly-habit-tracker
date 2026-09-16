package com.streakly.service;

import com.streakly.dto.HabitRequest;
import com.streakly.model.Frequency;
import com.streakly.model.Habit;
import com.streakly.model.HabitCompletion;
import com.streakly.repository.HabitCompletionRepository;
import com.streakly.repository.HabitRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HabitService {

    private static final int CHALLENGE_DAYS = 75;

    private final HabitRepository habitRepository;
    private final HabitCompletionRepository completionRepository;

    public HabitService(HabitRepository habitRepository,
                        HabitCompletionRepository completionRepository) {
        this.habitRepository = habitRepository;
        this.completionRepository = completionRepository;
    }

    public List<HabitResponse> getTodayHabits() {
        LocalDate today = LocalDate.now();

        return habitRepository.findAllByOrderByIdAsc().stream()
                .filter(h -> !h.isArchived())
                .filter(h -> isApplicable(h, today))
                .map(h -> toResponse(h, today))
                .toList();
    }

    public List<HabitResponse> getAllHabits() {
        LocalDate today = LocalDate.now();

        return habitRepository.findAllByOrderByIdAsc().stream()
                .filter(h -> !h.isArchived())
                .map(h -> toResponse(h, today))
                .toList();
    }

    public List<HabitResponse> getArchivedHabits() {
        LocalDate today = LocalDate.now();

        return habitRepository.findAllByOrderByIdAsc().stream()
                .filter(Habit::isArchived)
                .map(h -> toResponse(h, today))
                .toList();
    }

    public List<HabitResponse> search(String query) {
        LocalDate today = LocalDate.now();
        String q = query == null ? "" : query.trim().toLowerCase();

        return habitRepository.findAllByOrderByIdAsc().stream()
                .filter(h -> !h.isArchived())
                .filter(h -> h.getName().toLowerCase().contains(q))
                .map(h -> toResponse(h, today))
                .toList();
    }

    @Transactional
    public HabitResponse addHabit(HabitRequest request) {

        // Validate habit name
        if (request == null || request.name() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Habit name is required"
            );
        }

        String name = request.name().trim();

        // Empty habit name is not allowed
        if (name.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Habit name cannot be empty"
            );
        }

        // Habit name must contain at least one letter
        // This prevents values like "15", "123", "999", "@@@", etc.
        if (!name.matches(".*[A-Za-z].*")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Habit name must contain at least one letter"
            );
        }

        // Frequency is required
        if (request.frequency() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Habit frequency is required"
            );
        }

        Habit habit = habitRepository.save(
                new Habit(name, request.frequency())
        );

        return toResponse(habit, LocalDate.now());
    }

    @Transactional
    public HabitResponse toggleCompletion(Long habitId, LocalDate date) {
        Habit habit = getHabit(habitId);

        if (!isApplicable(habit, date)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This habit is not applicable on " + date
            );
        }

        Optional<HabitCompletion> existing =
                completionRepository.findByHabitIdAndCompletedOn(habitId, date);

        if (existing.isPresent()) {
            completionRepository.delete(existing.get());
        } else {
            completionRepository.save(new HabitCompletion(habit, date));
        }

        return toResponse(habit, LocalDate.now());
    }

    @Transactional
    public HabitResponse setArchived(Long habitId, boolean archived) {
        Habit habit = getHabit(habitId);
        habit.setArchived(archived);
        habitRepository.save(habit);
        return toResponse(habit, LocalDate.now());
    }

    public DashboardResponse getDashboard() {
        List<Habit> habits = habitRepository.findAllByOrderByIdAsc();
        LocalDate today = LocalDate.now();

        if (habits.isEmpty()) {
            return new DashboardResponse(1, CHALLENGE_DAYS, 0, 0, 0);
        }

        LocalDate challengeStart = habits.stream()
                .map(Habit::getCreatedOn)
                .min(LocalDate::compareTo)
                .orElse(today);

        long rawDay = ChronoUnit.DAYS.between(challengeStart, today) + 1;
        int day = (int) Math.max(1, Math.min(CHALLENGE_DAYS, rawDay));

        List<Habit> activeToday = habits.stream()
                .filter(h -> !h.isArchived())
                .filter(h -> isApplicable(h, today))
                .toList();

        int completedToday = (int) activeToday.stream()
                .filter(h -> completionRepository.existsByHabitIdAndCompletedOn(h.getId(), today))
                .count();

        int totalActive = (int) habits.stream()
                .filter(h -> !h.isArchived())
                .count();

        int progressPercent = (int) Math.round((day * 100.0) / CHALLENGE_DAYS);

        return new DashboardResponse(
                day,
                CHALLENGE_DAYS,
                completedToday,
                activeToday.size(),
                progressPercent
        );
    }

    private Habit getHabit(Long id) {
        return habitRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Habit not found"));
    }

    private HabitResponse toResponse(Habit habit, LocalDate today) {
        Set<LocalDate> completedDates = completionRepository
                .findAllByHabitIdOrderByCompletedOnAsc(habit.getId())
                .stream()
                .map(HabitCompletion::getCompletedOn)
                .collect(Collectors.toCollection(TreeSet::new));

        boolean completedToday = completedDates.contains(today);
        int currentStreak = calculateCurrentStreak(habit, completedDates, today);
        int bestStreak = calculateBestStreak(habit, completedDates);

        return new HabitResponse(
                habit.getId(),
                habit.getName(),
                habit.getFrequency().name(),
                habit.isArchived(),
                completedToday,
                currentStreak,
                bestStreak,
                habit.getCreatedOn().toString()
        );
    }

    private boolean isApplicable(Habit habit, LocalDate date) {
        if (habit.getFrequency() == Frequency.DAILY) {
            return true;
        }

        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }

    private LocalDate previousApplicable(Habit habit, LocalDate date) {
        LocalDate previous = date.minusDays(1);

        while (!isApplicable(habit, previous)) {
            previous = previous.minusDays(1);
        }

        return previous;
    }

    private int calculateCurrentStreak(Habit habit,
                                       Set<LocalDate> completed,
                                       LocalDate today) {

        if (!isApplicable(habit, today) || !completed.contains(today)) {
            return 0;
        }

        int streak = 1;
        LocalDate cursor = today;

        while (true) {
            LocalDate previous = previousApplicable(habit, cursor);

            if (completed.contains(previous)) {
                streak++;
                cursor = previous;
            } else {
                break;
            }
        }

        return streak;
    }

    private int calculateBestStreak(Habit habit, Set<LocalDate> completed) {
        if (completed.isEmpty()) {
            return 0;
        }

        List<LocalDate> dates = completed.stream()
                .filter(d -> isApplicable(habit, d))
                .sorted()
                .toList();

        if (dates.isEmpty()) {
            return 0;
        }

        int best = 1;
        int current = 1;

        for (int i = 1; i < dates.size(); i++) {
            LocalDate expectedPrevious =
                    previousApplicable(habit, dates.get(i));

            if (expectedPrevious.equals(dates.get(i - 1))) {
                current++;
                best = Math.max(best, current);
            } else {
                current = 1;
            }
        }

        return best;
    }

    public record HabitResponse(
            Long id,
            String name,
            String frequency,
            boolean archived,
            boolean completedToday,
            int currentStreak,
            int bestStreak,
            String createdOn
    ) {}

    public record DashboardResponse(
            int currentDay,
            int totalDays,
            int completedToday,
            int totalToday,
            int progressPercent
    ) {}
}