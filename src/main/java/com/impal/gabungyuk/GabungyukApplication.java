package com.impal.gabungyuk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication
@EnableScheduling
public class GabungyukApplication {

	public static void main(String[] args) {
		SpringApplication.run(GabungyukApplication.class, args);
	}

}