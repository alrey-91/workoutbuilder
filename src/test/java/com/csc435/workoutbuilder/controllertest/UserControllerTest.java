package com.csc435.workoutbuilder.controllertest;
import com.csc435.workoutbuilder.controller.UserController;
import com.csc435.workoutbuilder.dto.UserCredentials;
import com.csc435.workoutbuilder.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.csc435.workoutbuilder.model.User;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import java.util.Optional;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserRepository userRepo;
    @Autowired
    private ObjectMapper mapper;

    private UserCredentials creds;

    @Test
    public void testLoginSuccess() throws Exception{
        creds = new UserCredentials("testuser", "test123");
        User mockUser = new User("testuser", "test123");
        mockUser.setId(1);

        when(userRepo.findByUsername(mockUser.getUsername())).thenReturn(Optional.of(mockUser));

        ResultActions result = mockMvc.perform(post("/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(creds)));

        result.andExpect(status().isOk())    
        .andExpect(jsonPath("$.message").value("login successful"))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.userId").value(mockUser.getId()));
    }

    @Test
    public void testLoginFail() throws Exception {
        //test incorrect password on login request
        creds = new UserCredentials("testuser", "wrongpassword");
        User mockUser = new User("testuser", "test123");

        when(userRepo.findByUsername(mockUser.getUsername())).thenReturn(Optional.of(mockUser));

        ResultActions result = mockMvc.perform(post("/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(creds)));

        result.andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("incorrect username or password."))
        .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void testLogoutSuccess() throws Exception {
        User mockUser = new User("testuser", "pass123");
        mockUser.setId(1);

        mockMvc.perform(post("/logout")
        .sessionAttr("currUser", mockUser))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("you have successfully logged out"));

    }

    @Test
    public void testLogoutFail() throws Exception {
        //test fail if no session on logout request
        mockMvc.perform(post("/logout"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void testSignupSuccess() throws Exception {
        creds = new UserCredentials("signuptest", "test123");
        User mockUser = new User("signuptest", "test123");
        mockUser.setId(1);

        when(userRepo.existsByUsername("signuptest")).thenReturn(false);
        when(userRepo.save(any(User.class))).thenReturn(mockUser);

        ResultActions result = mockMvc.perform(post("/signup")
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(creds)));

        result.andExpect(status().isOk())    
        .andExpect(jsonPath("$.message").value("signup successful. Please login"))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.userId").value(mockUser.getId()));
    }

    @Test
    public void testSignupFail_UsernameTaken() throws Exception {
        creds = new UserCredentials("existinguser", "test123");

        when(userRepo.existsByUsername("existinguser")).thenReturn(true);

        ResultActions result = mockMvc.perform(post("/signup")
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(creds)));

        result.andExpect(status().isOk())    
        .andExpect(jsonPath("$.message").value("Username has been taken."))
        .andExpect(jsonPath("$.success").value(false));
        
    }

    @Test
    public void testSignupFail_EmptyRequest() throws Exception {
        //empty request body
        creds = new UserCredentials("", "");

        ResultActions result = mockMvc.perform(post("/signup")
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(creds)));

        result.andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Please enter a valid username and password"));
    }

    @Test
    public void testSignupFail_EmptyUsername() throws Exception {
        //empty username field
        creds = new UserCredentials("", "test123");

        ResultActions result = mockMvc.perform(post("/signup")
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(creds)));

        result.andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Please enter a valid username and password"));
    }
    @Test
    public void testSignupFail_EmptyPassword() throws Exception {
        //empty pass
        creds = new UserCredentials("testuser", "");

        ResultActions result = mockMvc.perform(post("/signup")
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(creds)));

        result.andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Please enter a valid username and password"));
    }
}
