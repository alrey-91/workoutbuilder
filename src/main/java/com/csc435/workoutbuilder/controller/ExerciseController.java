package com.csc435.workoutbuilder.controller;
import com.csc435.workoutbuilder.service.FetchExercises;
import com.csc435.workoutbuilder.model.Exercise;
import com.csc435.workoutbuilder.repository.ExerciseRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

@RestController
@RequestMapping("/exercises")
public class ExerciseController {
    
    private final ExerciseRepository repo;
    private final FetchExercises fetchExercises;

    public ExerciseController(FetchExercises fetchExercises, ExerciseRepository repo) {
        this.fetchExercises = fetchExercises;
        this.repo = repo;
    }
    //helper function since external api sometimes serves duplicate exercise names per request
    public static Predicate<Exercise> distinctByName() {
    Set<String> seen = ConcurrentHashMap.newKeySet(); //no dupes
    return e -> seen.add(e.getName());
    }

    private void apiExercisesToDb(String muscle, String type) {
        List<Exercise> apiExercises = fetchExercises.fetch(muscle, type);
        //only insert exercises that arent in db
        List<Exercise> newExercises = apiExercises.stream().filter(distinctByName()).filter(e -> !repo.existsByName(e.getName())).toList();
        repo.saveAll(newExercises);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getExercises(@RequestParam(required = false) String muscle, @RequestParam(required = false) String type) {
        //store exercises
        apiExercisesToDb(muscle, type);
        List<Exercise> result;

        //if no param or param field is empty
        if ((muscle == null || muscle.isEmpty()) && (type == null || type.isEmpty())) {
            result = repo.findAll();
        }
        else if (muscle != null && !muscle.isEmpty() && type != null && !type.isEmpty()) {
            result = repo.findByMuscleAndType(muscle, type);
        }
        //only muscle provided
        else if (muscle != null && !muscle.isEmpty()) {
            result = repo.findByMuscle(muscle);
        
        } else {
        //only type provided
        result = repo.findByType(type);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("exercises", result);

        if (result.isEmpty()) {
            response.put("message", "No exercises found");
            response.put("success", false);
        } else {
            response.put("success", true);
        }
        return ResponseEntity.ok(response);
    }
}
