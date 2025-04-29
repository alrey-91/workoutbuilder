package com.csc435.workoutbuilder.model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

@Entity //new entity for workout_exercises join table
@Table(name= "workout_exercises")
public class WorkoutExercise {
    
    //composite primary key embedded from key class
    @EmbeddedId
    @JsonIgnore //dont include in response body
    private WorkoutExerciseKey id;

    @ManyToOne
    @MapsId("workoutId")
    @JoinColumn(name= "workout_id")
    @JsonIgnore 
    private Workout workout;

    @ManyToOne
    @MapsId("exerciseId")
    @JoinColumn(name= "exercise_id")
    private Exercise exercise;

    @JsonIgnore //hide sets and reps in json body of workoutExercises
    private Integer sets;
    @JsonIgnore
    private Integer reps;

    public WorkoutExercise() {}

    public WorkoutExercise(Workout workout, Exercise exercise, Integer sets, Integer reps) {
        this.workout = workout;
        this.exercise = exercise;
        this.sets = sets;
        this.reps = reps;
        this.id = new WorkoutExerciseKey(workout.getId(), exercise.getId());
    }

    public WorkoutExerciseKey getId() {
        return id;
    }

    public void setId(WorkoutExerciseKey id) {
        this.id = id;
    }

    public Workout getWorkout() {
        return workout;
    }

    public void setWorkout(Workout workout) {
        this.workout = workout;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    public Integer getSets() {
        return sets;
    }

    public void setSets(Integer sets) {
        this.sets = sets;
    }

    public Integer getReps() {
        return reps;
    }

    public void setReps(Integer reps) {
        this.reps = reps;
    }
    
}
