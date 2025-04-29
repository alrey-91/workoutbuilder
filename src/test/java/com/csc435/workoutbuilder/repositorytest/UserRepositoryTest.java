package com.csc435.workoutbuilder.repositorytest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Optional;
import com.csc435.workoutbuilder.model.User;
import com.csc435.workoutbuilder.repository.UserRepository;


@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class UserRepositoryTest {
   
    @Autowired
    private UserRepository userRepo;

    @Test
    public void test_FindByUsername() {
        User mockUser = new User("testuser", "test");
        userRepo.save(mockUser);

        Optional<User> result = userRepo.findByUsername(mockUser.getUsername());
        assertTrue(result.isPresent());
    }

    @Test
    public void test_existsByUsername() {
        User mockUser = new User("testuser", "test");
        userRepo.save(mockUser);

        boolean userExists = userRepo.existsByUsername(mockUser.getUsername());
        assertTrue(userExists);
    }
}
