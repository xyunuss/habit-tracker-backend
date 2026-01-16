package htw.webtech.habit_tracker;

import htw.webtech.habit_tracker.model.HabitEntry;
import htw.webtech.habit_tracker.service.HabitEntryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/entries")
public class HabitEntryController {

    private final HabitEntryService entryService;

    public HabitEntryController(HabitEntryService entryService) {
        this.entryService = entryService;
    }

    // Get all entries for a specific habit
    @GetMapping("/habit/{habitId}")
    public ResponseEntity<List<HabitEntryDTO>> getEntriesForHabit(@PathVariable Long habitId) {
        List<HabitEntry> entries = entryService.getEntriesForHabit(habitId);
        List<HabitEntryDTO> dtos = entries.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Get entries for a habit within a date range
    @GetMapping("/habit/{habitId}/range")
    public ResponseEntity<List<HabitEntryDTO>> getEntriesForHabitInRange(
            @PathVariable Long habitId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        List<HabitEntry> entries = entryService.getEntriesForHabitInRange(habitId, start, end);
        List<HabitEntryDTO> dtos = entries.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Get all entries for a specific date (all habits)
    @GetMapping("/date/{date}")
    public ResponseEntity<List<HabitEntryDTO>> getEntriesForDate(@PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date);
        List<HabitEntry> entries = entryService.getEntriesForDate(localDate);
        List<HabitEntryDTO> dtos = entries.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Get all entries within a date range (for dashboard/overview)
    @GetMapping("/range")
    public ResponseEntity<List<HabitEntryDTO>> getEntriesInRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        List<HabitEntry> entries = entryService.getEntriesInRange(start, end);
        List<HabitEntryDTO> dtos = entries.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Toggle a habit entry (check/uncheck)
    @PostMapping("/toggle")
    public ResponseEntity<?> toggleEntry(@RequestBody ToggleRequest request) {
        LocalDate date = LocalDate.parse(request.date);
        return entryService.toggleEntry(request.habitId, date)
                .<ResponseEntity<?>>map(entry -> ResponseEntity.ok(toDTO(entry)))
                .orElseGet(() -> ResponseEntity.badRequest().body(Map.of("error", "Habit not found")));
    }

    // Explicitly set completion status
    @PostMapping
    public ResponseEntity<?> setEntry(@RequestBody SetEntryRequest request) {
        LocalDate date = LocalDate.parse(request.date);
        return entryService.setEntry(request.habitId, date, request.completed)
                .<ResponseEntity<?>>map(entry -> ResponseEntity.ok(toDTO(entry)))
                .orElseGet(() -> ResponseEntity.badRequest().body(Map.of("error", "Habit not found")));
    }

    // Delete an entry
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEntry(@PathVariable Long id) {
        if (entryService.deleteEntry(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // DTO for response (avoids circular reference and lazy loading issues)
    private HabitEntryDTO toDTO(HabitEntry entry) {
        return new HabitEntryDTO(
                entry.getId(),
                entry.getHabit().getId(),
                entry.getDate().toString(),
                entry.isCompleted());
    }

    // DTOs
    public record HabitEntryDTO(Long id, Long habitId, String date, boolean completed) {
    }

    public static class ToggleRequest {
        public Long habitId;
        public String date;
    }

    public static class SetEntryRequest {
        public Long habitId;
        public String date;
        public boolean completed;
    }
}
