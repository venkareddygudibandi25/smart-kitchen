package com.smartkitchen.scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulerConfig {

	@Value("${scheduler.concurrency}")
	private int concurrency;

	@Bean
	public ExecutorService executorService() {
		return Executors.newFixedThreadPool(concurrency);
	}
}