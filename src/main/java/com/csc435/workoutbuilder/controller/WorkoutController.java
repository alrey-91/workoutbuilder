package com.csc435.workoutbuilder.controller;
import com.csc435.workoutbuilder.model.Workout;
import com.csc435.workoutbuilder.config.CustomOAuth2User;
import com.csc435.workoutbuilder.model.Exercise;
import com.csc435.workoutbuilder.model.WorkoutExercise;
import com.csc435.workoutbuilder.model.WorkoutExerciseKey;
import com.csc435.workoutbuilder.model.User;
import com.csc435.workoutbuilder.repository.WorkoutExerciseRepo;
import com.csc435.workoutbuilder.repository.WorkoutRepository;
import com.csc435.workoutbuilder.repository.ExerciseRepository;
import com.csc435.workoutbuilder.repository.UserRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import jakarta.transaction.Transactional;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@RestController
@RequestMapping("/workouts")
public class WorkoutController {
    
    private final ExerciseRepository exerciseRepo;
    private final WorkoutRepository workoutRepo;
    private final WorkoutExerciseRepo workoutExerciseRepo;
    private final UserRepository userRepo;
    private final Logger logger = LoggerFactory.getLogger(WorkoutController.class);
    
    public WorkoutController(ExerciseRepository exerciseRepo, WorkoutRepository workoutRepo,
            WorkoutExerciseRepo workoutExerciseRepo, UserRepository userRepo) {
        this.exerciseRepo = exerciseRepo;
        this.workoutRepo = workoutRepo;
        this.workoutExerciseRepo = workoutExerciseRepo;
        this.userRepo = userRepo;
    }
    //new method for extracting user data in session
    private User extractAuthedUser(Object principal) {
        if (principal instanceof CustomOAuth2User oAuth2User) {
            return oAuth2User.getUser();
        } else if (principal instanceof UserDetails userDetails) {
            return userRepo.findByUsername(userDetails.getUsername()).orElse(null);
        }
        return null;
    }

