package htw.webtech.habit_tracker.repository;

import htw.webtech.habit_tracker.model.HabitEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HabitEntryRepository extends JpaRepository<HabitEntry, Long> {
    
    List<HabitEntry> findByHabitId(Long habitId);
    
    List<HabitEntry> findByHabitIdAndDateBetween(Long habitId, LocalDate startDate, LocalDate endDate);
    
    Optional<HabitEntry> findByHabitIdAndDate(Long habitId, LocalDate date);
    
    List<HabitEntry> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<HabitEntry> findByDate(LocalDate date);
    
    void deleteByHabitId(Long habitId);
}

