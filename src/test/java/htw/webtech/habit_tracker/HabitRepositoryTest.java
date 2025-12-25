package htw.webtech.habit_tracker;

import htw.webtech.habit_tracker.model.Habit;
import htw.webtech.habit_tracker.repository.HabitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class HabitRepositoryTest {

    @Autowired
    private HabitRepository habitRepository;

    @BeforeEach
    void setUp() {
        habitRepository.deleteAll();
    }

    @Test
    void save_shouldPersistHabit() {
        // Given
        Habit habit = createHabit("Meditation", "purple");

        // When
        Habit saved = habitRepository.save(habit);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Meditation");
        assertThat(saved.getColor()).isEqualTo("purple");
    }

    @Test
    void findById_shouldReturnHabit_whenExists() {
        // Given
        Habit habit = habitRepository.save(createHabit("Sport", "blue"));

        // When
        Optional<Habit> found = habitRepository.findById(habit.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Sport");
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        // When
        Optional<Habit> found = habitRepository.findById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllHabits() {
        // Given
        habitRepository.save(createHabit("Habit 1", "blue"));
        habitRepository.save(createHabit("Habit 2", "green"));
        habitRepository.save(createHabit("Habit 3", "red"));

        // When
        List<Habit> habits = habitRepository.findAll();

        // Then
        assertThat(habits).hasSize(3);
    }

    @Test
    void delete_shouldRemoveHabit() {
        // Given
        Habit habit = habitRepository.save(createHabit("To Delete", "red"));
        Long id = habit.getId();

        // When
        habitRepository.deleteById(id);

        // Then
        assertThat(habitRepository.findById(id)).isEmpty();
    }

    @Test
    void existsById_shouldReturnTrue_whenExists() {
        // Given
        Habit habit = habitRepository.save(createHabit("Test", "blue"));

        // When & Then
        assertThat(habitRepository.existsById(habit.getId())).isTrue();
    }

    @Test
    void existsById_shouldReturnFalse_whenNotExists() {
        // When & Then
        assertThat(habitRepository.existsById(999L)).isFalse();
    }

    @Test
    void update_shouldModifyExistingHabit() {
        // Given
        Habit habit = habitRepository.save(createHabit("Original", "blue"));

        // When
        habit.setName("Updated");
        habit.setColor("green");
        Habit updated = habitRepository.save(habit);

        // Then
        assertThat(updated.getName()).isEqualTo("Updated");
        assertThat(updated.getColor()).isEqualTo("green");
    }

    private Habit createHabit(String name, String color) {
        Habit habit = new Habit();
        habit.setName(name);
        habit.setColor(color);
        habit.setType(Habit.HabitType.DAILY);
        habit.setCreatedAt(LocalDate.now());
        return habit;
    }
}

