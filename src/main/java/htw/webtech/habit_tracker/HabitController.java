package htw.webtech.habit_tracker;

import htw.webtech.habit_tracker.model.Habit;
import htw.webtech.habit_tracker.repository.HabitEntryRepository;
import htw.webtech.habit_tracker.repository.HabitRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
    public Habit createHabit(@RequestBody Habit habit) {
        if (habit.getCreatedAt() == null) {
            habit.setCreatedAt(LocalDate.now());
        }
        if (habit.getType() == null) {
            habit.setType(Habit.HabitType.DAILY);
        }
        return habitRepository.save(habit);
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
