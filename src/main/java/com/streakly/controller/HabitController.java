package com.streakly.controller;

import com.streakly.dto.HabitRequest;
import com.streakly.service.HabitService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class HabitController {

    private final HabitService habitService;

    public HabitController(HabitService habitService) {
        this.habitService = habitService;
    }

    @GetMapping("/dashboard")
    public HabitService.DashboardResponse dashboard() {
        return habitService.getDashboard();
    }

    @GetMapping("/habits/today")
    public List<HabitService.HabitResponse> today() {
        return habitService.getTodayHabits();
    }

    @GetMapping("/habits")
    public List<HabitService.HabitResponse> all() {
        return habitService.getAllHabits();
    }

    @GetMapping("/habits/archived")
    public List<HabitService.HabitResponse> archived() {
        return habitService.getArchivedHabits();
    }

    @GetMapping("/habits/search")
    public List<HabitService.HabitResponse> search(@RequestParam(defaultValue = "") String q) {
        return habitService.search(q);
    }

    @PostMapping("/habits")
    public HabitService.HabitResponse add(@Valid @RequestBody HabitRequest request) {
        return habitService.addHabit(request);
    }

    @PostMapping("/habits/{id}/toggle")
    public HabitService.HabitResponse toggle(@PathVariable Long id) {
        return habitService.toggleCompletion(id, LocalDate.now());
    }

    @PostMapping("/habits/{id}/archive")
    public HabitService.HabitResponse archive(@PathVariable Long id) {
        return habitService.setArchived(id, true);
    }

    @PostMapping("/habits/{id}/restore")
    public HabitService.HabitResponse restore(@PathVariable Long id) {
        return habitService.setArchived(id, false);
    }
}
