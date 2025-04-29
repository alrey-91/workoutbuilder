package com.csc435.workoutbuilder.repository;
import com.csc435.workoutbuilder.model.WorkoutExercise;
import com.csc435.workoutbuilder.model.WorkoutExerciseKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkoutExerciseRepo extends JpaRepository<WorkoutExercise, WorkoutExerciseKey> {
    
    //get all exercises in workoutlist
    List<WorkoutExercise> findByWorkoutId(Integer workoutId);

    boolean existsById(WorkoutExerciseKey id);

    List<WorkoutExercise> deleteByWorkoutId(Integer workoutId);
}
