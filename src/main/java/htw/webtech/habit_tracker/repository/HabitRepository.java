package htw.webtech.habit_tracker.repository;

import htw.webtech.habit_tracker.model.Habit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HabitRepository extends JpaRepository<Habit, Long> {
}
