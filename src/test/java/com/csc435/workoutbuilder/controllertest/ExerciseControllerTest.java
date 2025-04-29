package com.csc435.workoutbuilder.controllertest;
import com.csc435.workoutbuilder.controller.ExerciseController;
import com.csc435.workoutbuilder.service.FetchExercises;
import com.csc435.workoutbuilder.repository.ExerciseRepository;
import com.csc435.workoutbuilder.model.Exercise;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import java.util.*;

@WebMvcTest(ExerciseController.class)
public class ExerciseControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FetchExercises fetchExercises;

    @MockBean
    private ExerciseRepository exerciseRepo;

    @Test
    public void test_GetExercises_BothParams_ReturnsList() throws Exception {
        Exercise ex1 = new Exercise("pushup", "arms", "strength", "do pushup", null, null);
        Exercise ex2 = new Exercise("pullup", "arms", "strength", "do pullup", null, null);

        when(fetchExercises.fetch("arms","strength")).thenReturn(List.of(ex1,ex2));
        //exercises are new
        when(exerciseRepo.existsByName(anyString())).thenReturn(false);
        when(exerciseRepo.findByMuscleAndType("arms", "strength")).thenReturn(List.of(ex1, ex2));

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
    public void test_GetExercises_NoParams_ReturnsAll() throws Exception {
        Exercise ex1 = new Exercise("pushup", "arms", "strength", "do pushup", null, null);
        Exercise ex2 = new Exercise("squat", "legs", "strength", "do it", null, null);

        when(fetchExercises.fetch(null,null)).thenReturn(List.of(ex1,ex2));
       
        when(exerciseRepo.existsByName(anyString())).thenReturn(false);
        when(exerciseRepo.findAll()).thenReturn(List.of(ex1, ex2));

        mockMvc.perform(get("/exercises")
            
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.exercises.length()").value(2))
            .andExpect(jsonPath("$.exercises[0].name").value("pushup"))
            .andExpect(jsonPath("$.exercises[1].name").value("squat"));
    }

    @Test
    public void test_GetExercises_MuscleParam_ReturnsList() throws Exception {
        Exercise ex1 = new Exercise("pushup", "arms", "strength", "do pushup", null, null);
        Exercise ex2 = new Exercise("shrugs", "traps", "strength", "do shrugs", null, null);

        when(fetchExercises.fetch("traps",null)).thenReturn(List.of(ex1,ex2));
        
        when(exerciseRepo.existsByName(anyString())).thenReturn(false);
        when(exerciseRepo.findByMuscle("traps")).thenReturn(List.of(ex2));

        mockMvc.perform(get("/exercises")
            .param("muscle", "traps")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.exercises.length()").value(1))
            .andExpect(jsonPath("$.exercises[0].name").value("shrugs"));
    }

    @Test
    public void test_GetExercises_TypeParam_ReturnsList() throws Exception {
        Exercise ex1 = new Exercise("dumbell fly", "arms", "bodybuild", "bodybuilder", null, null);
        Exercise ex2 = new Exercise("shrugs", "traps", "strength", "do shrugs", null, null);

        when(fetchExercises.fetch(null,"bodybuild")).thenReturn(List.of(ex1,ex2));
        
        when(exerciseRepo.existsByName(anyString())).thenReturn(false);
        when(exerciseRepo.findByType("bodybuild")).thenReturn(List.of(ex1));

        mockMvc.perform(get("/exercises")
            .param("type", "bodybuild")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.exercises.length()").value(1))
            .andExpect(jsonPath("$.exercises[0].name").value("dumbell fly"));
    }

    @Test
    public void test_GetExercises_EmptyResult() throws Exception {
        //fetch exercises doesnt return anything
        when(fetchExercises.fetch("fakemuscle","faketype")).thenReturn(Collections.emptyList());
        when(exerciseRepo.existsByName(anyString())).thenReturn(false);
        when(exerciseRepo.findByMuscleAndType("fakemuscle","faketype")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/exercises")
            .param("muscle", "fakemuscle")
            .param("type", "faketype")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.exercises.length()").value(0))
            .andExpect(jsonPath("$.message").value("No exercises found"));
    }
}
