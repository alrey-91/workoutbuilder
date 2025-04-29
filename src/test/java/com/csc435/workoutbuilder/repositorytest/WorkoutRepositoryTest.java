package com.csc435.workoutbuilder.repositorytest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.assertj.core.api.Assertions;
import java.util.Optional;
import java.util.List;
import com.csc435.workoutbuilder.model.User;
import com.csc435.workoutbuilder.model.Workout;
import com.csc435.workoutbuilder.repository.UserRepository;
import com.csc435.workoutbuilder.repository.WorkoutRepository;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class WorkoutRepositoryTest {
    
    @Autowired
    private WorkoutRepository workoutRepo;

    @Autowired
    private UserRepository userRepo;

    @Test
    public void test_FindByUserId() {
        User mockUser = new User("testuser", "test123");
        userRepo.save(mockUser);
        Workout workout = new Workout(null, mockUser.getId(), "testworkout", null);
        workoutRepo.save(workout);

        List<Workout> result = workoutRepo.findByUserId(mockUser.getId());
        Assertions.assertThat(result).extracting(Workout::getUserId).contains(mockUser.getId());
    }

    @Test
    public void test_FindByIdAndUserId() {
        User mockUser = new User("testuser", "test123");
        userRepo.save(mockUser);
        Workout workout = new Workout(null, mockUser.getId(), "testworkout", null);
        workoutRepo.save(workout);

        Optional<Workout> result = workoutRepo.findByIdAndUserId(workout.getId(), mockUser.getId());
        assertTrue(result.isPresent());
        
    }
}
