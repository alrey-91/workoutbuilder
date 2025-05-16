package com.csc435.workoutbuilder.config;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomLogoutSuccessHandler.class);
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
        throws IOException {
            Map<String, Object> json = new HashMap<>();

            if (authentication == null ) {
                logger.warn("failed logout attempt: authenticated user not found in session");
                json.put("success",false);
                json.put("message","You are not logged in");

                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(mapper.writeValueAsString(json));

            } else {
                logger.info("User {} logged out successfully", authentication.getName());
                
            
                json.put("success",true);
                json.put("message","you have successfully logged out");

                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(mapper.writeValueAsString(json));
            }
        }
}
