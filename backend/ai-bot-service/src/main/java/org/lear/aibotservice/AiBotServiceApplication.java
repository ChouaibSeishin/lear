package org.lear.aibotservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
		org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration.class
})
@EnableCaching
@EnableFeignClients
@EnableScheduling

public class AiBotServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiBotServiceApplication.class, args);
	}


}
