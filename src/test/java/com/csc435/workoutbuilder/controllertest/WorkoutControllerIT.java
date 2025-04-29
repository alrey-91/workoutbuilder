package com.csc435.workoutbuilder.controllertest;
import com.csc435.workoutbuilder.model.Exercise;
import com.csc435.workoutbuilder.model.User;
import com.csc435.workoutbuilder.repository.ExerciseRepository;
import com.csc435.workoutbuilder.repository.UserRepository;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class WorkoutControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExerciseRepository exerciseRepo;

    @Autowired
    private UserRepository userRepo;
    private User testUser;
    private MockHttpSession session;

    @BeforeEach
    void setup() {//save user once in db and persist the session
        Optional<User> existingUser = userRepo.findByUsername("testuser");
        if (existingUser.isEmpty()) {
            testUser = new User("testuser","testpass");
            userRepo.save(testUser);
        }
        else {
            testUser = existingUser.get();
        }
        session = new MockHttpSession();
        session.setAttribute("currUser", testUser);
    }
    /*
    integration test flow:(assuming user is in session)
    1. create new workout, view empty workout
    2. add exercises, try inserting same exercise
    3. delete exercises
    4. view workout with exercises
    5. delete workout, verify there is no workouts now for user
     */
    @Test
    @Order(1)
    public void test_NewWorkout_Success() throws Exception {

        String requestjson = "{\"name\": \"testworkout\"}";
        mockMvc.perform(post("/workouts/new-workout").session(session)
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestjson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("New workout called testworkout created. Add some exercises!"))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.workoutId").value(1));
    }
    @Test
    @Order(2)
    public void test_GetWorkouts_EmptyWorkout() throws Exception {
        mockMvc.perform(get("/workouts").session(session)
            .param("workoutId","1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.workout.workoutExercises.length()").value(0))
            .andExpect(jsonPath("$.workout.workoutName").value("testworkout"));
            
    }
    @Test
    @Order(3)
    public void test_AddExercise_Success() throws Exception {
        //create and store exercise in h2.
        Exercise ex = new Exercise("pushup", "arms", "strength", "do pushup", null, null);
        exerciseRepo.save(ex);

        String requestjson = "{\"workoutId\": 1, \"exerciseId\": 1}";

        mockMvc.perform(put("/workouts/add-exercise").session(session)
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestjson))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Exercise added to workout"));
    }
    @Test
    @Order(4)
    public void test_AddExercise_DoesNotExist() throws Exception {
        String requestjson = "{\"workoutId\": 1, \"exerciseId\": 2}";

        mockMvc.perform(put("/workouts/add-exercise").session(session)
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestjson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("exercise with id 2 not found"));
    }
    @Test
    @Order(5)
    public void test_AddExercise_NoWorkout() throws Exception {
        //no workout id 2
        String requestjson = "{\"workoutId\": 2, \"exerciseId\": 1}";

        mockMvc.perform(put("/workouts/add-exercise").session(session)
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestjson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("You do not own a workout with id 2"));
    }
    @Test
    @Order(6)
    public void test_AddExercise_ExerciseExists() throws Exception {
        //we already did this
        String requestjson = "{\"workoutId\": 1, \"exerciseId\": 1}";

        mockMvc.perform(put("/workouts/add-exercise").session(session)
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestjson))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("exercise already in workout!"));
    }
    //test removing exercise
    @Test
    @Order(7)
    public void test_RemoveExercise_Success() throws Exception {
        String requestjson = "{\"workoutId\": 1, \"exerciseId\": 1}";

        mockMvc.perform(delete("/workouts/remove-exercise")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestjson)
        .session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("exercise removed from workout"))
        .andExpect(jsonPath("$.success").value(true));
    }
    @Test
    @Order(8)
    public void test_RemoveExercise_ExerciseNotFound() throws Exception {
        //we already deleted exercise id 1
        String requestjson = "{\"workoutId\": 1, \"exerciseId\": 1}";

        mockMvc.perform(delete("/workouts/remove-exercise")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestjson)
        .session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Exercise not found in workout."))
        .andExpect(jsonPath("$.success").value(false));
    }
    @Test
    @Order(9)
    public void test_RemoveExercise_WorkoutNotFound() throws Exception {
        String requestjson = "{\"workoutId\": 2, \"exerciseId\": 1}";

        mockMvc.perform(delete("/workouts/remove-exercise")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestjson)
        .session(session))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("You do not own a workout with id 2"))
        .andExpect(jsonPath("$.success").value(false));
    }
    @Test
    @Order(10)
    public void test_DeleteWorkout_Success() throws Exception {

        mockMvc.perform(delete("/workouts/delete")
            .param("workoutId", "1")
            .session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("workout testworkout deleted"))
            .andExpect(jsonPath("$.success").value(true));
    }
    @Test
    @Order(11)
    public void test_DeleteWorkout_NotFound() throws Exception {
        //should pass since just deleted
        mockMvc.perform(delete("/workouts/delete")
        .param("workoutId", "1")
        .session(session))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("workout not found"))
        .andExpect(jsonPath("$.success").value(false));
    }
    @Test
    @Order(12)
    public void test_GetWorkouts_IsEmpty() throws Exception {
        
        mockMvc.perform(get("/workouts").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("You have no workouts!"));       
    }
    @Test
    @Order(13)
    public void test_GetOneWorkout_NotFound() throws Exception {
        //check again
        mockMvc.perform(get("/workouts").session(session)
        .param("workoutId","1"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("workout with id " + 1 + " not found"));
    }
}
