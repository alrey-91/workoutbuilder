package com.csc435.workoutbuilder.model;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name= "workouts")
public class Workout {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    
    @Column(name = "user_id", nullable= false)
    private Integer userId;

    private String workoutName;

    @OneToMany(mappedBy = "workout", cascade= CascadeType.ALL, orphanRemoval =true)
    private List<WorkoutExercise> workoutExercises = new ArrayList<>();

    public Workout() {}
    
    public Workout(Integer id, Integer userId, String workoutName, List<WorkoutExercise> workoutExercises) {
        this.id = id;
        this.userId = userId;
        this.workoutName = workoutName;
        this.workoutExercises = workoutExercises;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getWorkoutName() {
        return workoutName;
    }

    public void setWorkoutName(String workoutName) {
        this.workoutName = workoutName;
    }

    public List<WorkoutExercise> getWorkoutExercises() {
        return workoutExercises;
    }

    public void setWorkoutExercises(List<WorkoutExercise> workoutExercises) {
        this.workoutExercises = workoutExercises;
    }

    
}
