package htw.webtech.habit_tracker;

import htw.webtech.habit_tracker.model.Habit;
import htw.webtech.habit_tracker.repository.HabitEntryRepository;
import htw.webtech.habit_tracker.repository.HabitRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/habits")
public class HabitController {

    private final HabitRepository habitRepository;
    private final HabitEntryRepository entryRepository;

    public HabitController(HabitRepository habitRepository, HabitEntryRepository entryRepository) {
        this.habitRepository = habitRepository;
        this.entryRepository = entryRepository;
    }

    @GetMapping
    public List<Habit> getHabits() {
        return habitRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Habit> getHabit(@PathVariable Long id) {
        return habitRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createHabit(@RequestBody Habit habit) {
        try {
            // Ensure id is null for new habits (ignore if sent)
            habit.setId(null);
            
            // Validate required fields
            if (habit.getName() == null || habit.getName().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Name is required");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (habit.getCreatedAt() == null) {
                habit.setCreatedAt(LocalDate.now());
            }
            if (habit.getType() == null) {
                habit.setType(Habit.HabitType.DAILY);
            }
            
            Habit saved = habitRepository.save(habit);
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
        Optional<Habit> habitOpt = habitRepository.findById(id);
        if (habitOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Habit habit = habitOpt.get();
        
        if (habitDetails.getName() != null) {
            habit.setName(habitDetails.getName());
        }
        if (habitDetails.getDescription() != null) {
            habit.setDescription(habitDetails.getDescription());
        }
        if (habitDetails.getColor() != null) {
            habit.setColor(habitDetails.getColor());
        }
        if (habitDetails.getIcon() != null) {
            habit.setIcon(habitDetails.getIcon());
        }
        if (habitDetails.getType() != null) {
            habit.setType(habitDetails.getType());
        }
        if (habitDetails.getTargetPerWeek() != null) {
            habit.setTargetPerWeek(habitDetails.getTargetPerWeek());
        }

        Habit updatedHabit = habitRepository.save(habit);
        return ResponseEntity.ok(updatedHabit);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteHabit(@PathVariable Long id) {
        if (!habitRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        // Delete all entries for this habit first
        entryRepository.deleteByHabitId(id);
        
        // Then delete the habit
        habitRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Habit deleted successfully"));
    }
}
