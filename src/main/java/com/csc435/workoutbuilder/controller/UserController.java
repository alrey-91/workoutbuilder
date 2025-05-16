package com.csc435.workoutbuilder.controller;
import com.csc435.workoutbuilder.model.User;
import com.csc435.workoutbuilder.repository.UserRepository;
import com.csc435.workoutbuilder.dto.UserCredentials;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import java.util.*;


@RestController
@RequestMapping("/")
@SessionAttributes()
public class UserController {

    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(UserRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("signup")
    public ResponseEntity<Map<String, Object>> register(@RequestBody UserCredentials creds) {
        Map<String, Object> response = new HashMap<>();
        logger.info("/signup attempt with username: '{}'", creds.getUsername());

        if (creds.getUsername() == null || creds.getUsername().trim().isEmpty() ||
            creds.getPassword() == null || creds.getPassword().trim().isEmpty()) {
            response.put("message", "Please enter a valid username and password");
            response.put("success", false);
            logger.warn("failed signup attempt: invalid username or password entered.");
        }
        else if (repo.existsByUsername(creds.getUsername())){
            response.put("message", "Username has been taken.");
            response.put("success", false);
            logger.warn("failed signup attempt: username '{}' already taken.", creds.getUsername());
        }
        else {
            String encodedPassword = passwordEncoder.encode(creds.getPassword());
            User newUser = repo.save(new User(creds.getUsername(), encodedPassword));
            response.put("message", "signup successful. Please login");
            response.put("success", true);
            response.put("userId", newUser.getId());
            logger.info("signup successful with username '{}'", creds.getUsername());
        }
        return ResponseEntity.ok(response);

    }
}