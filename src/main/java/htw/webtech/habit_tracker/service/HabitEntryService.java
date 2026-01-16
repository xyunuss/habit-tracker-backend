package htw.webtech.habit_tracker.service;

import htw.webtech.habit_tracker.model.Habit;
import htw.webtech.habit_tracker.model.HabitEntry;
import htw.webtech.habit_tracker.repository.HabitEntryRepository;
import htw.webtech.habit_tracker.repository.HabitRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class HabitEntryService {

    private final HabitEntryRepository entryRepository;
    private final HabitRepository habitRepository;

    public HabitEntryService(HabitEntryRepository entryRepository, HabitRepository habitRepository) {
        this.entryRepository = entryRepository;
        this.habitRepository = habitRepository;
    }

    /**
     * Get all entries for a specific habit
     */
    public List<HabitEntry> getEntriesForHabit(Long habitId) {
        return entryRepository.findByHabitId(habitId);
    }

    /**
     * Get entries for a habit within a date range
     */
    public List<HabitEntry> getEntriesForHabitInRange(Long habitId, LocalDate startDate, LocalDate endDate) {
        return entryRepository.findByHabitIdAndDateBetween(habitId, startDate, endDate);
    }

    /**
     * Get all entries for a specific date (all habits)
     */
    public List<HabitEntry> getEntriesForDate(LocalDate date) {
        return entryRepository.findByDate(date);
    }

    /**
     * Get all entries within a date range (for dashboard/overview)
     */
    public List<HabitEntry> getEntriesInRange(LocalDate startDate, LocalDate endDate) {
        return entryRepository.findByDateBetween(startDate, endDate);
    }

    /**
     * Toggle a habit entry (check/uncheck)
     * 
     * @return the updated/created entry, or empty if habit not found
     */
    public Optional<HabitEntry> toggleEntry(Long habitId, LocalDate date) {
        Optional<Habit> habitOpt = habitRepository.findById(habitId);
        if (habitOpt.isEmpty()) {
            return Optional.empty();
        }

        Optional<HabitEntry> existingEntry = entryRepository.findByHabitIdAndDate(habitId, date);

        if (existingEntry.isPresent()) {
            // Toggle existing entry
            HabitEntry entry = existingEntry.get();
            entry.setCompleted(!entry.isCompleted());
            return Optional.of(entryRepository.save(entry));
        } else {
            // Create new entry as completed
            HabitEntry newEntry = new HabitEntry(habitOpt.get(), date, true);
            return Optional.of(entryRepository.save(newEntry));
        }
    }

    /**
     * Explicitly set completion status for an entry
     * 
     * @return the updated/created entry, or empty if habit not found
     */
    public Optional<HabitEntry> setEntry(Long habitId, LocalDate date, boolean completed) {
        Optional<Habit> habitOpt = habitRepository.findById(habitId);
        if (habitOpt.isEmpty()) {
            return Optional.empty();
        }

        Optional<HabitEntry> existingEntry = entryRepository.findByHabitIdAndDate(habitId, date);

        HabitEntry entry;
        if (existingEntry.isPresent()) {
            entry = existingEntry.get();
            entry.setCompleted(completed);
        } else {
            entry = new HabitEntry(habitOpt.get(), date, completed);
        }

        return Optional.of(entryRepository.save(entry));
    }

    /**
     * Delete an entry by ID
     * 
     * @return true if deleted, false if not found
     */
    public boolean deleteEntry(Long id) {
        if (!entryRepository.existsById(id)) {
            return false;
        }
        entryRepository.deleteById(id);
        return true;
    }

    /**
     * Check if a habit exists
     */
    public boolean habitExists(Long habitId) {
        return habitRepository.existsById(habitId);
    }
}
