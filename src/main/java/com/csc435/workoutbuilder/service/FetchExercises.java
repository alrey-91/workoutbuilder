package com.csc435.workoutbuilder.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Component;
import com.csc435.workoutbuilder.model.Exercise;
import java.util.*;
import java.lang.StringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class FetchExercises {
    private static final String API_URL = "https://api.api-ninjas.com/v1/exercises";
    
    @Value("${api.ninjas.key}")
    private String KEY;
    
    private final RestTemplate restTemplate;
    private final Logger logger = LoggerFactory.getLogger(FetchExercises.class);
    
    public FetchExercises(RestTemplate restTemplate) {
        //constructor injection
        this.restTemplate = restTemplate;
    }
    
    public List<Exercise> fetch(String muscle, String type) {
        logger.debug("calling external API at '{}'...", API_URL);
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
            if (Arrays.asList(resp.getBody()).size()  == 0) {
                logger.info("Did not fetch any exercises from external API with params muscle='{}' and type='{}'",muscle, type);
            } else {
                logger.info("Fetched exercises successfully from external API with params muscle='{}' and type='{}'",muscle, type);
            }  
            
            logger.info("Fetched {} exercises from API", Arrays.asList(resp.getBody()).size());
            return Arrays.asList(resp.getBody());     
        } else {
            logger.error("api error: HTTP status code {}", resp.getStatusCode());
            return List.of();
        }
    } catch (Exception e) {
        e.printStackTrace();
        logger.error("Error fetching exercises from API: ", e);
        return List.of();    
    }
    
    }
}