    //get all workouts or just one if given workoutid param
    @GetMapping
    public ResponseEntity<Map<String, Object>> getWorkouts(@RequestParam(required = false) Integer workoutId, @AuthenticationPrincipal Object principal) {
        Map<String, Object> response = new HashMap<>();

        User user = extractAuthedUser(principal);
        
        logger.info("User session for username '{}' found - userId: {}", user.getUsername(), user.getId());

        try {
            if (workoutId != null) {
                logger.info("Get /workouts called with param workoutId={}", workoutId);
                Optional<Workout> workout = workoutRepo.findByIdAndUserId(workoutId, user.getId());
                if (workout.isPresent()) {
                    Workout w = workout.get();
                    //get each exercise and inject sets and reps to resp
                    if (w.getWorkoutExercises() != null) {
                        for (WorkoutExercise we : w.getWorkoutExercises()) {
                            Exercise e = we.getExercise();
                            logger.debug("exercise '{}', ex id={} - found in workoutId= {}", e.getName(), e.getId(), workoutId);
                            if (e != null) {
                                e.setSets(we.getSets()); 
                                e.setReps(we.getReps());
                            }
                        }
                    }
                    logger.info("workout '{}' found for userId={}: workoutId={}", w.getWorkoutName(), user.getId(), workoutId);
                    response.put("workout",  w);
                    response.put("success", true);
                } else {
                    logger.warn("workoutId {} not found for userId={}",workoutId, user.getId());
                    response.put("message", "workout with id " + workoutId + " not found");
                    response.put("success", false);
                    return ResponseEntity.status(404).body(response);
                }
            }
            else {
                logger.info("Get /workouts called with no workoutId param");
                List<Workout> workouts = workoutRepo.findByUserId(user.getId());
                if (workouts.isEmpty()) {
                    logger.info("user with userId={} has no workouts - lol what a loser", user.getId());
                    response.put("message", "You have no workouts!");
    
                } else {
                    for (Workout w : workouts) {
                        if (w.getWorkoutExercises() != null) {
                            for (WorkoutExercise we : w.getWorkoutExercises()) {
                                Exercise e = we.getExercise();
                                logger.debug("exercise '{}', ex id={} - found in workoutId= {}", e.getName(), e.getId(), w.getId());
                                if (e != null) {
                                    e.setSets(we.getSets()); 
                                    e.setReps(we.getReps());
                                }
                            }
                        }
                    }
                logger.info("{} workouts found for userId: {}", workouts.size(), user.getId());
                response.put("workouts", workouts);
                response.put("success", true);
                }
            }
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error getting workouts: ",e);
            response.put("message", "internal server error");
            response.put("success", false);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/new-workout")
    public ResponseEntity<Map<String, Object>> createNewWorkout(@RequestBody Map<String, Object> reqData, @AuthenticationPrincipal Object principal) {
        Map<String, Object> response = new HashMap<>();

        User user = extractAuthedUser(principal);

        logger.info("User session for '{}' found - userId: {}", user.getUsername(), user.getId());

        try {
            String workoutName = (String)reqData.get("name");
            logger.info("/new-workout called for userId={} - new workout request name: '{}'",user.getId(), workoutName);

            if (workoutName == null || workoutName.trim().isEmpty()) {
                logger.warn("Bad request for userId={} - invalid workout name", user.getId());
                response.put("message", "workoutname field is required.");
                response.put("success", false);
                return ResponseEntity.status(400).body(response);
            }
            Workout newWorkout = new Workout();
            newWorkout.setUserId(user.getId());
            newWorkout.setWorkoutName(workoutName);
            Workout savedWorkout = workoutRepo.save(newWorkout);
            logger.info("Workout '{}', workoutId={} - saved to database for userId={}", savedWorkout.getWorkoutName(), savedWorkout.getId(), user.getId());
            response.put("message", "New workout called " + workoutName + " created. Add some exercises!");
			response.put("workoutId", savedWorkout.getId());
			response.put("success", true);
            return ResponseEntity.ok(response);
        }
        catch(Exception e) {
            e.printStackTrace();
            logger.error("Error creating new workout: ",e);
            response.put("message", "internal server error");
            response.put("success", false);
            return ResponseEntity.status(500).body(response);
        }
    }

    @Transactional
    @DeleteMapping("/delete") 
    //bad request error if no param
    public ResponseEntity<Map<String, Object>> deleteWorkout(@RequestParam(required= true) Integer workoutId, @AuthenticationPrincipal Object principal) {
        Map<String, Object> response = new HashMap<>();
        logger.info("/workouts/delete called with param workoutId={}", workoutId);
        
        User user = extractAuthedUser(principal);
        
        logger.info("User session for '{}' found - userId: {}", user.getUsername(), user.getId());

    try {
        logger.info("workouts/delete called for userId={} - param workoutId={}", user.getId(), workoutId);
        Optional<Workout> workout = workoutRepo.findByIdAndUserId(workoutId, user.getId());
        if (workout.isPresent()) {
            workoutRepo.delete(workout.get());
            logger.info("workout '{}', workoutId={} - found and deleted successfully for userId={}", workout.get().getWorkoutName(), workout.get().getId(), user.getId());
            response.put("message", "workout " + workout.get().getWorkoutName() + " deleted");
            response.put("success", true);
            return ResponseEntity.ok(response);
        } 
        else {
            logger.warn("workout with id={} not found for userId={} - could not delete", workoutId, user.getId());
            response.put("message", "workout not found");
            response.put("success", false);
            return ResponseEntity.status(404).body(response);
        }
    } catch (Exception e) {
        e.printStackTrace();
            logger.error("Error deleting workout: ",e);
            response.put("message", "failed to delete workout");
            response.put("success", false);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/add-exercise")
    public ResponseEntity<Map<String, Object>> addExercise(@RequestBody Map<String, Object> reqData, @RequestParam(required= false) Integer sets, @RequestParam(required= false) Integer reps, @AuthenticationPrincipal Object principal) {
        Map<String, Object> response = new HashMap<>();
        logger.info("/workouts/add-exercise called with params sets={}, reps={}", sets,reps);
        
        User user = extractAuthedUser(principal);
        
        logger.info("User session for '{}' found - userId: {}", user.getUsername(), user.getId());

        try {
            Integer workoutId = (Integer)reqData.get("workoutId");
            Integer exerciseId = (Integer)reqData.get("exerciseId");
            logger.info("/add-exercise called for workoutId={}, exercise to add id={} - userId={}",workoutId,exerciseId, user.getId());
            Optional<Workout> workout = workoutRepo.findByIdAndUserId(workoutId, user.getId());
            Optional<Exercise> exercise = exerciseRepo.findById(exerciseId);

            if (exercise.isEmpty()) {
                logger.warn("exercise with id={} not found", exerciseId);
                response.put("message", "exercise with id " + exerciseId + " not found");
                response.put("success", false);
                return ResponseEntity.status(400).body(response);
            } 
            if (workout.isEmpty()) {
                logger.warn("workout with id={} not found for userId {}", exerciseId, user.getId());
                response.put("message", "You do not own a workout with id " + workoutId);
                response.put("success", false);
                return ResponseEntity.status(400).body(response);
            }

            WorkoutExerciseKey key = new WorkoutExerciseKey(workoutId, exerciseId);
            if (workoutExerciseRepo.existsById(key)) {
                logger.info("exercise with id={} already exists in workoutId={}",exerciseId, workoutId);
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
            logger.info("exercise '{}', ex id={} added to workoutId={} with sets={}, reps={}",
             we.getExercise().getName(), exerciseId, workoutId, setsFinal, repsFinal);

            response.put("message", "Exercise added to workout");
            response.put("success",true);
            return ResponseEntity.ok(response);
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Error adding exercise to workout: ",e);
            response.put("message", "Unable to add exercise to workout");
            response.put("success", false);
            return ResponseEntity.status(500).body(response);
        }
    }
    @DeleteMapping("/remove-exercise")
    public ResponseEntity<Map<String, Object>> removeExercise(@RequestBody Map<String, Object> reqData, @AuthenticationPrincipal Object principal) {
        Map<String, Object> response = new HashMap<>();
        
        User user = extractAuthedUser(principal);
        
        logger.info("User session for '{}' found - userId: {}", user.getUsername(), user.getId());

        try {
            Integer workoutId = (Integer)reqData.get("workoutId");
            Integer exerciseId = (Integer)reqData.get("exerciseId");
            WorkoutExerciseKey key = new WorkoutExerciseKey(workoutId, exerciseId);
            logger.info("/remove-exercise called for workoutId={}, exercise to remove id={} - userId={}",workoutId,exerciseId, user.getId());

            Optional<Workout> workout = workoutRepo.findByIdAndUserId(workoutId, user.getId());
            if (workout.isEmpty()) {
                logger.warn("workoutId {} not found for userId={}",workoutId, user.getId());
                response.put("message", "You do not own a workout with id " + workoutId);
                response.put("success", false);
                return ResponseEntity.status(400).body(response);
            }
            if (workoutExerciseRepo.existsById(key)) {

                workoutExerciseRepo.deleteById(key);
                logger.info("exercise '{}', ex id={} removed from workoutId={}", 
                exerciseRepo.findById(exerciseId).get().getName(), exerciseId, workoutId);
                response.put("message", "exercise removed from workout");
                response.put("success", true);
                return ResponseEntity.ok(response);
            } else {
                logger.info("exercise with id={} not found in workoutId={}", exerciseId, workoutId);
                response.put("message", "Exercise not found in workout.");
                response.put("success", false);
                return ResponseEntity.ok(response);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Error deleting exercise from workout: ",e);
            response.put("success",false);
            response.put("message","could not remove exercise");
            return ResponseEntity.status(500).body(response);
        }
    }
}
