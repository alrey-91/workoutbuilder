package com.csc435.workoutbuilder.config;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.csc435.workoutbuilder.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuthLoginSuccessHandler implements AuthenticationSuccessHandler{
    
    private final Logger logger = LoggerFactory.getLogger(LoginSuccessHandler.class);
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
        throws IOException {

            CustomOAuth2User principal = (CustomOAuth2User) authentication.getPrincipal();
            User user = principal.getUser();

            Map<String, Object> json = new HashMap<>();
            json.put("success", true);
            json.put("message", "oauth2 login successful");
            json.put("userId", user.getId());

            logger.info("oauth2 login for userId {} successful. Provider: {}", user.getId(), user.getProvider());
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(mapper.writeValueAsString(json));
        }

}
