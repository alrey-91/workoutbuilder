package com.csc435.workoutbuilder.repository;
import com.csc435.workoutbuilder.model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Integer> {
    
    //check for duplicates method
    boolean existsByName(String name);
    //finding by params
    List<Exercise> findByMuscle(String muscle);
    List<Exercise> findByType(String type);
    List<Exercise> findByMuscleAndType(String muscle, String type);
}
