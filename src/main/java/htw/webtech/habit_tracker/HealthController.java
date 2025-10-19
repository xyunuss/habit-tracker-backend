package htw.webtech.habit_tracker;

import htw.webtech.habit_tracker.model.Habit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HealthController {

    @GetMapping("/api/habits")
    public List<Habit> getHabits() {
        return List.of(
                new Habit("1", "Trinken", "blue", "2025-10-19"),
                new Habit("2", "Sport machen", "green", "2025-10-19"),
                new Habit("3", "Lesen", "orange", "2025-10-19")
        );
    }
}
