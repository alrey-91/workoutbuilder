package com.csc435.workoutbuilder.controllertest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.csc435.workoutbuilder.dto.UserCredentials;
import com.csc435.workoutbuilder.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import com.csc435.workoutbuilder.model.User;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class UserControllerIT {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserRepository userRepo;

    private UserCredentials creds;
    
    /* integration test for User controller
        1. test signup with new user creds
        2. test login with newly created user
        3. test logout
        4. test fail use cases aswell
     */
  
    @Test
    @Order(1)
    public void test_SignupSuccess() throws Exception {
        creds = new UserCredentials("testuser","testpass");

        mockMvc.perform(post("/signup")
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(creds)))
        .andExpect(status().isOk())    
        .andExpect(jsonPath("$.message").value("signup successful. Please login"))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.userId").value(1));
    }
    @Test
    @Order(2)
    public void test_SignupFail_UsernameTaken() throws Exception {
        creds = new UserCredentials("testuser", "testpass");

        mockMvc.perform(post("/signup")
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(creds)))
        .andExpect(status().isOk())    
        .andExpect(jsonPath("$.message").value("Username has been taken."))
        .andExpect(jsonPath("$.success").value(false));    
    }
    @Test
    public void test_SignupFail_EmptyRequest() throws Exception {
        //empty request body
        creds = new UserCredentials("", "");

        mockMvc.perform(post("/signup")
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(creds)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Please enter a valid username and password"));
    }

    @Test
    @Order(3)
    public void test_LoginFail() throws Exception {
        creds = new UserCredentials("testuser", "wrongpass");

        mockMvc.perform(post("/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(creds)))
        .andExpect(status().isOk())    
        .andExpect(jsonPath("$.message").value("incorrect username or password."))
        .andExpect(jsonPath("$.success").value(false));
    }
    @Test
    @Order(4)
    public void test_LoginSuccess() throws Exception {
        creds = new UserCredentials("testuser", "testpass");

        MvcResult result = mockMvc.perform(post("/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(creds)))
        .andExpect(status().isOk())    
        .andExpect(jsonPath("$.message").value("login successful"))
        .andExpect(jsonPath("$.success").value(true))
        //should be id =1
        .andExpect(jsonPath("$.userId").value(1))
        .andReturn();

        //see if it created user session
        HttpSession session = result.getRequest().getSession(false);
        assertNotNull(session.getAttribute("currUser"));
        assertTrue(session.getAttribute("currUser") instanceof User);
        //check if session username == login user
        User user = (User)session.getAttribute("currUser");
        assertEquals(userRepo.findByUsername(creds.getUsername()).get().getId(), user.getId());
    }

    @Test
    @Order(5)
    public void test_LogoutSuccess() throws Exception {
        //login again to create session
        UserCredentials creds = new UserCredentials("testuser", "testpass");
        MvcResult loginResult = mockMvc.perform(post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(creds)))
            .andExpect(status().isOk())
            .andReturn();
        MockHttpSession session = (MockHttpSession)loginResult.getRequest().getSession(false);
        assertNotNull(session);

        mockMvc.perform(post("/logout")
        .session(session))
        .andExpect(status().isOk())    
        .andExpect(jsonPath("$.message").value("you have successfully logged out"))
        .andExpect(jsonPath("$.success").value(true));
        assertTrue(session.isInvalid());
    }
}
