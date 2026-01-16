package htw.webtech.habit_tracker.service;

import htw.webtech.habit_tracker.model.Habit;
import htw.webtech.habit_tracker.repository.HabitEntryRepository;
import htw.webtech.habit_tracker.repository.HabitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class HabitService {

    private final HabitRepository habitRepository;
    private final HabitEntryRepository entryRepository;

    public HabitService(HabitRepository habitRepository, HabitEntryRepository entryRepository) {
        this.habitRepository = habitRepository;
        this.entryRepository = entryRepository;
    }

    /**
     * Get all habits
     */
    public List<Habit> getAllHabits() {
        return habitRepository.findAll();
    }

    /**
     * Get a single habit by ID
     */
    public Optional<Habit> getHabitById(Long id) {
        return habitRepository.findById(id);
    }

    /**
     * Create a new habit with default values
     */
    public Habit createHabit(Habit habit) {
        // Ensure id is null for new habits
        habit.setId(null);

        // Set default values if not provided
        if (habit.getCreatedAt() == null) {
            habit.setCreatedAt(LocalDate.now());
        }
        if (habit.getType() == null) {
            habit.setType(Habit.HabitType.DAILY);
        }

        return habitRepository.save(habit);
    }

    /**
     * Update an existing habit
     */
    public Optional<Habit> updateHabit(Long id, Habit habitDetails) {
        return habitRepository.findById(id)
                .map(existingHabit -> {
                    if (habitDetails.getName() != null) {
                        existingHabit.setName(habitDetails.getName());
                    }
                    if (habitDetails.getDescription() != null) {
                        existingHabit.setDescription(habitDetails.getDescription());
                    }
                    if (habitDetails.getColor() != null) {
                        existingHabit.setColor(habitDetails.getColor());
                    }
                    if (habitDetails.getIcon() != null) {
                        existingHabit.setIcon(habitDetails.getIcon());
                    }
                    if (habitDetails.getType() != null) {
                        existingHabit.setType(habitDetails.getType());
                    }
                    if (habitDetails.getTargetPerWeek() != null) {
                        existingHabit.setTargetPerWeek(habitDetails.getTargetPerWeek());
                    }
                    return habitRepository.save(existingHabit);
                });
    }

    /**
     * Delete a habit and all its entries
     */
    @Transactional
    public boolean deleteHabit(Long id) {
        if (!habitRepository.existsById(id)) {
            return false;
        }

        // Delete all entries for this habit first
        entryRepository.deleteByHabitId(id);

        // Then delete the habit
        habitRepository.deleteById(id);
        return true;
    }

    /**
     * Check if a habit exists
     */
    public boolean existsById(Long id) {
        return habitRepository.existsById(id);
    }

    /**
     * Validate that a habit has required fields
     */
    public boolean isValidHabit(Habit habit) {
        return habit.getName() != null && !habit.getName().trim().isEmpty();
    }
}
