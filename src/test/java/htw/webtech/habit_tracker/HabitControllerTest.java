package htw.webtech.habit_tracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import htw.webtech.habit_tracker.model.Habit;
import htw.webtech.habit_tracker.repository.HabitRepository;
import htw.webtech.habit_tracker.repository.HabitEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HabitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private HabitEntryRepository entryRepository;

    @BeforeEach
    void setUp() {
        entryRepository.deleteAll();
        habitRepository.deleteAll();
    }

    @Test
    void getHabits_shouldReturnEmptyList_whenNoHabitsExist() throws Exception {
        mockMvc.perform(get("/api/habits"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getHabits_shouldReturnAllHabits() throws Exception {
        // Given
        Habit habit1 = createHabit("Sport", "blue");
        Habit habit2 = createHabit("Lesen", "green");
        habitRepository.save(habit1);
        habitRepository.save(habit2);

        // When & Then
        mockMvc.perform(get("/api/habits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Sport")))
                .andExpect(jsonPath("$[1].name", is("Lesen")));
    }

    @Test
    void getHabit_shouldReturnHabit_whenExists() throws Exception {
        // Given
        Habit habit = createHabit("Meditation", "purple");
        habit = habitRepository.save(habit);

        // When & Then
        mockMvc.perform(get("/api/habits/" + habit.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Meditation")))
                .andExpect(jsonPath("$.color", is("purple")));
    }

    @Test
    void getHabit_shouldReturn404_whenNotExists() throws Exception {
        mockMvc.perform(get("/api/habits/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createHabit_shouldCreateAndReturnHabit() throws Exception {
        // Given
        Habit habit = new Habit();
        habit.setName("Joggen");
        habit.setColor("orange");
        habit.setType(Habit.HabitType.DAILY);

        // When & Then
        mockMvc.perform(post("/api/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(habit)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Joggen")))
                .andExpect(jsonPath("$.color", is("orange")))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void createHabit_shouldReturnBadRequest_whenNameMissing() throws Exception {
        // Given
        Habit habit = new Habit();
        habit.setColor("blue");

        // When & Then
        mockMvc.perform(post("/api/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(habit)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Name")));
    }

    @Test
    void createHabit_shouldSetDefaultValues() throws Exception {
        // Given
        Habit habit = new Habit();
        habit.setName("Test Habit");

        // When & Then
        mockMvc.perform(post("/api/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(habit)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type", is("DAILY")))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    @Test
    void updateHabit_shouldUpdateAndReturnHabit() throws Exception {
        // Given
        Habit habit = createHabit("Alte Gewohnheit", "blue");
        habit = habitRepository.save(habit);

        Habit updateData = new Habit();
        updateData.setName("Neue Gewohnheit");
        updateData.setColor("green");

        // When & Then
        mockMvc.perform(put("/api/habits/" + habit.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Neue Gewohnheit")))
                .andExpect(jsonPath("$.color", is("green")));
    }

    @Test
    void updateHabit_shouldReturn404_whenNotExists() throws Exception {
        Habit updateData = new Habit();
        updateData.setName("Test");

        mockMvc.perform(put("/api/habits/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteHabit_shouldDeleteAndReturnOk() throws Exception {
        // Given
        Habit habit = createHabit("Zu löschen", "red");
        habit = habitRepository.save(habit);

        // When & Then
        mockMvc.perform(delete("/api/habits/" + habit.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("deleted")));

        // Verify deletion
        mockMvc.perform(get("/api/habits/" + habit.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteHabit_shouldReturn404_whenNotExists() throws Exception {
        mockMvc.perform(delete("/api/habits/999"))
                .andExpect(status().isNotFound());
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

