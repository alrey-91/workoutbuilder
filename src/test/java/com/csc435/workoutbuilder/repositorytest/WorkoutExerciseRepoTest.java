package com.csc435.workoutbuilder.repositorytest;
import com.csc435.workoutbuilder.model.WorkoutExercise;
import com.csc435.workoutbuilder.model.WorkoutExerciseKey;
import com.csc435.workoutbuilder.model.User;
import com.csc435.workoutbuilder.model.Exercise;
import com.csc435.workoutbuilder.model.Workout;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.*;
import com.csc435.workoutbuilder.repository.ExerciseRepository;
import com.csc435.workoutbuilder.repository.UserRepository;
import com.csc435.workoutbuilder.repository.WorkoutExerciseRepo;
import com.csc435.workoutbuilder.repository.WorkoutRepository;

@DataJpaTest
@AutoConfigureTestDatabase(connection= EmbeddedDatabaseConnection.H2)
public class WorkoutExerciseRepoTest {
    
    @Autowired
    private WorkoutExerciseRepo weRepo;

    @Autowired
    private ExerciseRepository exerciseRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private WorkoutRepository workoutRepo;

    @Test
    public void test_FindByWorkoutId() {
        User mockUser = new User("testuser","test123");
        userRepo.save(mockUser);

        Workout workout = new Workout(null, mockUser.getId(), "test workout", null);
        workoutRepo.save(workout);

        Exercise exercise = new Exercise("pushup", "arms", "strength", "do pushup", null, null);
        exerciseRepo.save(exercise);

        WorkoutExercise we = new WorkoutExercise(workout, exercise, 3, 10);
        WorkoutExerciseKey key = new WorkoutExerciseKey(workout.getId(), exercise.getId());
        we.setId(key);
        weRepo.save(we);

        List<WorkoutExercise> result = weRepo.findByWorkoutId(workout.getId());
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.get(0).getExercise().getName()).isEqualTo("pushup");
        Assertions.assertThat(result.get(0).getWorkout().getWorkoutName()).isEqualTo("test workout");
    }

    @Test
    public void test_ExistsById() {
        User mockUser = new User("testuser","test123");
        userRepo.save(mockUser);

        Workout workout = new Workout(null, mockUser.getId(), "test workout", null);
        workoutRepo.save(workout);

        Exercise exercise = new Exercise("pushup", "arms", "strength", "do pushup", null, null);
        exerciseRepo.save(exercise);

        WorkoutExercise we = new WorkoutExercise(workout, exercise, 3, 10);
        WorkoutExerciseKey key = new WorkoutExerciseKey(workout.getId(), exercise.getId());
        we.setId(key);
        weRepo.save(we);

        boolean doesExist = weRepo.existsById(we.getId());
        assertTrue(doesExist);
    }

    @Test
    public void test_DeleteByWorkoutId() {
        User mockUser = new User("testuser","test123");
        userRepo.save(mockUser);

        Workout workout = new Workout(null, mockUser.getId(), "test workout", null);
        workoutRepo.save(workout);

        Exercise exercise = new Exercise("pushup", "arms", "strength", "do pushup", null, null);
        exerciseRepo.save(exercise);

        WorkoutExercise we = new WorkoutExercise(workout, exercise, 3, 10);
        WorkoutExerciseKey key = new WorkoutExerciseKey(workout.getId(), exercise.getId());
        we.setId(key);
        weRepo.save(we);

        List<WorkoutExercise> beforeDelete = weRepo.findByWorkoutId(workout.getId());
        Assertions.assertThat(beforeDelete).isNotNull();

        List<WorkoutExercise> deleted = weRepo.deleteByWorkoutId(workout.getId());
        Assertions.assertThat(deleted).hasSize(1);
        Assertions.assertThat(weRepo.findByWorkoutId(workout.getId())).isEmpty();
    }
}


