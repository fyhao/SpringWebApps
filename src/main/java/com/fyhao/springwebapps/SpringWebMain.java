package com.fyhao.springwebapps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


@SpringBootApplication
public class SpringWebMain {
	static Logger logger = LoggerFactory.getLogger(SpringWebMain.class);
	public static void main(String[] args) {
		SpringApplication.run(SpringWebMain.class, args);
	}
}
