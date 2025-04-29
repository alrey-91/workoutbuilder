package com.csc435.workoutbuilder.repositorytest;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.List;

import com.csc435.workoutbuilder.model.Exercise;
import com.csc435.workoutbuilder.repository.ExerciseRepository;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class ExerciseRepositoryTest {
    @Autowired
    private ExerciseRepository exerciseRepo;

    @Test
    public void test_ExistsByName() {
        Exercise exercise = new Exercise("pushup", "arms", null, null, null, null);
        exerciseRepo.save(exercise);

        boolean exerciseExists = exerciseRepo.existsByName("pushup");
        assertTrue(exerciseExists);
    }

    @Test
    public void test_FindByMuscle() {
        Exercise exercise = new Exercise("pushup", "arms", null, null, null, null);
        exerciseRepo.save(exercise);

        List<Exercise> foundExercises = exerciseRepo.findByMuscle(exercise.getMuscle());
        Assertions.assertThat(foundExercises).isNotEmpty().extracting(Exercise::getMuscle).contains("arms");
    }

    @Test
    public void test_FindByType() {
        Exercise exercise = new Exercise("pushup", "arms", "strength", null, null, null);
        exerciseRepo.save(exercise);

        List<Exercise> foundExercises = exerciseRepo.findByType(exercise.getType());
        Assertions.assertThat(foundExercises).isNotEmpty().extracting(Exercise::getType).contains("strength");
    }

    @Test
    public void test_FindByMuscleAndType() {
        Exercise exercise = new Exercise("pushup", "arms", "strength", null, null, null);
        exerciseRepo.save(exercise);
        List<Exercise> foundExercises = exerciseRepo.findByMuscleAndType(exercise.getMuscle(), exercise.getType());
        Assertions.assertThat(foundExercises).isNotEmpty().extracting(Exercise::getMuscle).contains("arms");
        Assertions.assertThat(foundExercises).isNotEmpty().extracting(Exercise::getType).contains("strength");
    }
}
