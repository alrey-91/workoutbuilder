package com.csc435.workoutbuilder.servicetest;
import com.csc435.workoutbuilder.model.Exercise;
import com.csc435.workoutbuilder.service.FetchExercises;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FetchExercisesTest {

    @Mock
    private RestTemplate restTemplate;

    @Test
    public void test_FetchExercises_ReturnsList() {
        Exercise[] mockExercises = {
            new Exercise("pushup", "arms", "strength", "do pushup", null, null),
            new Exercise("dumbell curls", "arms", "strength", "curl", null, null)
        };
        //mock http response
        ResponseEntity<Exercise[]> mockResp = new ResponseEntity<>(mockExercises, HttpStatus.OK);

        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(Exercise[].class)
        )).thenReturn(mockResp);

        FetchExercises fetchExercises = new FetchExercises(restTemplate);
        List<Exercise> result = fetchExercises.fetch("arms", "strength");

        assertNotNull(result);
         //should return 2
        assertEquals(2, result.size());
        //should return expected muscle and type
        assertEquals("arms", result.get(0).getMuscle());
        assertEquals("arms", result.get(1).getMuscle());
        assertEquals("strength", result.get(0).getType());
        assertEquals("strength", result.get(1).getType());
    }
    
    @Test
    public void test_Fetch_TriggersCatchBlock_ReturnsEmptyList() {
        when(restTemplate.exchange(
            anyString(),
            any(),
            any(),
            eq(Exercise[].class)))
            .thenThrow(new RuntimeException("simulated api failure"));
        
        FetchExercises fetchExercises = new FetchExercises(restTemplate);
        List<Exercise> result = fetchExercises.fetch("fakemuscle","faketype");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
