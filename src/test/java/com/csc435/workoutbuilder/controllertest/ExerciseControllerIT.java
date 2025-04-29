package com.csc435.workoutbuilder.controllertest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import com.csc435.workoutbuilder.repository.ExerciseRepository;
import java.util.List;
import com.csc435.workoutbuilder.model.Exercise;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ExerciseControllerIT {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExerciseRepository exerciseRepo;

    //exercise controller integration tests with 3 exercises
    //avoid using real external api, just insert fake exercises into h2 with fake params
    @Test
    @Order(1)
    public void test_GetExercises_WithMuscleAndType() throws Exception {
        Exercise ex1 = new Exercise("pushup", "arms", "strength", "do pushup", null, null);
        Exercise ex2 = new Exercise("pullup", "arms", "strength", "do pullup", null, null);
        Exercise ex3 = new Exercise("treadmill", "legs", "running", "run", null, null);
        exerciseRepo.saveAll(List.of(ex1, ex2, ex3));

        mockMvc.perform(get("/exercises")
            .param("muscle", "arms")
            .param("type", "strength")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.exercises.length()").value(2))
            .andExpect(jsonPath("$.exercises[0].name").value("pushup"))
            .andExpect(jsonPath("$.exercises[1].name").value("pullup"));
    }
    @Test
    @Order(2)
    public void test_GetExercises_WithMuscle() throws Exception {
        mockMvc.perform(get("/exercises")
            .param("muscle", "arms")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.exercises.length()").value(2))
            .andExpect(jsonPath("$.exercises[0].name").value("pushup"))
            .andExpect(jsonPath("$.exercises[1].name").value("pullup"));
    }
    @Test
    @Order(3)
    public void test_GetExercises_WithType() throws Exception {
        mockMvc.perform(get("/exercises")
            .param("type", "running")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.exercises.length()").value(1))
            .andExpect(jsonPath("$.exercises[0].name").value("treadmill"));
    }
    @Test
    @Order(4)
    public void test_GetExercises_All() throws Exception {
        mockMvc.perform(get("/exercises")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            //fetches 10 additional exercises from external api
            .andExpect(jsonPath("$.exercises.length()").value(13))
            .andExpect(jsonPath("$.exercises[0].name").value("pushup"))
            .andExpect(jsonPath("$.exercises[1].name").value("pullup"))
            .andExpect(jsonPath("$.exercises[2].name").value("treadmill"));
    }
    @Test
    @Order(5)
    public void test_GetExercises_EmptyResult() throws Exception {
        //fetch exercises doesnt return anything
        mockMvc.perform(get("/exercises")
            .param("muscle", "fakemuscle")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.exercises.length()").value(0))
            .andExpect(jsonPath("$.message").value("No exercises found"));
    }
}
