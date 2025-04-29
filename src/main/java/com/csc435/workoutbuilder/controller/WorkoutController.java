package com.csc435.workoutbuilder.controller;
import com.csc435.workoutbuilder.model.Workout;
import com.csc435.workoutbuilder.model.Exercise;
import com.csc435.workoutbuilder.model.WorkoutExercise;
import com.csc435.workoutbuilder.model.WorkoutExerciseKey;
import com.csc435.workoutbuilder.model.User;
import com.csc435.workoutbuilder.repository.WorkoutExerciseRepo;
import com.csc435.workoutbuilder.repository.WorkoutRepository;
import com.csc435.workoutbuilder.repository.ExerciseRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import java.util.*;

@RestController
@RequestMapping("/workouts")
@SessionAttributes("currUser")
public class WorkoutController {
    
    private final ExerciseRepository exerciseRepo;
    private final WorkoutRepository workoutRepo;
    private final WorkoutExerciseRepo workoutExerciseRepo;
    
    public WorkoutController(ExerciseRepository exerciseRepo, WorkoutRepository workoutRepo,
            WorkoutExerciseRepo workoutExerciseRepo) {
        this.exerciseRepo = exerciseRepo;
        this.workoutRepo = workoutRepo;
        this.workoutExerciseRepo = workoutExerciseRepo;
    }

