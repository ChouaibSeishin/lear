package org.lear.aibotservice.config;

import feign.Client;
import feign.Logger;
import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {



    @Bean
    public Client feignClient() {
        return new Client.Default(null, null); //Or use a custom client
    }

    @Bean
    public Request.Options feignOptions() {
        return new Request.Options();
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}


