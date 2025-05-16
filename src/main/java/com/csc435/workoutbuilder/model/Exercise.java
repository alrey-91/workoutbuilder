package com.csc435.workoutbuilder.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
@Table(name= "exercises")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable= false,unique= true)
    private String name;
    private String muscle;
    private String type;
    @Column(length= 3000)
    private String instructions;
    @Transient
    private Integer sets;
    @Transient
    private Integer reps;

    protected Exercise() {}

    public Exercise(String name, String muscle, String type, String instructions, Integer sets,
            Integer reps) {
        this.name = name;
        this.muscle = muscle;
        this.type = type;
        this.instructions = instructions;
        this.sets = sets;
        this.reps = reps;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMuscle() {
        return muscle;
    }

    public void setMuscle(String muscle) {
        this.muscle = muscle;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
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