    //get all workouts or just one if given workoutid param
    @GetMapping
    public ResponseEntity<Map<String, Object>> getWorkouts(@RequestParam(required = false) Integer workoutId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        User user = (User)session.getAttribute("currUser");

        if (user == null) {
            response.put("message", "youre not logged in silly");
            response.put("success", false);
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            if (workoutId != null) {
                Optional<Workout> workout = workoutRepo.findByIdAndUserId(workoutId, user.getId());
                if (workout.isPresent()) {
                    Workout w = workout.get();
                    //get each exercise and inject sets and reps to resp
                    if (w.getWorkoutExercises() != null) {
                        for (WorkoutExercise we : w.getWorkoutExercises()) {
                            Exercise e = we.getExercise();
                            if (e != null) {
                                e.setSets(we.getSets()); 
                                e.setReps(we.getReps());
                            }
                        }
                    }
                    response.put("workout",  w);
                    response.put("success", true);
                } else {
                    response.put("message", "workout with id " + workoutId + " not found");
                    response.put("success", false);
                    return ResponseEntity.status(404).body(response);
                }
            }
            else {
                List<Workout> workouts = workoutRepo.findByUserId(user.getId());
                if (workouts.isEmpty()) {
                    response.put("message", "You have no workouts!");
    
                } else {
                    for (Workout w : workouts) {
                        if (w.getWorkoutExercises() != null) {
                            for (WorkoutExercise we : w.getWorkoutExercises()) {
                                Exercise e = we.getExercise();
                                if (e != null) {
                                    e.setSets(we.getSets()); 
                                    e.setReps(we.getReps());
                                }
                            }
                        }
                    }
                response.put("workouts", workouts);
                response.put("success", true);
                }
            }
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "internal server error");
            response.put("success", false);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/new-workout")
    public ResponseEntity<Map<String, Object>> createNewWorkout(@RequestBody Map<String, Object> reqData, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        User user = (User)session.getAttribute("currUser");
        if (user == null) {
            response.put("message", "youre not logged in silly");
            response.put("success", false);
            return ResponseEntity.status(401).body(response);
        }

        try {
            String workoutName = (String)reqData.get("name");

            if (workoutName == null || workoutName.trim().isEmpty()) {
                response.put("message", "workoutname field is required.");
                response.put("success", false);
                return ResponseEntity.status(400).body(response);
            }
            Workout newWorkout = new Workout();
            newWorkout.setUserId(user.getId());
            newWorkout.setWorkoutName(workoutName);
            Workout savedWorkout = workoutRepo.save(newWorkout);
            response.put("message", "New workout called " + workoutName + " created. Add some exercises!");
			response.put("workoutId", savedWorkout.getId());
			response.put("success", true);
            return ResponseEntity.ok(response);
        }
        catch(Exception e) {
            e.printStackTrace();
            response.put("message", "internal server error");
            response.put("success", false);
            return ResponseEntity.status(500).body(response);
        }
    }

    @Transactional
    @DeleteMapping("/delete") 
    //bad request error if no param
    public ResponseEntity<Map<String, Object>> deleteWorkout(@RequestParam(required= true) Integer workoutId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        User user = (User)session.getAttribute("currUser");
        if (user == null) {
            response.put("message", "youre not logged in silly");
            response.put("success", false);
            return ResponseEntity.status(401).body(response);
        }
    try {
        Optional<Workout> workout = workoutRepo.findByIdAndUserId(workoutId, user.getId());
        if (workout.isPresent()) {
            workoutRepo.delete(workout.get());
            response.put("message", "workout " + workout.get().getWorkoutName() + " deleted");
            response.put("success", true);
            return ResponseEntity.ok(response);
        } 
        else {
            response.put("message", "workout not found");
            response.put("success", false);
            return ResponseEntity.status(404).body(response);
        }
    } catch (Exception e) {
        e.printStackTrace();
            response.put("message", "failed to delete workout");
            response.put("success", false);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/add-exercise")
    public ResponseEntity<Map<String, Object>> addExercise(@RequestBody Map<String, Object> reqData, @RequestParam(required= false) Integer sets, @RequestParam(required= false) Integer reps, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        User user = (User)session.getAttribute("currUser");
        if (user == null) {
            response.put("message", "youre not logged in silly");
            response.put("success", false);
            return ResponseEntity.status(401).body(response);
        }

        try {
            Integer workoutId = (Integer)reqData.get("workoutId");
            Integer exerciseId = (Integer)reqData.get("exerciseId");
            
            Optional<Workout> workout = workoutRepo.findByIdAndUserId(workoutId, user.getId());
            Optional<Exercise> exercise = exerciseRepo.findById(exerciseId);

            if (exercise.isEmpty()) {
                response.put("message", "exercise with id " + exerciseId + " not found");
                response.put("success", false);
                return ResponseEntity.status(400).body(response);
            } 
            if (workout.isEmpty()) {
                response.put("message", "You do not own a workout with id " + workoutId);
                response.put("success", false);
                return ResponseEntity.status(400).body(response);
            }

            WorkoutExerciseKey key = new WorkoutExerciseKey(workoutId, exerciseId);
            if (workoutExerciseRepo.existsById(key)) {
                response.put("message", "exercise already in workout!");
                response.put("success", false);
                return ResponseEntity.ok(response);
            }
            //sets and reps. 3 and 10 are defaults if params are null
            Integer setsFinal = (sets != null) ? sets : 3;
            Integer repsFinal = (reps != null) ? reps : 10;

            //insert new ex/workout key in table
            WorkoutExercise we = new WorkoutExercise();
            we.setId(key);
            we.setWorkout(workout.get());
            we.setExercise(exercise.get());
            we.setSets(setsFinal);
            we.setReps(repsFinal);
            workoutExerciseRepo.save(we);

            response.put("message", "Exercise added to workout");
            response.put("success",true);
            return ResponseEntity.ok(response);
        }
        catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Unable to add exercise to workout");
            response.put("success", false);
            return ResponseEntity.status(500).body(response);
        }
    }
    @DeleteMapping("/remove-exercise")
    public ResponseEntity<Map<String, Object>> removeExercise(@RequestBody Map<String, Object> reqData, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        User user = (User)session.getAttribute("currUser");
        if (user == null) {
            response.put("message", "youre not logged in silly");
            response.put("success", false);
            return ResponseEntity.status(401).body(response);
        }
        try {
            Integer workoutId = (Integer)reqData.get("workoutId");
            Integer exerciseId = (Integer)reqData.get("exerciseId");
            WorkoutExerciseKey key = new WorkoutExerciseKey(workoutId, exerciseId);

            Optional<Workout> workout = workoutRepo.findByIdAndUserId(workoutId, user.getId());
            if (workout.isEmpty()) {
                response.put("message", "You do not own a workout with id " + workoutId);
                response.put("success", false);
                return ResponseEntity.status(400).body(response);
            }
            if (workoutExerciseRepo.existsById(key)) {

                workoutExerciseRepo.deleteById(key);
                response.put("message", "exercise removed from workout");
                response.put("success", true);
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Exercise not found in workout.");
                response.put("success", false);
                return ResponseEntity.ok(response);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            response.put("success",false);
            response.put("message","could not remove exercise");
            return ResponseEntity.status(500).body(response);
        }
    }
}
