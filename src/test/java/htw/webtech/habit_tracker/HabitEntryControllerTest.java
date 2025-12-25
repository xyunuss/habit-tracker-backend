package htw.webtech.habit_tracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import htw.webtech.habit_tracker.model.Habit;
import htw.webtech.habit_tracker.model.HabitEntry;
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
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HabitEntryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private HabitEntryRepository entryRepository;

    private Habit testHabit;

    @BeforeEach
    void setUp() {
        entryRepository.deleteAll();
        habitRepository.deleteAll();

        // Create a test habit
        testHabit = new Habit();
        testHabit.setName("Test Habit");
        testHabit.setColor("blue");
        testHabit.setType(Habit.HabitType.DAILY);
        testHabit.setCreatedAt(LocalDate.now());
        testHabit = habitRepository.save(testHabit);
    }

    @Test
    void getEntriesForHabit_shouldReturnEmptyList_whenNoEntriesExist() throws Exception {
        mockMvc.perform(get("/api/entries/habit/" + testHabit.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getEntriesForHabit_shouldReturnEntries() throws Exception {
        // Given
        createEntry(testHabit, LocalDate.now(), true);
        createEntry(testHabit, LocalDate.now().minusDays(1), true);

        // When & Then
        mockMvc.perform(get("/api/entries/habit/" + testHabit.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getEntriesInRange_shouldReturnEntriesWithinDateRange() throws Exception {
        // Given
        LocalDate today = LocalDate.now();
        createEntry(testHabit, today, true);
        createEntry(testHabit, today.minusDays(1), true);
        createEntry(testHabit, today.minusDays(10), true); // Outside range

        String startDate = today.minusDays(5).toString();
        String endDate = today.toString();

        // When & Then
        mockMvc.perform(get("/api/entries/range")
                        .param("startDate", startDate)
                        .param("endDate", endDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void toggleEntry_shouldCreateNewEntry_whenNotExists() throws Exception {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("habitId", testHabit.getId());
        request.put("date", LocalDate.now().toString());

        // When & Then
        mockMvc.perform(post("/api/entries/toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed", is(true)))
                .andExpect(jsonPath("$.habitId", is(testHabit.getId().intValue())));
    }

    @Test
    void toggleEntry_shouldToggleExistingEntry() throws Exception {
        // Given
        LocalDate today = LocalDate.now();
        createEntry(testHabit, today, true);

        Map<String, Object> request = new HashMap<>();
        request.put("habitId", testHabit.getId());
        request.put("date", today.toString());

        // When & Then - Toggle from true to false
        mockMvc.perform(post("/api/entries/toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed", is(false)));

        // Toggle again from false to true
        mockMvc.perform(post("/api/entries/toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed", is(true)));
    }

    @Test
    void toggleEntry_shouldReturnBadRequest_whenHabitNotFound() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("habitId", 999L);
        request.put("date", LocalDate.now().toString());

        mockMvc.perform(post("/api/entries/toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Habit not found")));
    }

    @Test
    void setEntry_shouldSetCompletionStatus() throws Exception {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("habitId", testHabit.getId());
        request.put("date", LocalDate.now().toString());
        request.put("completed", true);

        // When & Then
        mockMvc.perform(post("/api/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed", is(true)));
    }

    @Test
    void getEntriesForDate_shouldReturnEntriesForSpecificDate() throws Exception {
        // Given
        LocalDate today = LocalDate.now();
        createEntry(testHabit, today, true);

        // Create another habit with entry
        Habit habit2 = new Habit();
        habit2.setName("Habit 2");
        habit2.setColor("green");
        habit2.setType(Habit.HabitType.DAILY);
        habit2.setCreatedAt(LocalDate.now());
        habit2 = habitRepository.save(habit2);
        createEntry(habit2, today, false);

        // When & Then
        mockMvc.perform(get("/api/entries/date/" + today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void deleteEntry_shouldDeleteEntry() throws Exception {
        // Given
        HabitEntry entry = createEntry(testHabit, LocalDate.now(), true);

        // When & Then
        mockMvc.perform(delete("/api/entries/" + entry.getId()))
                .andExpect(status().isOk());

        // Verify deletion
        mockMvc.perform(get("/api/entries/habit/" + testHabit.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deleteEntry_shouldReturn404_whenNotExists() throws Exception {
        mockMvc.perform(delete("/api/entries/999"))
                .andExpect(status().isNotFound());
    }

    private HabitEntry createEntry(Habit habit, LocalDate date, boolean completed) {
        HabitEntry entry = new HabitEntry(habit, date, completed);
        return entryRepository.save(entry);
    }
}

