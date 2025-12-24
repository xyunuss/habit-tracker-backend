package htw.webtech.habit_tracker;

import htw.webtech.habit_tracker.model.Habit;
import htw.webtech.habit_tracker.model.HabitEntry;
import htw.webtech.habit_tracker.repository.HabitEntryRepository;
import htw.webtech.habit_tracker.repository.HabitRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/entries")
public class HabitEntryController {

    private final HabitEntryRepository entryRepository;
    private final HabitRepository habitRepository;

    public HabitEntryController(HabitEntryRepository entryRepository, HabitRepository habitRepository) {
        this.entryRepository = entryRepository;
        this.habitRepository = habitRepository;
    }

    // Get all entries for a specific habit
    @GetMapping("/habit/{habitId}")
    public ResponseEntity<List<HabitEntryDTO>> getEntriesForHabit(@PathVariable Long habitId) {
        List<HabitEntry> entries = entryRepository.findByHabitId(habitId);
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
        List<HabitEntry> entries = entryRepository.findByHabitIdAndDateBetween(habitId, start, end);
        List<HabitEntryDTO> dtos = entries.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Get all entries for a specific date (all habits)
    @GetMapping("/date/{date}")
    public ResponseEntity<List<HabitEntryDTO>> getEntriesForDate(@PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date);
        List<HabitEntry> entries = entryRepository.findByDate(localDate);
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
        List<HabitEntry> entries = entryRepository.findByDateBetween(start, end);
        List<HabitEntryDTO> dtos = entries.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Toggle a habit entry (check/uncheck)
    @PostMapping("/toggle")
    public ResponseEntity<?> toggleEntry(@RequestBody ToggleRequest request) {
        Optional<Habit> habitOpt = habitRepository.findById(request.habitId);
        if (habitOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Habit not found"));
        }

        LocalDate date = LocalDate.parse(request.date);
        Optional<HabitEntry> existingEntry = entryRepository.findByHabitIdAndDate(request.habitId, date);

        if (existingEntry.isPresent()) {
            // Toggle existing entry
            HabitEntry entry = existingEntry.get();
            entry.setCompleted(!entry.isCompleted());
            HabitEntry saved = entryRepository.save(entry);
            return ResponseEntity.ok(toDTO(saved));
        } else {
            // Create new entry as completed
            HabitEntry newEntry = new HabitEntry(habitOpt.get(), date, true);
            HabitEntry saved = entryRepository.save(newEntry);
            return ResponseEntity.ok(toDTO(saved));
        }
    }

    // Explicitly set completion status
    @PostMapping
    public ResponseEntity<?> setEntry(@RequestBody SetEntryRequest request) {
        Optional<Habit> habitOpt = habitRepository.findById(request.habitId);
        if (habitOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Habit not found"));
        }

        LocalDate date = LocalDate.parse(request.date);
        Optional<HabitEntry> existingEntry = entryRepository.findByHabitIdAndDate(request.habitId, date);

        HabitEntry entry;
        if (existingEntry.isPresent()) {
            entry = existingEntry.get();
            entry.setCompleted(request.completed);
        } else {
            entry = new HabitEntry(habitOpt.get(), date, request.completed);
        }

        HabitEntry saved = entryRepository.save(entry);
        return ResponseEntity.ok(toDTO(saved));
    }

    // Delete an entry
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEntry(@PathVariable Long id) {
        if (!entryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        entryRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // DTO for response (avoids circular reference and lazy loading issues)
    private HabitEntryDTO toDTO(HabitEntry entry) {
        return new HabitEntryDTO(
                entry.getId(),
                entry.getHabit().getId(),
                entry.getDate().toString(),
                entry.isCompleted()
        );
    }

    // DTOs
    public record HabitEntryDTO(Long id, Long habitId, String date, boolean completed) {}
    
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

