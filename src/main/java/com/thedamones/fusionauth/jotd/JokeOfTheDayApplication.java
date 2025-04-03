package com.thedamones.fusionauth.jotd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.hateoas.config.EnableHypermediaSupport;

@SpringBootApplication
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
public class JokeOfTheDayApplication {

	public static void main(String[] args) {
		SpringApplication.run(JokeOfTheDayApplication.class, args);
	}

}
