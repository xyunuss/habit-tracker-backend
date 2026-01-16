package htw.webtech.habit_tracker;

import htw.webtech.habit_tracker.model.Habit;
import htw.webtech.habit_tracker.service.HabitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/habits")
public class HabitController {

    private final HabitService habitService;

    public HabitController(HabitService habitService) {
        this.habitService = habitService;
    }

    @GetMapping
    public List<Habit> getHabits() {
        return habitService.getAllHabits();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Habit> getHabit(@PathVariable Long id) {
        return habitService.getHabitById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createHabit(@Valid @RequestBody Habit habit) {
        try {
            // Validate required fields
            if (!habitService.isValidHabit(habit)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Name is required");
                return ResponseEntity.badRequest().body(error);
            }

            Habit saved = habitService.createHabit(habit);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            System.err.println("Error creating habit: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create habit: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateHabit(@PathVariable Long id, @RequestBody Habit habitDetails) {
        return habitService.updateHabit(id, habitDetails)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHabit(@PathVariable Long id) {
        if (habitService.deleteHabit(id)) {
            return ResponseEntity.ok(Map.of("message", "Habit deleted successfully"));
        }
        return ResponseEntity.notFound().build();
    }
}
