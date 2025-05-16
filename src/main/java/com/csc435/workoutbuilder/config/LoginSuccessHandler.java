package com.csc435.workoutbuilder.config;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import com.csc435.workoutbuilder.model.User;
import com.csc435.workoutbuilder.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.HashMap;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler{
    
    private final Logger logger = LoggerFactory.getLogger(LoginSuccessHandler.class);
    private final UserRepository userRepo;
    private final ObjectMapper mapper = new ObjectMapper();
    //for form login
    public LoginSuccessHandler(UserRepository userRepo) {
        this.userRepo = userRepo;
    }
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String username = authentication.getName();
        User user = userRepo.findByUsername(username).get();

        Map<String, Object> json = new HashMap<>();
        json.put("success", true);
        json.put("message", "form login successful");
        json.put("userId", user.getId());

        logger.info("login attempt for user '{}' successful. userId: {}",user.getUsername(), user.getId());
       
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(mapper.writeValueAsString(json));
    }
}