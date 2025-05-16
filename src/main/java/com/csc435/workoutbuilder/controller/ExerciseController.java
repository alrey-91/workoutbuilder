package com.csc435.workoutbuilder.controller;
import com.csc435.workoutbuilder.service.FetchExercises;
import com.csc435.workoutbuilder.model.Exercise;
import com.csc435.workoutbuilder.repository.ExerciseRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/exercises")
public class ExerciseController {
    
    private final ExerciseRepository repo;
    private final FetchExercises fetchExercises;
    private final Logger logger = LoggerFactory.getLogger(ExerciseController.class);

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
        
        Predicate<Exercise> distinct = distinctByName();
        for (Exercise e : apiExercises) {
        //log any duplicates found in external api
            if (!distinct.test(e)) { 
                logger.debug("duplicate exercise found in external API response: '{}'", e.getName());
            }
        }
        //only insert exercises that arent in db
        List<Exercise> newExercises = apiExercises.stream().filter(distinctByName()).filter(e -> !repo.existsByName(e.getName())).toList();
        
        if (newExercises.isEmpty()) {
            logger.info("No exercises from external API saved - already exists in database or not found");
        } else {
            logger.info("Saved {} new exercises to database", newExercises.size());
            repo.saveAll(newExercises);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getExercises(@RequestParam(required = false) String muscle, @RequestParam(required = false) String type) {
        //store exercises
        apiExercisesToDb(muscle, type);
        List<Exercise> result;
        logger.info("Get /exercises called with muscle='{}', type='{}'", muscle, type);

        //if no param or param field is empty
        if ((muscle == null || muscle.isEmpty()) && (type == null || type.isEmpty())) {
            logger.debug("No params specified - returning all exercises");
            result = repo.findAll();
        }
        else if (muscle != null && !muscle.isEmpty() && type != null && !type.isEmpty()) {
            logger.debug("returning all exercises with params muscle='{}' and type='{}'",muscle,type);
            result = repo.findByMuscleAndType(muscle, type);
        }
        //only muscle provided
        else if (muscle != null && !muscle.isEmpty()) {
            logger.debug("returning all exercises with param muscle='{}'", muscle);
            result = repo.findByMuscle(muscle);
        
        } else {
        //only type provided
        logger.debug("returning all exercises with param type='{}'", type);
        result = repo.findByType(type);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("exercises", result);

        if (result.isEmpty()) {
            logger.info("No exercises found - Not in database or external API");
            response.put("message", "No exercises found");
            response.put("success", false);
        } else {
            logger.info("found {} exercises matching filters", result.size());
            response.put("success", true);
        }
        return ResponseEntity.ok(response);
    }
}
