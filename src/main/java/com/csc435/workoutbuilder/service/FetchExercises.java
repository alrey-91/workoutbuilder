package com.csc435.workoutbuilder.service;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Component;
import com.csc435.workoutbuilder.model.Exercise;
import java.util.*;
import java.lang.StringBuilder;

@Component
public class FetchExercises {
    private static final String API_URL = "https://api.api-ninjas.com/v1/exercises";
    private static final String KEY = "qCxpwnAI9M50dCpnf+F5/Q==Qw2uHLCjkWFRlyKn";
    
    private final RestTemplate restTemplate;
    
    public FetchExercises(RestTemplate restTemplate) {
        //constructor injection
        this.restTemplate = restTemplate;
    }
    
    public List<Exercise> fetch(String muscle, String type) {

        try {
            StringBuilder buildUrl = new StringBuilder(API_URL);
            boolean hasParam = false;

            if (muscle != null && !muscle.isEmpty()) {
                buildUrl.append("?muscle=").append(muscle);
                hasParam = true;
            }
            if (type != null && !type.isEmpty()) {
                buildUrl.append(hasParam ? "&" : "?");
                buildUrl.append("type=").append(type);
        }
        //headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Api-Key", KEY);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        //exercises response
        ResponseEntity<Exercise[]> resp = restTemplate.exchange(buildUrl.toString(),HttpMethod.GET, request, Exercise[].class);

        if (resp.getStatusCode() == HttpStatus.OK) {
            System.out.println("fetched exercises successfully");
            return Arrays.asList(resp.getBody());
            
        } else {
            System.out.println("api error" + resp.getStatusCode());
            return List.of();
        }
    } catch (Exception e) {
        e.printStackTrace();
        return List.of();
    }
    
    }
}