package com.csc435.workoutbuilder.controller;
import com.csc435.workoutbuilder.model.User;
import com.csc435.workoutbuilder.repository.UserRepository;
import com.csc435.workoutbuilder.dto.UserCredentials;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequestMapping("/")
@SessionAttributes()
public class UserController {

    private final UserRepository repo;

    public UserController(UserRepository repo) {
        this.repo = repo;
    }

    @PostMapping("login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody UserCredentials creds, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<User> optionalUser = repo.findByUsername(creds.getUsername());
        //user exists and password is correct
        if (optionalUser.isPresent() && optionalUser.get().getPassword().equals(creds.getPassword())) {
            User user = optionalUser.get();
            session.setAttribute("currUser", user);
            response.put("message", "login successful");
            response.put("success", true);
            response.put("userId", user.getId());
        } else {
            response.put("message", "incorrect username or password.");
            response.put("success", false);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("signup")
    public ResponseEntity<Map<String, Object>> register(@RequestBody UserCredentials creds) {
        Map<String, Object> response = new HashMap<>();

        if (creds.getUsername().isEmpty() || creds.getPassword().isEmpty()) {
            response.put("message", "Please enter a valid username and password");
            response.put("success", false);
        }
        else if (repo.existsByUsername(creds.getUsername())){
            response.put("message", "Username has been taken.");
            response.put("success", false);
        }
        else {
            User newUser = repo.save(new User(creds.getUsername(), creds.getPassword()));
            response.put("message", "signup successful. Please login");
            response.put("success", true);
            response.put("userId", newUser.getId());
        }
        return ResponseEntity.ok(response);

    }
    //add logging out
    @PostMapping("logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        if (session.getAttribute("currUser") == null) {
            response.put("message", "youre not logged in silly");
            response.put("success", false);
            return ResponseEntity.status(401).body(response); //unauthorized
        }
        session.invalidate();
        response.put("message", "you have successfully logged out");
        response.put("success", true);
        
        return ResponseEntity.ok(response);
    }
}