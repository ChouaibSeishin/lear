package org.lear.aibotservice.config;

import org.lear.aibotservice.services.EnhancedGeminiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class RAGConfiguration {

    @Bean
    @Primary
    public EnhancedGeminiClient enhancedGeminiClient() {
        return new EnhancedGeminiClient();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("rag-async-");
        executor.initialize();
        return executor;
    }
}
