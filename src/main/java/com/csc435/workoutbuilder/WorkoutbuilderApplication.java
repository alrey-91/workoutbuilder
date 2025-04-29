package com.csc435.workoutbuilder;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
//import com.csc435.workoutbuilder.service.FetchExercises;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import java.util.Scanner;

@SpringBootApplication
public class WorkoutbuilderApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkoutbuilderApplication.class, args);
		
	}
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
	/* test
	@Bean
	CommandLineRunner run(FetchExercises fetchExercises) {
		return args -> {
			System.out.println("testing fetch exercises: enter muscle then type");
			Scanner sc = new Scanner(System.in);
			String muscle = sc.nextLine();
			String type = sc.nextLine();
			var exercises = fetchExercises.fetch(muscle, type);
			
			System.out.println("fetched" + exercises.size());
			ObjectMapper mapper = new ObjectMapper();
			String jsonresp = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(exercises);
			System.out.println(jsonresp);
		};
	}
		*/	 
}
	


