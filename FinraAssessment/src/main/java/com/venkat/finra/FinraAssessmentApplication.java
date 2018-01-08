package com.venkat.finra;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.venkat.finra.service.FileUploadService;

@SpringBootApplication
public class FinraAssessmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinraAssessmentApplication.class, args);
	}
	
}
