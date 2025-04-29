package com.csc435.workoutbuilder.controllertest;
import com.csc435.workoutbuilder.controller.WorkoutController;
import com.csc435.workoutbuilder.repository.ExerciseRepository;
import com.csc435.workoutbuilder.repository.WorkoutExerciseRepo;
import com.csc435.workoutbuilder.repository.WorkoutRepository;
import com.csc435.workoutbuilder.model.Exercise;
import com.csc435.workoutbuilder.model.User;
import com.csc435.workoutbuilder.model.Workout;
import com.csc435.workoutbuilder.model.WorkoutExercise;
import com.csc435.workoutbuilder.model.WorkoutExerciseKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import java.util.*;

@WebMvcTest(WorkoutController.class)
public class WorkoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkoutRepository workoutRepo;

    @MockBean
    private WorkoutExerciseRepo weRepo;

    @MockBean
    private ExerciseRepository exerciseRepo;

    private MockHttpSession session;
    private User mockUser;
    private String oopsies = "youre not logged in silly";

    @BeforeEach
    void setup() {
        mockUser = new User("testuser","test");
        mockUser.setId(1);
        //mock logged in user session
        session = new MockHttpSession();
        session.setAttribute("currUser", mockUser);
    }
    //test authorization
    @Test
    public void test_GetWorkouts_Unauthorized() throws Exception {
        mockMvc.perform(get("/workouts"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value(oopsies))
            .andExpect(jsonPath("$.success").value(false));
    }
    @Test
    public void test_NewWorkout_Unauthorized() throws Exception {
        String requestjson = "{\"name\": \"workoutname\"}";
        mockMvc.perform(post("/workouts/new-workout")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestjson))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value(oopsies))
            .andExpect(jsonPath("$.success").value(false));
    }
    @Test
    public void test_DeleteWorkout_Unauthorized() throws Exception {
        mockMvc.perform(delete("/workouts/delete")
            .param("workoutId","1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value(oopsies))
            .andExpect(jsonPath("$.success").value(false));
    }
    @Test
    public void test_AddExercise_Unauthorized() throws Exception {
        //sample requestjson so doesnt return 400
        String requestjson = "{\"workoutId\": 1, \"exerciseId\": 2}";
        mockMvc.perform(put("/workouts/add-exercise")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestjson))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value(oopsies))
            .andExpect(jsonPath("$.success").value(false));
    }
    @Test
    public void test_RemoveExercise_Unauthorized() throws Exception {
        String requestjson = "{\"workoutId\": 1, \"exerciseId\": 2}";
        mockMvc.perform(delete("/workouts/remove-exercise")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestjson))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value(oopsies))
            .andExpect(jsonPath("$.success").value(false));
    }
    //test getting workouts along with exercises
    @Test
    public void test_GetAllWorkouts() throws Exception {
        Workout workout = new Workout();
        workout.setId(1);
        workout.setUserId(mockUser.getId());
        workout.setWorkoutName("testworkout1");
        Workout workout2 = new Workout();
        workout2.setId(2);
        workout2.setUserId(mockUser.getId());
        workout2.setWorkoutName("testworkout2");

        //workout1 will contain a exercise
        Exercise exercise = new Exercise("pushup", "chest", "strength","do pushup", null, null);
        exercise.setId(1);
        WorkoutExercise we = new WorkoutExercise();
        we.setId(new WorkoutExerciseKey(1,1));
        we.setWorkout(workout);
        we.setExercise(exercise);
        we.setSets(3);
        we.setReps(10);
        workout.setWorkoutExercises(List.of(we));

        when(workoutRepo.findByUserId(mockUser.getId())).thenReturn(List.of(workout, workout2));
        mockMvc.perform(get("/workouts").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.workouts[0].workoutName").value("testworkout1"))
            .andExpect(jsonPath("$.workouts[0].workoutExercises[0].exercise.name").value("pushup"))
            .andExpect(jsonPath("$.workouts[1].workoutName").value("testworkout2"));
    }
    @Test
    public void test_GetAllWorkouts_WithParam() throws Exception {
        Workout workout = new Workout();
        workout.setId(1);
        workout.setUserId(mockUser.getId());
        workout.setWorkoutName("testworkout1");

        Exercise exercise = new Exercise("pushup", "chest", "strength","do pushup", null, null);
        exercise.setId(1);
        WorkoutExercise we = new WorkoutExercise();
        we.setId(new WorkoutExerciseKey(1,1));
        we.setWorkout(workout);
        we.setExercise(exercise);
        we.setSets(3);
        we.setReps(10);
        workout.setWorkoutExercises(List.of(we));

        when(workoutRepo.findByIdAndUserId(workout.getId(), mockUser.getId())).thenReturn(Optional.of(workout));
        mockMvc.perform(get("/workouts").session(session)
            .param("workoutId","1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.workout.workoutExercises[0].exercise.name").value("pushup"))
            .andExpect(jsonPath("$.workout.workoutName").value("testworkout1"));
    }

    @Test
    public void test_GetWorkouts_IsEmpty() throws Exception {
        when(workoutRepo.findByUserId(mockUser.getId())).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/workouts").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("You have no workouts!"));
            
    }
    @Test
    public void test_GetOneWorkout_NotFound() throws Exception {
        when(workoutRepo.findByIdAndUserId(1,mockUser.getId())).thenReturn(Optional.empty());
        mockMvc.perform(get("/workouts").session(session)
            .param("workoutId","1"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("workout with id " + 1 + " not found"));
    }
    @Test
    public void test_GetWorkout_CatchesException() throws Exception {
        when(workoutRepo.findByIdAndUserId(1, mockUser.getId()))
            .thenThrow(new RuntimeException("mock exception"));

        mockMvc.perform(get("/workouts")
            .param("workoutId", "1").session(session))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("internal server error"));
    }

    //unit test /new-workout endpt
    @Test
    public void test_NewWorkout_Success() throws Exception {
        Workout newWorkout = new Workout(1, mockUser.getId(), "workoutname", null);
        when(workoutRepo.save(any(Workout.class))).thenReturn(newWorkout);

        String requestjson = "{\"name\": \"workoutname\"}";
        mockMvc.perform(post("/workouts/new-workout").session(session)
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestjson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("New workout called workoutname created. Add some exercises!"))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.workoutId").value(1));
    }
    @Test
    public void test_NewWorkout_CatchesException() throws Exception {
        when(workoutRepo.save(any(Workout.class))).thenThrow(new RuntimeException("mock error"));
         
        String requestjson = "{\"name\": \"workoutname\"}";
        mockMvc.perform(post("/workouts/new-workout").session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestjson))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("internal server error"));
    }
    @Test
    public void test_NewWorkout_BadRequest() throws Exception {
        String requestjson = "{\"name\": \"\"}"; //empty name
        mockMvc.perform(post("/workouts/new-workout").session(session)
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestjson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("workoutname field is required."))
            .andExpect(jsonPath("$.success").value(false));      
    }

    //tests for deleting workout
    //workout to be deleted will include workoutexercises
    @Test
    public void test_DeleteWorkout_Success() throws Exception {
        Workout workout = new Workout(1, mockUser.getId(), "testworkout", null);
        Exercise exercise = new Exercise("pushup", "chest", "strength","do pushup", null, null);
        exercise.setId(1);
        WorkoutExercise we = new WorkoutExercise();
        we.setId(new WorkoutExerciseKey(1,1));
        we.setWorkout(workout);
        we.setExercise(exercise);
        we.setSets(3);
        we.setReps(10);
        workout.setWorkoutExercises(List.of(we));

        when(workoutRepo.findByIdAndUserId(1, mockUser.getId())).thenReturn(Optional.of(workout));

        mockMvc.perform(delete("/workouts/delete")
            .param("workoutId", "1")
            .session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("workout testworkout deleted"))
            .andExpect(jsonPath("$.success").value(true));
        //verify it was deleted
        verify(workoutRepo, times(1)).delete(workout);
    }
    @Test
    public void test_DeleteWorkout_NotFound() throws Exception {

        when(workoutRepo.findByIdAndUserId(1, mockUser.getId())).thenReturn(Optional.empty());
        mockMvc.perform(delete("/workouts/delete")
        .param("workoutId", "1")
        .session(session))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("workout not found"))
        .andExpect(jsonPath("$.success").value(false));
    }
    @Test
    public void test_DeleteWorkout_CatchesException() throws Exception {
        when(workoutRepo.findByIdAndUserId(1, mockUser.getId())).thenThrow(new RuntimeException("mock error"));
         
        mockMvc.perform(delete("/workouts/delete").session(session)
            .contentType(MediaType.APPLICATION_JSON)
            .param("workoutId", "1"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("failed to delete workout"));
    }
    
    //test adding exercises
    @Test
    public void test_AddExercise_Success() throws Exception {
        Workout workout = new Workout(1, mockUser.getId(), "testworkout", null);
        Exercise exercise = new Exercise("pushup", "chest", "strength", "do pushup", null, null);
        exercise.setId(1);

        when(workoutRepo.findByIdAndUserId(1, mockUser.getId())).thenReturn(Optional.of(workout));
        when(exerciseRepo.findById(1)).thenReturn(Optional.of(exercise));
        when(weRepo.existsById(any())).thenReturn(false);

        String requestjson = "{\"workoutId\": 1, \"exerciseId\": 1}";

        mockMvc.perform(put("/workouts/add-exercise").session(session)
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestjson))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Exercise added to workout"));
    }
    @Test
    public void test_AddExercise_DoesNotExist() throws Exception {
        Workout workout = new Workout(1, mockUser.getId(), "testworkout", null);

        when(workoutRepo.findByIdAndUserId(1, mockUser.getId())).thenReturn(Optional.of(workout));

        String requestjson = "{\"workoutId\": 1, \"exerciseId\": 1}";

        mockMvc.perform(put("/workouts/add-exercise").session(session)
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestjson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("exercise with id 1 not found"));
    }
    @Test
    public void test_AddExercise_NoWorkout() throws Exception {
        Exercise exercise = new Exercise("pushup", "chest", "strength", "do pushup", null, null);
        exercise.setId(1);

        when(workoutRepo.findByIdAndUserId(1, mockUser.getId())).thenReturn(Optional.empty());
        when(exerciseRepo.findById(1)).thenReturn(Optional.of(exercise));

        String requestjson = "{\"workoutId\": 1, \"exerciseId\": 1}";

        mockMvc.perform(put("/workouts/add-exercise").session(session)
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestjson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("You do not own a workout with id 1"));
    }
    @Test   //exercise already in specified workout
    public void test_AddExercise_ExerciseExists() throws Exception {
        Workout workout = new Workout(1, mockUser.getId(), "testworkout", null);
        Exercise exercise = new Exercise("pushup", "chest", "strength", "do pushup", null, null);
        exercise.setId(1);

        when(workoutRepo.findByIdAndUserId(1, mockUser.getId())).thenReturn(Optional.of(workout));
        when(exerciseRepo.findById(1)).thenReturn(Optional.of(exercise));
        when(weRepo.existsById(any(WorkoutExerciseKey.class))).thenReturn(true);

        String requestjson = "{\"workoutId\": 1, \"exerciseId\": 1}";

        mockMvc.perform(put("/workouts/add-exercise").session(session)
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestjson))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("exercise already in workout!"));
    }
    @Test
    public void test_AddExercise_CatchesException() throws Exception {
        when(workoutRepo.findByIdAndUserId(1, mockUser.getId()))
        .thenThrow(new RuntimeException("mock error"));

        String requestjson = "{\"workoutId\": 1, \"exerciseId\": 1}";

        mockMvc.perform(put("/workouts/add-exercise")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestjson)
        .session(session))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.message").value("Unable to add exercise to workout"))
        .andExpect(jsonPath("$.success").value(false));
    }

    //unit tests for removing exercise
    @Test
    public void test_RemoveExercise_Success() throws Exception {
        Workout workout = new Workout(1, mockUser.getId(), "testworkout", null);

        when(workoutRepo.findByIdAndUserId(1, mockUser.getId())).thenReturn(Optional.of(workout));
        when(weRepo.existsById(any(WorkoutExerciseKey.class))).thenReturn(true);

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
    public void test_RemoveExercise_ExerciseNotFound() throws Exception {
        Workout workout = new Workout(1, mockUser.getId(), "testworkout", null);

        when(workoutRepo.findByIdAndUserId(1, mockUser.getId())).thenReturn(Optional.of(workout));
        when(weRepo.existsById(any(WorkoutExerciseKey.class))).thenReturn(false);

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
    public void test_RemoveExercise_WorkoutNotFound() throws Exception {

        when(workoutRepo.findByIdAndUserId(1, mockUser.getId())).thenReturn(Optional.empty());

        String requestjson = "{\"workoutId\": 1, \"exerciseId\": 1}";

        mockMvc.perform(delete("/workouts/remove-exercise")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestjson)
        .session(session))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("You do not own a workout with id 1"))
        .andExpect(jsonPath("$.success").value(false));
    }
    @Test
    public void test_RemoveExercise_CatchesException() throws Exception {
        when(workoutRepo.findByIdAndUserId(1, mockUser.getId()))
        .thenThrow(new RuntimeException("mock error"));

        String requestjson = "{\"workoutId\": 1, \"exerciseId\": 1}";

        mockMvc.perform(delete("/workouts/remove-exercise")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestjson)
        .session(session))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.message").value("could not remove exercise"))
        .andExpect(jsonPath("$.success").value(false));
    }
}