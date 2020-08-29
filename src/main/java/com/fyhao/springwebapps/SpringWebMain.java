package com.fyhao.springwebapps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class SpringWebMain {
	static Logger logger = LoggerFactory.getLogger(SpringWebMain.class);
	public static void main(String[] args) {
		SpringApplication.run(SpringWebMain.class, args);
	}
}
