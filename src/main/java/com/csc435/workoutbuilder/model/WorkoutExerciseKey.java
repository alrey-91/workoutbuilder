package com.csc435.workoutbuilder.model;
import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.*;

@Embeddable
public class WorkoutExerciseKey implements Serializable{
    
    @Column(name = "workout_id")
    private Integer workoutId;
    @Column(name = "exercise_id")
    private Integer exerciseId;

    protected WorkoutExerciseKey() {}

    public WorkoutExerciseKey(Integer workoutId, Integer exerciseId) {
        this.workoutId = workoutId;
        this.exerciseId = exerciseId;
    }

    public Integer getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(Integer workoutId) {
        this.workoutId = workoutId;
    }

    public Integer getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(Integer exerciseId) {
        this.exerciseId = exerciseId;
    }
    @Override
    public int hashCode() {
        return Objects.hash(workoutId, exerciseId);

    }
    //needed to compare key instances
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof WorkoutExerciseKey)) return false;
        WorkoutExerciseKey other = (WorkoutExerciseKey) obj;
        return Objects.equals(workoutId, other.workoutId) && Objects.equals(exerciseId, other.exerciseId);
    }
}
