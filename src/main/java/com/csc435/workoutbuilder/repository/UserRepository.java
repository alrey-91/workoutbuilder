package com.csc435.workoutbuilder.repository;
import com.csc435.workoutbuilder.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>{
    
    //check username exists alr in db
    boolean existsByUsername(String username);
    //find user by username
    Optional<User> findByUsername(String username);
}
